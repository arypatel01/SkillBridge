import Database.DBConnection;
import Util.InputUtil;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

class LearningModule{

    private HistoryService historyService;

    void acceptLearningRequest() throws SQLException {

        Connection connection = DBConnection.getConnection();

        viewLearningRequests();

        System.out.println("\n========== ACCEPT LEARNING REQUEST ==========");

        int requestId;
        int learnerId;
        String skillNeeded;

        while (true) {

            requestId = InputUtil.readInt("Enter Request ID : ");

            PreparedStatement checkRequest = connection.prepareStatement("""
                SELECT
                    student_id,
                    skill_needed,
                    status
                FROM LearningRequests
                WHERE request_id = ?
                """);

            checkRequest.setInt(1, requestId);

            ResultSet rs = checkRequest.executeQuery();

            if (!rs.next()) {

                System.out.println("Request ID does not exist.");

                rs.close();
                checkRequest.close();
                return;
            }

            learnerId = rs.getInt("student_id");
            skillNeeded = rs.getString("skill_needed");
            String status = rs.getString("status");

            rs.close();
            checkRequest.close();

            if (learnerId == Menu.loggedInStudent.getStudentId()) {

                System.out.println("You cannot accept your own learning request.");
                return;
            }

            if (!status.equalsIgnoreCase("Pending")) {

                System.out.println("This request is no longer available.");
                return;
            }

            break;
        }

        Date meetingDate;

        while (true) {

            System.out.print("Enter Meeting Date (YYYY-MM-DD): ");

            try {

                String input = InputUtil.readString("");

                LocalDate enteredDate = LocalDate.parse(input);
                LocalDate today = LocalDate.now();

                if (enteredDate.isAfter(today)) {
                    meetingDate = Date.valueOf(enteredDate);
                    break;
                } else {
                    System.out.println("Date must be in the future.");
                }

            } catch (DateTimeParseException e) {
                System.out.println("Invalid Date Format.");
            }
        }

        Time meetingTime;

        while (true) {

            System.out.print("Enter Meeting Time (HH:MM): ");

            try {

                String input = InputUtil.readString("");

                // Convert HH:MM to HH:MM:SS
                meetingTime = Time.valueOf(input + ":00");

                break;

            } catch (IllegalArgumentException e) {

                System.out.println("Invalid Time Format. Please enter time in HH:MM format.");
            }
        }

        String location;

        while (true) {

            location = InputUtil.readString("Enter Location : ");

            if (location.length() >= 3)
                break;

            System.out.println("Location must contain at least 3 characters.");
        }

        connection.setAutoCommit(false);

        try {

            CallableStatement insertSession = connection.prepareCall("{call AcceptLearningRequest(?,?,?,?,?,?,?)}");

            insertSession.setInt(1, requestId);
            insertSession.setInt(2, Menu.loggedInStudent.getStudentId());
            insertSession.setInt(3, learnerId);
            insertSession.setDate(4, meetingDate);
            insertSession.setTime(5, meetingTime);
            insertSession.setString(6, location);
            insertSession.setString(7, skillNeeded);

            insertSession.executeUpdate();

            insertSession.close();

            historyService.logEvent("Student '" + Menu.loggedInStudent.getName()
                    + "' accepted request #" + requestId
                    + " for skill: " + skillNeeded);

            connection.commit();

            System.out.println("\n========================================");
            System.out.println("Learning Request Accepted Successfully.");
            System.out.println("Waiting for learner confirmation.");
            System.out.println("========================================");

        }
        catch (SQLException e) {

            connection.rollback();

            System.out.println("\nTransaction Rolled Back.");

            throw e;

        }
        finally {

            connection.setAutoCommit(true);
            connection.close();
        }
    }

