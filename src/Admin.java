import Util.InputUtil;
import Database.DBConnection;

import java.io.IOException;
import DS.ArrayList;
import java.sql.*;

class Admin {

    private final HistoryService historyService;

    Admin(HistoryService historyService) {
        this.historyService = historyService;
    }


    public void start(){

        boolean running = true;
        System.out.println("\n\n===================== ADMIN DASHBOARD =====================");
        System.out.println(" Logged in as: Admin");
        System.out.println("-----------------------------------------------------------");

        while (running) {

            System.out.println("1. Manage Students");
            System.out.println("2. Manage Learning Requests");
            System.out.println("3. Manage Sessions");
            System.out.println("4. Manage Questions");
            System.out.println("5. Manage Answers");
            System.out.println("6. View Leaderboard");
            System.out.println("7. History Logs");
            System.out.println("8. Logout");
            System.out.println("-----------------------------------------------------------");
            int choice = InputUtil.readInt("Your choice: ");

            switch (choice) {
                case 1: manageStudents();          break;
                case 2: manageLearningRequests();  break;
                case 3: manageSessions();          break;
                case 4: manageQuestions();         break;
                case 5: manageAnswers();           break;
                case 6: viewLeaderboard();         break;
                case 7: viewHistoryLogs();         break;
                case 8:
                    System.out.println("\nLogging out... Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // ============================
    //  1. MANAGE STUDENTS
    // ============================

    private void manageStudents() {
        boolean inMenu = true;

        while (inMenu) {
            System.out.println("\n  ---- MANAGE STUDENTS ----");
            System.out.println("1. View All Students");
            System.out.println("2. Delete a Student");
            System.out.println("3. Back");

            int choice = InputUtil.readInt("Choice : ");

            switch (choice) {
                case 1: viewAllStudents();   break;
                case 2: deleteStudent();     break;
                case 3: inMenu = false;      break;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private void viewAllStudents() {
        try {
            String sql = "SELECT * FROM Students ORDER BY name";

            ArrayList<Student> students = new ArrayList<>();

            Connection conn = DBConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                students.add(new Student(
                        rs.getInt("student_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("branch"),
                        rs.getString("phone"),
                        rs.getDouble("average_rating"),
                        rs.getInt("total_sessions")
                ));
            }

            System.out.println();

            System.out.printf("%-5s %-20s %-30s %-25s %-8s %-8s%n",
                    "ID", "Name", "Email", "Branch", "Rating", "Sessions");

            System.out.println("---------------------------------------------------------------------------------------------------------------");

            for (Student s : students) {
                System.out.printf("%-5d %-20s %-30s %-25s %-8.2f %-8d%n",
                        s.getStudentId(),
                        s.getName(),
                        s.getEmail(),
                        s.getBranch(),
                        s.getAverageRating(),
                        s.getTotalSessions());
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.print("\nPress Enter...");
        InputUtil.getScanner().nextLine();
    }

    private void deleteStudent() {
        System.out.print("Enter Student ID to delete : ");
        int id = InputUtil.readInt("  ");
        try {
            String sql = "DELETE FROM Students WHERE student_id = ?";

            Connection conn = DBConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1,id);
            int r = pst.executeUpdate();
            if (r > 0) {
                System.out.println("Student deleted.");
                historyService.logEvent("Admin deleted student #" + id);
            } else {
                System.out.println("Student not found.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ============================
    //  2. MANAGE LEARNING REQUESTS
    // ============================

    private void manageLearningRequests() {
        boolean inMenu = true;

        while (inMenu) {
            System.out.println("\n  ---- MANAGE LEARNING REQUESTS ----");
            System.out.println("1. View All Requests");
            System.out.println("2. Back");

            int choice = InputUtil.readInt("Choice : ");

            switch (choice) {
                case 1: viewAllRequests();   break;
                case 2: inMenu = false;      break;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private void viewAllRequests() {
        try {
            String sql = "SELECT lr.*, s.name AS student_name FROM LearningRequests lr JOIN " +
                    "Students s ON lr.student_id = s.student_id ORDER BY lr.created_at DESC";

            Connection conn = DBConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            System.out.printf("\n  %-6s %-20s %-20s %-16s %-12s%n",
                    "ID", "Skill", "Student", "Status", "Date");
            System.out.println("  " + "----------------------------------------------------------------------------------------------------");
            while (rs.next()){
                System.out.printf("  %-6d %-20s %-20s %-16s %-12s%n",
                        rs.getInt("request_id"), rs.getString("skill_needed"),
                        rs.getString("student_name"), rs.getString("status"),
                        rs.getTimestamp("created_at"), "-");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        System.out.print("\nPress Enter...");
        InputUtil.getScanner().nextLine();
    }

    // ============================
    //  3. MANAGE SESSIONS
    // ============================

    private void manageSessions() {
        System.out.println("\n  ---- ALL SESSIONS ----");
        try {
            String sql = "SELECT ls.*, t.name AS teacher_name, l.name AS learner_name " +
                    "FROM LearningSessions ls " +
                    "JOIN Students t ON ls.teacher_id = t.student_id " +
                    "JOIN Students l ON ls.learner_id = l.student_id " +
                    "ORDER BY ls.created_at DESC";

            Connection conn = DBConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            if (rs.next()) {
                System.out.printf("  %-6s %-20s %-20s %-12s %-20s%n",
                        "ID", "Teacher", "Learner", "Date", "Status");
                System.out.println("  " + "---------------------------------------------------------------------------------------------------------");
                do {
                    System.out.printf("  %-6d %-20s %-20s %-12s %-20s%n",
                            rs.getInt("session_id"), rs.getString("teacher_name"),
                            rs.getString("learner_name"), rs.getDate("meeting_date"),
                            rs.getString("status"));
                }while (rs.next());
            }
            else {
                System.out.println("Data not found");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        System.out.print("\nPress Enter...");
        InputUtil.getScanner().nextLine();
    }

    // ============================
    //  4. MANAGE QUESTIONS
    // ============================

    private void manageQuestions() {
        boolean inMenu = true;

        while (inMenu) {
            System.out.println("\n  ---- MANAGE QUESTIONS ----");
            System.out.println("1. View All Questions");
            System.out.println("2. Delete a Question");
            System.out.println("3. Back");

            int choice = InputUtil.readInt("Choice : ");

            switch (choice) {
                case 1: viewAllQuestions();  break;
                case 2: deleteQuestion();    break;
                case 3: inMenu = false;      break;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private void viewAllQuestions() {
        try {
            String sql = "SELECT q.*, s.name AS student_name " +
                    "FROM Questions q " +
                    "JOIN Students s ON q.asked_by_student_id = s.student_id " +
                    "ORDER BY q.created_at DESC";

            Connection conn = DBConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()){
                do {
                    System.out.println("Q-" + rs.getInt("question_id") + ". " +
                            rs.getString("title") + " - by " +
                            rs.getString("student_name") + " (" +
                            rs.getTimestamp("created_at") + ")");
                }while (rs.next());
            }else {
                System.out.println("No Question yet!!");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        System.out.print("\nPress Enter...");
        InputUtil.getScanner().nextLine();
    }

    private void deleteQuestion() {
        int qId = InputUtil.readInt("Enter Question ID to delete: ");
        try {
            String sql = "DELETE FROM Questions WHERE question_id = ?";

            Connection conn = DBConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1,qId);
            int r = pst.executeUpdate();
            if (r > 0) {
                System.out.println("  Question deleted.");
                historyService.logEvent("Admin deleted question #" + qId);
            } else {
                System.out.println("Question not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ============================
    //  5. MANAGE ANSWERS
    // ============================

    private void manageAnswers() {
        boolean inMenu = true;

        while (inMenu) {
            System.out.println("\n  ---- MANAGE ANSWERS ----");
            System.out.println("1. View Answers for a Question");
            System.out.println("2. Delete an Answer");
            System.out.println("3. Back");

            int choice = InputUtil.readInt("Choice : ");

            switch (choice) {
                case 1: viewAnswersForQuestion(); break;
                case 2: deleteAnswer();           break;
                case 3: inMenu = false;           break;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private void viewAnswersForQuestion() {
        int qId = InputUtil.readInt("Enter Question ID: ");
        try {
            String sql1 = "SELECT q.*, s.name AS student_name " +
                    "FROM Questions q " +
                    "JOIN Students s ON q.asked_by_student_id = s.student_id " +
                    "WHERE q.question_id = ?";

            Connection conn1 = DBConnection.getConnection();
            PreparedStatement pst1 = conn1.prepareStatement(sql1);
            pst1.setInt(1,qId);
            ResultSet rs1 = pst1.executeQuery();
            if (!rs1.next()) {
                System.out.println("Question not found.");
                return;
            }
            System.out.println("Q: " + rs1.getString("title"));
            String sql2 = "SELECT a.*, s.name AS student_name " +
                    "FROM Answers a " +
                    "JOIN Students s ON a.answered_by_student_id = s.student_id " +
                    "WHERE a.question_id = ? " +
                    "ORDER BY a.is_best_answer DESC, a.created_at ASC";

            Connection conn2 = DBConnection.getConnection();
            PreparedStatement pst2 = conn2.prepareStatement(sql2);
            pst2.setInt(1,qId);
            ResultSet rs2 = pst2.executeQuery();
            if (rs2.next()) {
                do {
                    System.out.println("  A-" + rs2.getInt("answer_id") + " by " +
                            rs2.getString("student_name") + " ("+
                            rs2.getTimestamp("created_at") + ")");
                }while (rs2.next());
            } else {
                System.out.println("No answers for this question.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        System.out.print("\nPress Enter...");
        InputUtil.getScanner().nextLine();
    }

    private void deleteAnswer() {
        int aId = InputUtil.readInt("Enter Answer ID to delete: ");
        try {
            String sql = "DELETE FROM Answers WHERE answer_id = ?";

            Connection conn = DBConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1,aId);
            int r = pst.executeUpdate();
            if (r > 0) {
                System.out.println("Answer deleted.");
                historyService.logEvent("Admin deleted answer #" + aId);
            } else {
                System.out.println("Answer not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ============================
    //  6. LEADERBOARD
    // ============================

    private void viewLeaderboard() {
        System.out.println("\n========== LEADERBOARD - TOP STUDENTS ==========\n");

        try {
            String sql = "{call TopStudents()}";

            ArrayList<Student> students = new ArrayList<>();

            Connection conn = DBConnection.getConnection();
            CallableStatement cst = conn.prepareCall(sql);
            ResultSet rs = cst.executeQuery();

            while (rs.next()) {
                students.add(new Student(
                        rs.getInt("student_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("branch"),
                        rs.getString("phone"),
                        rs.getDouble("average_rating"),
                        rs.getInt("total_sessions")
                ));
            }

            if (students.isEmpty()) {
                System.out.println("No data yet.");
                return;
            }

            System.out.printf("%-6s %-20s %-25s %-8s %-8s%n",
                    "Rank", "Name", "Branch", "Rating", "Sessions");

            System.out.println("-----------------------------------------------------------------------");

            int rank = 1;

            for (Student s : students) {
                System.out.printf("%-6d %-20s %-25s %-8.2f %-8d%n",
                        rank++,
                        s.getName(),
                        s.getBranch(),
                        s.getAverageRating(),
                        s.getTotalSessions());
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.print("\nPress Enter...");
        InputUtil.getScanner().nextLine();
    }

    // ============================
    //  7. HISTORY LOGS
    // ============================

    private void viewHistoryLogs() {
        boolean inMenu = true;

        while (inMenu) {
            System.out.println("\n  ---- HISTORY LOGS ----");
            System.out.println("1. View All Logs (newest first)");
            System.out.println("2. Peek Latest Event");
            System.out.println("3. Export Logs to File");
            System.out.println("4. Back");

            int choice = InputUtil.readInt("Choice : ");

            switch (choice) {
                case 1: displayAllLogs();       break;
                case 2: peekLatestLog();        break;
                case 3: exportLogsToFile();     break;
                case 4: inMenu = false;         break;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    /** Displays all stack events (newest first - LIFO order). */
    private void displayAllLogs() {
        System.out.println("\n  ---- ALL HISTORY EVENTS (Newest First) ----");
        ArrayList<String> events = historyService.getAllEvents();

        if (events.isEmpty()) {
            System.out.println("No events recorded yet.");
        } else {
            int i = 1;
            for (String event : events) {
                System.out.println("  " + i + ". " + event);
                i++;
            }
            System.out.println("\nTotal Events: " + events.size());
        }
        System.out.print("\nPress Enter...");
        InputUtil.getScanner().nextLine();
    }

    /** Shows the most recent event using peek(). */
    private void peekLatestLog() {
        String latest = historyService.peekLatest();
        if (latest == null) {
            System.out.println("Stack is empty. No events yet.");
        } else {
            System.out.println("Latest event: " + latest);
        }
        System.out.print("\nPress Enter...");
        InputUtil.getScanner().nextLine();
    }

    /** Exports logs to file - demonstrates both Stack and IO. */
    private void exportLogsToFile() {
        try {
            String path = historyService.exportToFile();
            System.out.println("Logs exported to: " + path);
        } catch (IOException e) {
            System.out.println("Error writing file: " + e.getMessage());
        }
        System.out.print("\nPress Enter...");
        InputUtil.getScanner().nextLine();
    }
}