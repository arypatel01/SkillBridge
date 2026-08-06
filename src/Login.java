import Database.DBConnection;
import Util.InputUtil;

import java.sql.*;
import java.util.Scanner;

class Login{
    
    Validations obj = new Validations();
    
    boolean handleAdminLogin()  {
        String name = InputUtil.readString("\nEnter username : ");
        System.out.print("Enter Password : ");
        String password = InputUtil.getScanner().nextLine();
        if (name.equalsIgnoreCase("admin") && password.equals("admin123")){
            return true;
        }
        return false;
    }
    
    boolean handleStudentLogin() throws SQLException {

        Connection connection = DBConnection.getConnection();

        String email;

        while (true) {

            email = InputUtil.readString("Enter Email : ");

            if (obj.isValidEmail(email)) {
                break;
            }

            System.out.println("Invalid Email Format.");
        }

        String password;

        while (true) {

            System.out.print("Enter Password : ");
            password = InputUtil.getScanner().nextLine();

            if (password.isBlank()) {
                System.out.println("Password cannot be empty.");
                continue;
            }

            break;
        }

        password = obj.hashPassword(password);

        String query = """
            SELECT student_id,name,email,branch,phone,average_rating,total_sessions
            FROM Students
            WHERE email=? AND password=?
            """;

        PreparedStatement ps = connection.prepareStatement(query);

        ps.setString(1, email);
        ps.setString(2, password);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {

            Menu.currentStudentId = rs.getInt(1);

            String name = rs.getString(2);
            String emailId = rs.getString(3);
            String Branch = rs.getString(4);
            String phone = rs.getString(5);
            double rating = rs.getDouble(6);
            int sessions = rs.getInt(7);

            Menu.loggedInStudent = new Student(Menu.currentStudentId,name,emailId,Branch,phone,rating,sessions);

            System.out.println("\n========================================");
            System.out.println("Login Successful!");
            System.out.println("Welcome " + name);
            System.out.println("Student ID : " + Menu.currentStudentId);
            System.out.println("========================================");
            return true;

        } else {

            System.out.println("\nInvalid Email or Password.");

        }

        rs.close();
        ps.close();
        connection.close();
        return false;
    }

    String registerStudent() throws SQLException {

        Connection connection = DBConnection.getConnection();

        String name;
        while (true) {

            name = InputUtil.readString("Enter Name : ");

            if (obj.isValidName(name)) {
                break;
            }

            System.out.println("Invalid Name.");
            System.out.println("Name must contain only letters and spaces and be at least 3 characters long.");
        }

        String email;

        while (true) {

            email = InputUtil.readString("Enter Email : ");

            if (!obj.isValidEmail(email)) {
                System.out.println("Invalid Email.");
                continue;
            }

            PreparedStatement checkEmail = connection.prepareStatement(
                    "SELECT student_id FROM Students WHERE email=?");

            checkEmail.setString(1, email);

            ResultSet rs = checkEmail.executeQuery();

            if (rs.next()) {
                System.out.println("Email already registered.");
            } else {
                break;
            }

            rs.close();
            checkEmail.close();
        }

        String password;

        while (true) {

            System.out.print("Enter Password : ");
            password = InputUtil.getScanner().nextLine();

            if (password.length() < 8) {
                System.out.println("Password must be at least 8 characters.");
                continue;
            }

            break;
        }

        password = obj.hashPassword(password);

        String branch;

        while (true) {

            System.out.println("""
                Select Branch
                1. Computer Engineering
                2. Information Technology
                3. Mechanical
                4. Civil
                5. Electrical
                6. Electronics
                """);

            int choice = InputUtil.readInt("Enter choice : ");

            switch (choice) {
                case 1 -> {
                    branch = "Computer Engineering";
                    break;
                }
                case 2 -> {
                    branch = "Information Technology";
                    break;
                }
                case 3 -> {
                    branch = "Mechanical";
                    break;
                }
                case 4 -> {
                    branch = "Civil";
                    break;
                }
                case 5 -> {
                    branch = "Electrical";
                    break;
                }
                case 6 -> {
                    branch = "Electronics";
                    break;
                }
                default -> {
                    System.out.println("Invalid Choice.");
                    continue;
                }
            }
            break;
        }

        String phoneNumber;

        while (true) {
            phoneNumber = InputUtil.readString("Enter Phone Number: ");

            if (new Validations().isValidPhoneNumber(phoneNumber)) {
                break; // Exit loop if phone number is valid
            } else {
                System.out.println("Invalid Phone Number!");
                System.out.println("Phone number must:");
                System.out.println("- Contain exactly 10 digits");
                System.out.println("- Start with 6, 7, 8, or 9");
                System.out.println("- Contain only numbers\n");
            }
        }

        String securityQuestion = InputUtil.readString("Enter Security Question : ");

        String securityAnswer = InputUtil.readString("Enter Security Answer : ");

        securityAnswer = obj.hashPassword(securityAnswer);

        String query = """
            INSERT INTO Students
            (name,email,password,branch,phone,security_question,security_answer)
            VALUES(?,?,?,?,?,?,?)
            """;

        PreparedStatement ps = connection.prepareStatement(query);

        ps.setString(1, name);
        ps.setString(2, email);
        ps.setString(3, password);
        ps.setString(4, branch);
        ps.setString(5, phoneNumber);
        ps.setString(6, securityQuestion);
        ps.setString(7, securityAnswer);

        int rows = ps.executeUpdate();

        if (rows > 0) {
            System.out.println("\nStudent Registered Successfully.");
            ps.close();
            connection.close();
            return name;
        } else {
            System.out.println("\nRegistration Failed.");
            ps.close();
            connection.close();
            return null;
        }

    }

    void handleForgotPassword(String email) throws SQLException {

        Connection connection = DBConnection.getConnection();

        PreparedStatement ps = connection.prepareStatement(
                "SELECT security_question, security_answer FROM Students WHERE email = ?");

        ps.setString(1, email);

        ResultSet rs = ps.executeQuery();

        if (!rs.next()) {
            System.out.println("No student found with this email.");

            rs.close();
            ps.close();
            connection.close();
            return;
        }

        String securityQuestion = rs.getString("security_question");
        String dbAnswer = rs.getString("security_answer");

        System.out.print("\nSecurity Question :");
        System.out.println(securityQuestion);

        String answer = InputUtil.readString("Enter Security Answer: ");

        answer = obj.hashPassword(answer);

        if (!answer.equals(dbAnswer)) {

            System.out.println("Incorrect Security Answer.");

            rs.close();
            ps.close();
            connection.close();
            return;
        }

        String newPassword;

        while (true) {

            System.out.print("Enter New Password: ");
            newPassword = InputUtil.getScanner().nextLine();

            if (newPassword.length() < 8) {
                System.out.println("Password must contain at least 8 characters.");
                continue;
            }

            System.out.print("Confirm Password: ");
            String confirmPassword = InputUtil.getScanner().nextLine();

            if (!newPassword.equals(confirmPassword)) {
                System.out.println("Passwords do not match.");
                continue;
            }

            break;
        }

        newPassword = obj.hashPassword(newPassword);

        PreparedStatement update = connection.prepareStatement(
                "UPDATE Students SET password = ? WHERE email = ?");

        update.setString(1, newPassword);
        update.setString(2, email);

        int rows = update.executeUpdate();

        if (rows > 0) {
            System.out.println("\nPassword Changed Successfully.");
        } else {
            System.out.println("\nFailed to Change Password.");
        }

        update.close();
        rs.close();
        ps.close();
        connection.close();
    }
}