    void learningRequestMenu(HistoryService historyService) throws SQLException {

        this.historyService = historyService;

        while (true) {

            System.out.println("\n==================================================");
            System.out.println("              LEARNING REQUEST MODULE");
            System.out.println("==================================================");
            System.out.println("1. Create Learning Request");
            System.out.println("2. View All Learning Requests");
            System.out.println("3. My Learning Requests");
            System.out.println("4. Accept Learning Request");
            System.out.println("5. Pending Confirmations");
            System.out.println("6. My Sessions");
            System.out.println("7. Complete Session");
            System.out.println("8. Rate Teacher");
            System.out.println("9. Delete My Learning Request");
            System.out.println("10. Back");
            System.out.println("==================================================");

            int choice = InputUtil.readInt("Enter Choice : ");

            switch (choice) {

                case 1 -> createLearningRequest();

                case 2 -> viewLearningRequests();

                case 3 -> myLearningRequests();

                case 4 -> acceptLearningRequest();

                case 5 -> pendingConfirmations();

                case 6 -> mySessions();

                case 7 -> completeSession();

                case 8 -> rateTeacher();

                case 9 -> deleteMyLearningRequest();

                case 10 -> {
                    return;
                }

                default -> System.out.println("Invalid Choice.");
            }
        }
    }

    void myLearningRequests() throws SQLException {

        Connection connection = DBConnection.getConnection();

        String query = """
            SELECT
                request_id,
                skill_needed,
                description,
                status,
                created_at
            FROM LearningRequests
            WHERE student_id = ?
            ORDER BY created_at DESC
            """;

        PreparedStatement ps = connection.prepareStatement(query);

        ps.setInt(1, Menu.loggedInStudent.getStudentId());

        ResultSet rs = ps.executeQuery();

        boolean found = false;

        while (rs.next()) {

            found = true;

            System.out.println("\n====================================================");
            System.out.println("Request ID   : " + rs.getInt("request_id"));
            System.out.println("Skill Needed : " + rs.getString("skill_needed"));
            System.out.println("Description  : " + rs.getString("description"));
            System.out.println("Status       : " + rs.getString("status"));
            System.out.println("Created On   : " + rs.getTimestamp("created_at"));
            System.out.println("====================================================");
        }

        if (!found) {
            System.out.println("\nYou have not created any learning requests.");
        }

        rs.close();
        ps.close();
        connection.close();
    }

    void createLearningRequest() throws SQLException {

        Connection connection = DBConnection.getConnection();

        connection.setAutoCommit(false);

        try {

            String skillNeeded;

            while (true) {

                skillNeeded = InputUtil.readString("Enter Skill You Want To Learn : ");

                if (skillNeeded.length() >= 3) {
                    break;
                }

                System.out.println("Skill name must contain at least 3 characters.");
            }

            String description;

            while (true) {

                description = InputUtil.readString("Enter Description : ");

                if (description.length() >= 10) {
                    break;
                }

                System.out.println("Description must contain at least 10 characters.");
            }

            PreparedStatement ps = connection.prepareStatement("""
                INSERT INTO LearningRequests
                (
                    student_id,
                    skill_needed,
                    description,
                    status
                )
                VALUES(?,?,?,'Pending')
                """);

            ps.setInt(1, Menu.loggedInStudent.getStudentId());
            ps.setString(2, skillNeeded);
            ps.setString(3, description);

            int rows = ps.executeUpdate();

            ps.close();

            if (rows == 0) {

                connection.rollback();

                System.out.println("Failed to create learning request.");

                connection.close();
                return;
            }

            connection.commit();

            historyService.logEvent("Student '" + Menu.loggedInStudent.getName()
                    + "' created a request for skill: " + skillNeeded);

            System.out.println("\n======================================");
            System.out.println("Learning Request Created Successfully.");
            System.out.println("======================================");

        }
        catch (SQLException e) {

            connection.rollback();

            System.out.println("Transaction Rolled Back.");

            throw e;
        }
        finally {

            connection.setAutoCommit(true);
            connection.close();
        }
    }

    void viewLearningRequests() throws SQLException {

        Connection connection = DBConnection.getConnection();

        String query = """
            SELECT
                lr.request_id,
                s.name,
                s.branch,
                lr.skill_needed,
                lr.description,
                lr.created_at
            FROM LearningRequests lr
            JOIN Students s
            ON lr.student_id = s.student_id
            WHERE lr.status='Pending'
            AND lr.student_id<>?
            ORDER BY lr.created_at DESC
            """;

        PreparedStatement ps = connection.prepareStatement(query);

        ps.setInt(1, Menu.loggedInStudent.getStudentId());

        ResultSet rs = ps.executeQuery();

        boolean found = false;

        while (rs.next()) {

            found = true;

            System.out.println("\n====================================================");
            System.out.println("Request ID   : " + rs.getInt("request_id"));
            System.out.println("Student Name : " + rs.getString("name"));
            System.out.println("Branch       : " + rs.getString("branch"));
            System.out.println("Skill Needed : " + rs.getString("skill_needed"));
            System.out.println("Description  : " + rs.getString("description"));
            System.out.println("Posted On    : " + rs.getTimestamp("created_at"));
            System.out.println("====================================================");
        }

        if (!found) {
            System.out.println("\nNo Learning Requests Available.");
        }

        rs.close();
        ps.close();
        connection.close();
    }

