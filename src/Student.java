import Database.DBConnection;
import Util.InputUtil;

import java.io.IOException;
import java.sql.*;

public class Student {

    private int studentId;
    private String name;
    private String email;
    private String branch;
    private String phone;
    private double averageRating;
    private int totalSessions;
    private Timestamp Created_At;
    private HistoryService historyService;

    public Student(int studentId, String name, String email,
                   String branch, String phone,
                   double averageRating, int totalSessions) {

        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.branch = branch;
        this.phone = phone;
        this.averageRating = averageRating;
        this.totalSessions = totalSessions;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getBranch() {
        return branch;
    }

    public String getPhone() {
        return phone;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public int getTotalSessions() {
        return totalSessions;
    }

    @Override
    public String toString() {
        return "\nStudent ID      : " + studentId +
                "\nName            : " + name +
                "\nEmail           : " + email +
                "\nBranch          : " + branch +
                "\nPhone           : " + phone +
                "\nAverage Rating  : " + averageRating +
                "\nTotal Sessions  : " + totalSessions;
    }

    void studentPortal(Student s,HistoryService historyService) throws Exception {
        this.historyService = historyService;

        while (true) {

            System.out.println("\n\n===================== STUDENT DASHBOARD =====================");
            System.out.println("Welcome : " + getName());
            System.out.println("Student ID : " + getStudentId());
            System.out.println("=============================================================");
            System.out.println("1. View Profile");
            System.out.println("2. Edit Profile");
            System.out.println("3. Learning Module");
            System.out.println("4. Top Rated Students");
            System.out.println("5. Q&A Forum");
            System.out.println("6. Portfolio");
            System.out.println("7. Logout");
            System.out.println("=============================================================");

            int choice = InputUtil.readInt("Enter Choice : ");

            switch (choice) {

                case 1 -> {
                    viewProfile();
                }

                case 2 -> {
                    editProfile();
                }

                case 3 -> {
                    new LearningModule().learningRequestMenu(historyService);
                }

                case 4 -> {
                    topRatedStudents();
                }

                case 5 -> {
                    new QuestionAnswerForum().QuestionAnswerForum(historyService);
                }
                case 6 -> {
                    System.out.println(PortfolioWriter.generatePortfolio(s));
                }
                case 7 -> {

                    System.out.println("\nLogged Out Successfully.");

                    Menu.loggedInStudent = null;
                    Menu.currentStudentId = -1;

                    return;
                }

                default -> System.out.println("Invalid Choice.");
            }
        }
    }
    void viewProfile() {

        System.out.println("\n\n==================== MY PROFILE ====================");

        System.out.println("Student ID      : " + studentId);
        System.out.println("Name            : " + name);
        System.out.println("Email           : " + email);
        System.out.println("Branch          : " + branch);
        System.out.println("Phone           : " + phone);
        System.out.printf("Average Rating  : %.2f\n", averageRating);
        System.out.println("Total Sessions  : " + totalSessions);

        System.out.println("====================================================");
    }
    void editProfile() throws SQLException {

        Validations obj = new Validations();
        Connection connection = DBConnection.getConnection();

        while (true) {

            System.out.println("\n========== EDIT PROFILE ==========");
            System.out.println("1. Change Name");
            System.out.println("2. Change Email");
            System.out.println("3. Change Branch");
            System.out.println("4. Change Phone");
            System.out.println("5. Back");
            System.out.println("==================================");

            int choice = InputUtil.readInt("Enter Choice : ");
//            InputUtil.getScanner().nextLine();

            switch (choice) {

                case 1 -> {

                    String newName;

                    while (true) {

                        newName = InputUtil.readString("Enter Name : ");

                        if (!obj.isValidName(newName)) {
                            System.out.println("Invalid Name.");
                            System.out.println("Name must contain only letters and spaces and be at least 3 characters long.");
                            continue;
                        }

                        if (newName.equalsIgnoreCase(name)) {
                            System.out.println("New name cannot be the same as the current name.");
                            continue;
                        }

                        break;
                    }

                    PreparedStatement ps = connection.prepareStatement(
                            "UPDATE Students SET name=? WHERE student_id=?");

                    ps.setString(1, newName);
                    ps.setInt(2, studentId);

                    if (ps.executeUpdate() > 0) {
                        name = newName;
                        System.out.println("Name Updated Successfully.");
                        historyService.logEvent("Student '" + getName() + "' updated their profile.");
                    }

                    ps.close();
                }

                case 2 -> {

                    String newEmail;

                    while (true) {

                        newEmail = InputUtil.readString("Enter New Email : ");

                        if (!obj.isValidEmail(newEmail)) {
                            System.out.println("Invalid Email.");
                            continue;
                        }

                        PreparedStatement check = connection.prepareStatement(
                                "SELECT student_id FROM Students WHERE email=?");

                        check.setString(1, newEmail);

                        ResultSet rs = check.executeQuery();

                        if (rs.next()) {
                            System.out.println("Email Already Exists.");
                            rs.close();
                            check.close();
                            continue;
                        }

                        rs.close();
                        check.close();

                        break;
                    }

                    PreparedStatement ps = connection.prepareStatement(
                            "UPDATE Students SET email=? WHERE student_id=?");

                    ps.setString(1, newEmail);
                    ps.setInt(2, studentId);

                    if (ps.executeUpdate() > 0) {
                        email = newEmail;
                        System.out.println("Email Updated Successfully.");
                        historyService.logEvent("Student '" + getName() + "' updated their profile.");
                    }

                    ps.close();
                }

                case 3 -> {

                    String newBranch;

                    while (true) {

                        System.out.println("""
                            1. Computer Engineering
                            2. Information Technology
                            3. Mechanical
                            4. Civil
                            5. Electrical
                            6. Electronics
                            """);

                        int ch = InputUtil.readInt("Enter choice : ");
                        InputUtil.getScanner().nextLine();

                        switch (ch) {

                            case 1 -> newBranch = "Computer Engineering";
                            case 2 -> newBranch = "Information Technology";
                            case 3 -> newBranch = "Mechanical";
                            case 4 -> newBranch = "Civil";
                            case 5 -> newBranch = "Electrical";
                            case 6 -> newBranch = "Electronics";
                            default -> {
                                System.out.println("Invalid Choice.");
                                continue;
                            }
                        }

                        PreparedStatement ps = connection.prepareStatement(
                                "UPDATE Students SET branch=? WHERE student_id=?");

                        ps.setString(1, newBranch);
                        ps.setInt(2, studentId);

                        if (ps.executeUpdate() > 0) {
                            branch = newBranch;
                            System.out.println("Branch Updated Successfully.");
                            historyService.logEvent("Student '" + getName() + "' updated their profile.");
                        }

                        ps.close();
                        break;
                    }
                }

                case 4 -> {

                    String newPhone;

                    while (true) {
                        newPhone = InputUtil.readString("Enter Phone Number: ");

                        if (new Validations().isValidPhoneNumber(newPhone)) {
                            break; // Exit loop if phone number is valid
                        } else {
                            System.out.println("Invalid Phone Number!");
                            System.out.println("Phone number must:");
                            System.out.println("- Contain exactly 10 digits");
                            System.out.println("- Start with 6, 7, 8, or 9");
                            System.out.println("- Contain only numbers\n");
                        }
                    }

                    PreparedStatement ps = connection.prepareStatement(
                            "UPDATE Students SET phone=? WHERE student_id=?");

                    ps.setString(1, newPhone);
                    ps.setInt(2, studentId);

                    if (ps.executeUpdate() > 0) {
                        phone = newPhone;
                        System.out.println("Phone Updated Successfully.");
                        historyService.logEvent("Student '" + getName() + "' updated their profile.");
                    }

                    ps.close();
                }

                case 5 -> {
                    connection.close();
                    return;
                }

                default -> System.out.println("Invalid Choice.");
            }
        }
    }
    void topRatedStudents() throws SQLException {

        Connection connection = DBConnection.getConnection();

        PreparedStatement ps = connection.prepareStatement("""
            SELECT
                student_id,
                name,
                branch,
                average_rating,
                total_sessions
            FROM Students
            WHERE total_sessions > 0
            ORDER BY average_rating DESC,
                     total_sessions DESC
            LIMIT 10
            """);

        ResultSet rs = ps.executeQuery();

        boolean found = false;
        int rank = 1;

        System.out.println("\n============= TOP 10 RATED STUDENTS =============");

        while (rs.next()) {

            found = true;

            System.out.println("\n----------------------------------------------");
            System.out.println("Rank            : " + rank++);
            System.out.println("Student ID      : " + rs.getInt("student_id"));
            System.out.println("Name            : " + rs.getString("name"));
            System.out.println("Branch          : " + rs.getString("branch"));
            System.out.printf("Average Rating  : %.2f%n", rs.getDouble("average_rating"));
            System.out.println("Total Sessions  : " + rs.getInt("total_sessions"));
            System.out.println("----------------------------------------------");
        }

        if (!found) {
            System.out.println("\nNo Rated Students Found.");
        }

        rs.close();
        ps.close();
        connection.close();
    }
}