    void pendingConfirmations() throws SQLException {

        Connection connection = DBConnection.getConnection();

        String query = """
            SELECT
                ls.session_id,
                lr.request_id,
                lr.skill_needed,
                s.name AS teacher_name,
                ls.teacher_id,
                ls.meeting_date,
                ls.meeting_time,
                ls.location
            FROM LearningSessions ls
            JOIN LearningRequests lr
                ON ls.request_id = lr.request_id
            JOIN Students s
                ON ls.teacher_id = s.student_id
            WHERE ls.learner_id = ?
            AND ls.status='Pending Confirmation'
            ORDER BY ls.created_at DESC
            """;

        PreparedStatement ps = connection.prepareStatement(query);

        ps.setInt(1, Menu.loggedInStudent.getStudentId());

        ResultSet rs = ps.executeQuery();

        boolean found = false;

        while (rs.next()) {

            found = true;

            System.out.println("\n================================================");
            System.out.println("Session ID : " + rs.getInt("session_id"));
            System.out.println("Request ID : " + rs.getInt("request_id"));
            System.out.println("Teacher    : " + rs.getString("teacher_name"));
            System.out.println("Skill      : " + rs.getString("skill_needed"));
            System.out.println("Date       : " + rs.getDate("meeting_date"));
            System.out.println("Time       : " + rs.getTime("meeting_time"));
            System.out.println("Location   : " + rs.getString("location"));
            System.out.println("================================================");
        }

        if (!found) {

            System.out.println("\nNo Pending Confirmations.");

            rs.close();
            ps.close();
            connection.close();
            return;
        }

        rs.close();
        ps.close();

        int sessionId;
        int teacherId = 0;
        int requestId = 0;

        String teacherName = "";
        String skillNeeded = "";

        while (true) {

            sessionId = InputUtil.readInt("\nEnter Session ID : ");

            PreparedStatement check = connection.prepareStatement("""
                SELECT
                    ls.teacher_id,
                    ls.request_id,
                    lr.skill_needed,
                    s.name
                FROM LearningSessions ls
                JOIN LearningRequests lr
                    ON ls.request_id = lr.request_id
                JOIN Students s
                    ON ls.teacher_id = s.student_id
                WHERE ls.session_id=?
                AND ls.learner_id=?
                AND ls.status='Pending Confirmation'
                """);

            check.setInt(1, sessionId);
            check.setInt(2, Menu.loggedInStudent.getStudentId());

            ResultSet checkRs = check.executeQuery();

            if (checkRs.next()) {

                teacherId = checkRs.getInt("teacher_id");
                requestId = checkRs.getInt("request_id");
                teacherName = checkRs.getString("name");
                skillNeeded = checkRs.getString("skill_needed");

                checkRs.close();
                check.close();

                break;
            }

            checkRs.close();
            check.close();

            System.out.println("Invalid Session ID.");
        }

        int choice;

        while (true) {

            System.out.println("\n1. Confirm");
            System.out.println("2. Cancel");

            choice = InputUtil.readInt("Enter Choice : ");

            if (choice == 1 || choice == 2)
                break;

            System.out.println("Invalid Choice.");
        }

        connection.setAutoCommit(false);

        try {        if (choice == 1) {

            // =====================================
            // Update Learning Session
            // =====================================

            PreparedStatement confirmSession = connection.prepareStatement("""
                    UPDATE LearningSessions
                    SET status='Scheduled'
                    WHERE session_id=?
                    """);

            confirmSession.setInt(1, sessionId);
            confirmSession.executeUpdate();
            confirmSession.close();


            // =====================================
            // Update Response Status
            // =====================================

            PreparedStatement confirmResponse = connection.prepareStatement("""
                    UPDATE Responses
                    SET status='Confirmed'
                    WHERE request_id=?
                    """);

            confirmResponse.setInt(1, requestId);
            confirmResponse.executeUpdate();
            confirmResponse.close();

            historyService.logEvent("Student '" + Menu.loggedInStudent.getName()
                    + "' confirmed session #" + sessionId + " with "
                    + teacherName + " for skill "
                    + skillNeeded + ".");

            connection.commit();

            System.out.println("\n======================================");
            System.out.println("Learning Session Confirmed Successfully.");
            System.out.println("======================================");

        }
        else {

            // =====================================
            // Cancel Session
            // =====================================

            PreparedStatement cancelSession = connection.prepareStatement("""
                    UPDATE LearningSessions
                    SET status='Cancelled'
                    WHERE session_id=?
                    """);

            cancelSession.setInt(1, sessionId);
            cancelSession.executeUpdate();
            cancelSession.close();


            // =====================================
            // Make Request Available Again
            // =====================================

            PreparedStatement request = connection.prepareStatement("""
                    UPDATE LearningRequests
                    SET status='Pending'
                    WHERE request_id=?
                    """);

            request.setInt(1, requestId);
            request.executeUpdate();
            request.close();


            // =====================================
            // Update Response Status
            // =====================================

            PreparedStatement cancelResponse = connection.prepareStatement("""
                    UPDATE Responses
                    SET status='Cancelled'
                    WHERE request_id=?
                    """);

            cancelResponse.setInt(1, requestId);
            cancelResponse.executeUpdate();
            cancelResponse.close();

            historyService.logEvent("Student '" + Menu.loggedInStudent.getName()
                    + " rejected the proposed schedule of "
                    + teacherName + " for Learning Request ID "
                    + requestId
                    + ". The request is available again.");

            connection.commit();

            System.out.println("\n======================================");
            System.out.println("Schedule Cancelled Successfully.");
            System.out.println("Learning Request is available again.");
            System.out.println("======================================");
        }

        }
        catch (SQLException e) {

            connection.rollback();

            System.out.println("\nTransaction Rolled Back.");

            throw e;

        }
        finally {

            connection.setAutoCommit(true);
            connection.close();
        }
    }

    void mySessions() throws SQLException {

        Connection connection = DBConnection.getConnection();

        String query = """
            SELECT
                ls.session_id,
                ls.teacher_id,
                ls.learner_id,
                lr.skill_needed,
                teacher.name AS teacher_name,
                learner.name AS learner_name,
                ls.meeting_date,
                ls.meeting_time,
                ls.location,
                ls.status
            FROM LearningSessions ls
            JOIN LearningRequests lr
                ON ls.request_id = lr.request_id
            JOIN Students teacher
                ON ls.teacher_id = teacher.student_id
            JOIN Students learner
                ON ls.learner_id = learner.student_id
            WHERE ls.teacher_id = ?
               OR ls.learner_id = ?
            ORDER BY ls.meeting_date DESC,
                     ls.meeting_time DESC
            """;

        PreparedStatement ps = connection.prepareStatement(query);

        ps.setInt(1, Menu.loggedInStudent.getStudentId());
        ps.setInt(2, Menu.loggedInStudent.getStudentId());

        ResultSet rs = ps.executeQuery();

        boolean found = false;

        while (rs.next()) {

            found = true;

            System.out.println("\n================================================");

            System.out.println("Session ID : " + rs.getInt("session_id"));

            if (rs.getInt("teacher_id") == Menu.loggedInStudent.getStudentId()) {

                System.out.println("Role       : Teacher");
                System.out.println("Learner    : " + rs.getString("learner_name"));

            } else {

                System.out.println("Role       : Learner");
                System.out.println("Teacher    : " + rs.getString("teacher_name"));

            }

            System.out.println("Skill      : " + rs.getString("skill_needed"));
            System.out.println("Date       : " + rs.getDate("meeting_date"));
            System.out.println("Time       : " + rs.getTime("meeting_time"));
            System.out.println("Location   : " + rs.getString("location"));
            System.out.println("Status     : " + rs.getString("status"));

            System.out.println("================================================");
        }

        if (!found) {
            System.out.println("\nNo Sessions Found.");
        }

        rs.close();
        ps.close();
        connection.close();
    }

    void completeSession() throws SQLException {

        Connection connection = DBConnection.getConnection();

        String query = """
            SELECT
                ls.session_id,
                ls.teacher_id,
                ls.learner_id,
                lr.request_id,
                lr.skill_needed,
                t.name AS teacher_name,
                l.name AS learner_name,
                ls.meeting_date,
                ls.meeting_time,
                ls.location
            FROM LearningSessions ls
            JOIN LearningRequests lr
                ON ls.request_id = lr.request_id
            JOIN Students t
                ON ls.teacher_id = t.student_id
            JOIN Students l
                ON ls.learner_id = l.student_id
                WHERE
                ls.teacher_id = ?
                AND ls.status='Scheduled'
            ORDER BY ls.meeting_date,ls.meeting_time
            """;

        PreparedStatement ps = connection.prepareStatement(query);

        ps.setInt(1, Menu.loggedInStudent.getStudentId());

        ResultSet rs = ps.executeQuery();

        boolean found = false;

        while (rs.next()) {

            found = true;

            System.out.println("\n========================================");

            System.out.println("Session ID : " + rs.getInt("session_id"));

            System.out.println("Learner    : " + rs.getString("learner_name"));

            System.out.println("Skill      : " + rs.getString("skill_needed"));
            System.out.println("Date       : " + rs.getDate("meeting_date"));
            System.out.println("Time       : " + rs.getTime("meeting_time"));
            System.out.println("Location   : " + rs.getString("location"));

            System.out.println("========================================");
        }

        if (!found) {

            System.out.println("\nNo Scheduled Sessions Found.");

            rs.close();
            ps.close();
            connection.close();
            return;
        }

        rs.close();
        ps.close();

        int sessionId;
        int teacherId = 0;
        int requestId = 0;

        String teacherName = "";
        String skillNeeded = "";

        while (true) {

            sessionId = InputUtil.readInt("\nEnter Session ID : ");

            PreparedStatement check = connection.prepareStatement("""
                SELECT
                    ls.teacher_id,
                    lr.request_id,
                    lr.skill_needed,
                    t.name AS teacher_name
                FROM LearningSessions ls
                JOIN LearningRequests lr
                    ON ls.request_id = lr.request_id
                JOIN Students t
                    ON ls.teacher_id = t.student_id
                    WHERE
                    ls.session_id=?
                    AND ls.teacher_id=?
                    AND ls.status='Scheduled'
                """);

            check.setInt(1, sessionId);
            check.setInt(2, Menu.loggedInStudent.getStudentId());

            ResultSet checkRs = check.executeQuery();

            if (checkRs.next()) {

                teacherId = checkRs.getInt("teacher_id");
                requestId = checkRs.getInt("request_id");
                teacherName = checkRs.getString("teacher_name");
                skillNeeded = checkRs.getString("skill_needed");

                checkRs.close();
                check.close();

                break;
            }

            checkRs.close();
            check.close();

            System.out.println("Invalid Session ID.");
        }

        connection.setAutoCommit(false);

        try {        // ===================================
            // Mark Session as Completed
            // ===================================

            CallableStatement completeSession = connection.prepareCall("{call CompleteSession(?)}");

            completeSession.setInt(1, sessionId);

            completeSession.executeUpdate();
            completeSession.close();


            // ===================================
            // Increase Teacher Total Sessions
            // ===================================

            PreparedStatement totalSession = connection.prepareStatement("""
                UPDATE Students
                SET total_sessions = total_sessions + 1
                WHERE student_id=?
                """);

            totalSession.setInt(1, teacherId);

            totalSession.executeUpdate();
            totalSession.close();


            // ===================================
            // Update Response Status
            // ===================================

            PreparedStatement response = connection.prepareStatement("""
                UPDATE Responses
                SET status='Completed'
                WHERE request_id=?
                """);

            response.setInt(1, requestId);

            response.executeUpdate();
            response.close();

            historyService.logEvent("Session " + sessionId + " for skill "
                    + skillNeeded
                    + " completed successfully. Teacher : "
                    + teacherName + ". Marked as completed by "
                    + Menu.loggedInStudent.getName());

            // ===================================
            // Commit
            // ===================================

            connection.commit();

            System.out.println("\n======================================");
            System.out.println("Session Marked As Completed.");
            System.out.println("Learner can now rate this session.");
            System.out.println("======================================");

        }
        catch (SQLException e) {

            connection.rollback();

            System.out.println("Transaction Rolled Back.");

            throw e;
        }
        finally {

            connection.setAutoCommit(true);
            connection.close();
        }
    }

    void rateTeacher() throws SQLException {

        Connection connection = DBConnection.getConnection();

        String query = """
            SELECT
                ls.session_id,
                ls.teacher_id,
                t.name AS teacher_name,
                lr.skill_needed,
                ls.meeting_date,
                ls.meeting_time,
                ls.location
            FROM LearningSessions ls
            JOIN LearningRequests lr
                ON ls.request_id = lr.request_id
            JOIN Students t
                ON ls.teacher_id = t.student_id
            WHERE
                ls.learner_id = ?
            AND ls.status = 'Completed'
            AND ls.session_id NOT IN
            (
                SELECT session_id
                FROM Ratings
            )
            ORDER BY ls.meeting_date DESC,
                     ls.meeting_time DESC
            """;

        PreparedStatement ps = connection.prepareStatement(query);

        ps.setInt(1, Menu.loggedInStudent.getStudentId());

        ResultSet rs = ps.executeQuery();

        boolean found = false;

        while (rs.next()) {

            found = true;

            System.out.println("\n========================================");
            System.out.println("Session ID : " + rs.getInt("session_id"));
            System.out.println("Teacher    : " + rs.getString("teacher_name"));
            System.out.println("Skill      : " + rs.getString("skill_needed"));
            System.out.println("Date       : " + rs.getDate("meeting_date"));
            System.out.println("Time       : " + rs.getTime("meeting_time"));
            System.out.println("Location   : " + rs.getString("location"));
            System.out.println("========================================");
        }

        if (!found) {

            System.out.println("\nNo Sessions Available For Rating.");

            rs.close();
            ps.close();
            connection.close();
            return;
        }

        rs.close();
        ps.close();

        int sessionId;
        int teacherId = 0;
        String teacherName = "";
        String skillNeeded = "";

        while (true) {

            sessionId = InputUtil.readInt("\nEnter Session ID : ");

            PreparedStatement check = connection.prepareStatement("""
                SELECT
                    ls.teacher_id,
                    t.name,
                    lr.skill_needed
                FROM LearningSessions ls
                JOIN Students t
                    ON ls.teacher_id = t.student_id
                JOIN LearningRequests lr
                    ON ls.request_id = lr.request_id
                WHERE
                    ls.session_id = ?
                AND ls.learner_id = ?
                AND ls.status = 'Completed'
                AND ls.session_id NOT IN
                (
                    SELECT session_id
                    FROM Ratings
                )
                """);

            check.setInt(1, sessionId);
            check.setInt(2, Menu.loggedInStudent.getStudentId());

            ResultSet checkRs = check.executeQuery();

            if (checkRs.next()) {

                teacherId = checkRs.getInt("teacher_id");
                teacherName = checkRs.getString("name");
                skillNeeded = checkRs.getString("skill_needed");

                checkRs.close();
                check.close();

                break;
            }

            checkRs.close();
            check.close();

            System.out.println("Invalid Session ID.");
        }

        int score;

        while (true) {

            score = InputUtil.readInt("Enter Rating (1-5) : ");

            if (score >= 1 && score <= 5)
                break;

            System.out.println("Rating must be between 1 and 5.");
        }

        String comment = InputUtil.readString("Enter Comment : ");

        connection.setAutoCommit(false);

        try {
            // ===================================
            // Insert Rating
            // ===================================

            try {

                PreparedStatement insertRating = connection.prepareStatement("""
                    INSERT INTO Ratings
                    (
                        session_id,
                        rated_student_id,
                        rated_by_student_id,
                        score,
                        comment
                    )
                    VALUES(?,?,?,?,?)
                    """);

                insertRating.setInt(1, sessionId);
                insertRating.setInt(2, teacherId);
                insertRating.setInt(3, Menu.loggedInStudent.getStudentId());
                insertRating.setInt(4, score);
                insertRating.setString(5, comment);

                insertRating.executeUpdate();
                insertRating.close();

            }
            catch (SQLIntegrityConstraintViolationException e) {

                connection.rollback();

                System.out.println("You have already rated this session.");

                connection.setAutoCommit(true);
                connection.close();
                return;
            }


            // ===================================
            // Calculate Average Rating
            // ===================================

            double averageRating = 0;

            PreparedStatement avg = connection.prepareStatement("""
                SELECT AVG(score) AS avg_rating
                FROM Ratings
                WHERE rated_student_id=?
                """);

            avg.setInt(1, teacherId);

            ResultSet avgRs = avg.executeQuery();

            if (avgRs.next()) {
                averageRating = avgRs.getDouble("avg_rating");
            }

            avgRs.close();
            avg.close();


            // ===================================
            // Update Student Table
            // ===================================

            PreparedStatement updateStudent = connection.prepareStatement("""
                UPDATE Students
                SET average_rating=?
                WHERE student_id=?
                """);

            updateStudent.setDouble(1, averageRating);
            updateStudent.setInt(2, teacherId);

            updateStudent.executeUpdate();
            updateStudent.close();


            // ===================================
            // Update Responses
            // ===================================

            PreparedStatement response = connection.prepareStatement("""
                UPDATE Responses
                SET status='Rated'
                WHERE request_id=
                (
                    SELECT request_id
                    FROM LearningSessions
                    WHERE session_id=?
                )
                """);

            response.setInt(1, sessionId);

            response.executeUpdate();
            response.close();

            historyService.logEvent("Student " + Menu.loggedInStudent.getName() +
                    " rated "
                            + teacherName
                            + score
                            + "/5 for session \""
                            + skillNeeded
                            + "\".");

            // ===================================
            // Commit
            // ===================================

            connection.commit();

            System.out.println("\n========================================");
            System.out.println("Rating Submitted Successfully.");
            System.out.println("Teacher's Rating Updated.");
            System.out.println("========================================");

        }
        catch (SQLException e) {

            connection.rollback();

            System.out.println("Transaction Rolled Back.");

            throw e;

        }
        finally {

            connection.setAutoCommit(true);
            connection.close();
        }
    }

    void deleteMyLearningRequest() throws SQLException {

        Connection connection = DBConnection.getConnection();

        String query = """
            SELECT
                request_id,
                skill_needed,
                description,
                status,
                created_at
            FROM LearningRequests
            WHERE student_id=?
            AND status='Pending'
            ORDER BY created_at DESC
            """;

        PreparedStatement ps = connection.prepareStatement(query);

        ps.setInt(1, Menu.loggedInStudent.getStudentId());

        ResultSet rs = ps.executeQuery();

        boolean found = false;

        while (rs.next()) {

            found = true;

            System.out.println("\n========================================");
            System.out.println("Request ID  : " + rs.getInt("request_id"));
            System.out.println("Skill       : " + rs.getString("skill_needed"));
            System.out.println("Description : " + rs.getString("description"));
            System.out.println("Status      : " + rs.getString("status"));
            System.out.println("Created At  : " + rs.getTimestamp("created_at"));
            System.out.println("========================================");
        }

        rs.close();
        ps.close();

        if (!found) {

            System.out.println("\nNo Pending Learning Requests Found.");

            connection.close();
            return;
        }

        int requestId;
        String skillNeeded = "";

        while (true) {

            requestId = InputUtil.readInt("\nEnter Request ID : ");

            PreparedStatement check = connection.prepareStatement("""
                SELECT skill_needed
                FROM LearningRequests
                WHERE request_id=?
                AND student_id=?
                AND status='Pending'
                """);

            check.setInt(1, requestId);
            check.setInt(2, Menu.loggedInStudent.getStudentId());

            ResultSet checkRs = check.executeQuery();

            if (checkRs.next()) {

                skillNeeded = checkRs.getString("skill_needed");

                checkRs.close();
                check.close();

                break;
            }

            checkRs.close();
            check.close();

            System.out.println("Invalid Request ID.");
        }

        connection.setAutoCommit(false);

        try {

            PreparedStatement delete = connection.prepareStatement("""
                DELETE FROM LearningRequests
                WHERE request_id=?
                """);

            delete.setInt(1, requestId);

            delete.executeUpdate();

            delete.close();

            historyService.logEvent("Student " + Menu.loggedInStudent.getName() +" deleted Learning Request ID "
                    + requestId
                    + " for skill \""
                    + skillNeeded
                    + "\".");

            connection.commit();

            System.out.println("\n========================================");
            System.out.println("Learning Request Deleted Successfully.");
            System.out.println("========================================");

        }
        catch (SQLException e) {

            connection.rollback();

            System.out.println("Transaction Rolled Back.");

            throw e;
        }
        finally {

            connection.setAutoCommit(true);
            connection.close();
        }
    }
}