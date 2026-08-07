import Database.DBConnection;
import Util.InputUtil;

import java.sql.*;
import java.util.Scanner;

class QuestionAnswerForum{

    private HistoryService historyService;

    void QuestionAnswerForum(HistoryService historyService) throws SQLException {

        this.historyService = historyService;

        while (true) {

            System.out.println("\n\n====================== Q&A FORUM ======================");
            System.out.println("1. Ask Question");
            System.out.println("2. View All Questions");
            System.out.println("3. Answer a Question");
            System.out.println("4. View Answers");
            System.out.println("5. My Questions");
            System.out.println("6. Delete My Question");
            System.out.println("7. Back");
            System.out.println("=======================================================");

            int choice = InputUtil.readInt("Enter Choice : ");
//            InputUtil.getScanner().nextLine();

            switch (choice) {

                case 1 -> {
                    askQuestion();
                }

                case 2 -> {
                    viewAllQuestions();
                }

                case 3 -> {
                    answerQuestion();
                }

                case 4 -> {
                    viewAnswers();
                }

                case 5 -> {
                    myQuestions();
                }

                case 6 -> {
                    deleteMyQuestion();
                }

                case 7 -> {
                    return;
                }

                default -> {
                    System.out.println("Invalid Choice.");
                }
            }
        }
    }

    void askQuestion() throws SQLException {

        Connection connection = DBConnection.getConnection();

        String title;

        while (true) {

            title = InputUtil.readString("Enter Question Title : ");

            if (title.length() < 5) {
                System.out.println("Title must contain at least 5 characters.");
                continue;
            }

            if (title.length() > 200) {
                System.out.println("Title is too long.");
                continue;
            }

            break;
        }

        String body;

        while (true) {

            body = InputUtil.readString("Enter Question Description : ");

            if (body.length() < 10) {
                System.out.println("Description must contain at least 10 characters.");
                continue;
            }

            break;
        }

        String query = """
            INSERT INTO Questions
            (asked_by_student_id,title,body)
            VALUES(?,?,?)
            """;

        PreparedStatement ps = connection.prepareStatement(query);

        ps.setInt(1, Menu.loggedInStudent.getStudentId());
        ps.setString(2, title);
        ps.setString(3, body);

        int rows = ps.executeUpdate();

        if (rows > 0) {
            System.out.println("\nQuestion Posted Successfully.");
            historyService.logEvent("Student '" + Menu.loggedInStudent.getName() + "' asked a question: " + title);
        }
        else {
            System.out.println("\nFailed To Post Question.");
        }

        ps.close();
        connection.close();
    }

    void viewAllQuestions() throws SQLException {

        Connection connection = DBConnection.getConnection();

        String query = """
            SELECT
                q.question_id,
                q.title,
                q.body,
                s.name,
                q.created_at
            FROM Questions q
            JOIN Students s
            ON q.asked_by_student_id = s.student_id
            ORDER BY q.created_at DESC
            """;

        PreparedStatement ps = connection.prepareStatement(query);

        ResultSet rs = ps.executeQuery();

        boolean found = false;

        System.out.println("\n==================== ALL QUESTIONS ====================");

        while (rs.next()) {

            found = true;

            System.out.println("\n------------------------------------------------------");
            System.out.println("Question ID : " + rs.getInt("question_id"));
            System.out.println("Title       : " + rs.getString("title"));
            System.out.println("Description : " + rs.getString("body"));
            System.out.println("Asked By    : " + rs.getString("name"));
            System.out.println("Asked On    : " + rs.getTimestamp("created_at"));
            System.out.println("------------------------------------------------------");
        }

        if (!found) {
            System.out.println("\nNo Questions Available.");
        }

        rs.close();
        ps.close();
        connection.close();
    }

    void answerQuestion() throws SQLException {

        Connection connection = DBConnection.getConnection();

        connection.setAutoCommit(false);

        viewAllQuestions();
        System.out.println("\n================ Answer Question ================");

        int questionId;
        int questionOwnerId = 0;
        int answerId = 0;
        String questionTitle = "";

        while (true) {

            System.out.print("Enter Question ID : ");
            questionId = InputUtil.readInt("");
//            InputUtil.getScanner().nextLine();

            PreparedStatement checkQuestion = connection.prepareStatement("""
                    SELECT asked_by_student_id,title
                    FROM Questions
                    WHERE question_id = ?
                    """);

            checkQuestion.setInt(1, questionId);

            ResultSet rs = checkQuestion.executeQuery();

            if (rs.next()) {

                questionOwnerId = rs.getInt("asked_by_student_id");
                questionTitle = rs.getString("title");

                rs.close();
                checkQuestion.close();
                break;
            }

            rs.close();
            checkQuestion.close();

            System.out.println("Question ID does not exist.");
        }

        if (questionOwnerId == Menu.loggedInStudent.getStudentId()) {

            System.out.println("You cannot answer your own question.");

            connection.close();
            return;
        }

        String answer;

        while (true) {

            answer = InputUtil.readString("Enter Your Answer : ");

            if (answer.length() < 10) {

                System.out.println("Answer must contain at least 10 characters.");
                continue;
            }

            break;
        }

        PreparedStatement insertAnswer = connection.prepareStatement("""
                INSERT INTO Answers
                (question_id,answered_by_student_id,answer_text)
                VALUES(?,?,?)
                """, Statement.RETURN_GENERATED_KEYS);

        insertAnswer.setInt(1, questionId);
        insertAnswer.setInt(2, Menu.loggedInStudent.getStudentId());
        insertAnswer.setString(3, answer);

        int rows = insertAnswer.executeUpdate();

        if (rows == 0) {

            connection.rollback();

            System.out.println("Failed To Post Answer.");

            insertAnswer.close();
            connection.close();
            return;
        }

        ResultSet generatedKeys = insertAnswer.getGeneratedKeys();

        if (generatedKeys.next()) {
            answerId = generatedKeys.getInt(1);
        }

        generatedKeys.close();
        insertAnswer.close();
        try {

            // ==========================
            // Insert into AnswerResponses
            // ==========================

            PreparedStatement response = connection.prepareStatement("""
                INSERT INTO AnswerResponses
                (
                    question_id,
                    answer_id,
                    question_owner_id,
                    responder_id,
                    message
                )
                VALUES(?,?,?,?,?)
                """);

            response.setInt(1, questionId);
            response.setInt(2, answerId);
            response.setInt(3, questionOwnerId);
            response.setInt(4, Menu.loggedInStudent.getStudentId());

            response.setString(5,
                    Menu.loggedInStudent.getName()
                            + " answered your question \""
                            + questionTitle + "\".");

            response.executeUpdate();

            response.close();


            // ==========================
            // Insert into History
            // ==========================

            PreparedStatement history = connection.prepareStatement("""
                INSERT INTO request_history(event_description)
                VALUES(?)
                """);

            history.setString(1,
                    Menu.loggedInStudent.getName()
                            + " (Student ID : "
                            + Menu.loggedInStudent.getStudentId()
                            + ") answered Question ID "
                            + questionId
                            + " : "
                            + questionTitle);

            history.executeUpdate();

            history.close();


            // ==========================
            // Commit Transaction
            // ==========================

            connection.commit();
            historyService.logEvent("Student '" + Menu.loggedInStudent.getName() + "' answered question #" + questionId);
            System.out.println("\n======================================");
            System.out.println("Answer Posted Successfully.");
            System.out.println("======================================");

        }
        catch (SQLException e) {

            connection.rollback();

            System.out.println("\nSomething went wrong.");
            System.out.println("Transaction Rolled Back.");

            throw e;
        }
        finally {

            connection.setAutoCommit(true);
            connection.close();
        }
    }

    void viewAnswers() throws SQLException {

        Connection connection = DBConnection.getConnection();

        String query = """
            SELECT
                q.question_id,
                q.title,
                q.body,
                q.created_at AS question_time,

                qs.name AS question_owner,

                a.answer_id,
                a.answer_text,
                a.created_at AS answer_time,

                ans.name AS answered_by

            FROM Questions q

            JOIN Students qs
            ON q.asked_by_student_id = qs.student_id

            LEFT JOIN Answers a
            ON q.question_id = a.question_id

            LEFT JOIN Students ans
            ON a.answered_by_student_id = ans.student_id

            ORDER BY
                q.question_id,
                a.created_at;
            """;

        PreparedStatement ps = connection.prepareStatement(query);

        ResultSet rs = ps.executeQuery();

        int previousQuestionId = -1;
        boolean hasAnswer = false;
        int answerCount = 1;

        while (rs.next()) {

            int currentQuestionId = rs.getInt("question_id");

            if (currentQuestionId != previousQuestionId) {

                if (previousQuestionId != -1 && !hasAnswer) {
                    System.out.println("\nNo Answers Yet.");
                }

                hasAnswer = false;
                answerCount = 1;

                System.out.println("\n========================================================");
                System.out.println("Question ID : " + currentQuestionId);
                System.out.println("Title       : " + rs.getString("title"));
                System.out.println("Description : " + rs.getString("body"));
                System.out.println("Asked By    : " + rs.getString("question_owner"));
                System.out.println("Asked On    : " + rs.getTimestamp("question_time"));
                System.out.println("--------------------------------------------------------");
                System.out.println("Answers :\n");

                previousQuestionId = currentQuestionId;
            }

            if (rs.getObject("answer_id") != null) {

                hasAnswer = true;

                System.out.println(answerCount + ".");

                System.out.println("Answered By : "
                        + rs.getString("answered_by"));

                System.out.println("Answer      : "
                        + rs.getString("answer_text"));

                System.out.println("Answered On : "
                        + rs.getTimestamp("answer_time"));

                System.out.println("------------------------------------------");

                answerCount++;
            }
        }

        if (previousQuestionId == -1) {

            System.out.println("\nNo Questions Found.");

        } else if (!hasAnswer) {

            System.out.println("\nNo Answers Yet.");
        }

        rs.close();
        ps.close();
        connection.close();
    }

    boolean myQuestions() throws SQLException {

        Connection connection = DBConnection.getConnection();

        String query = """
        SELECT
            q.question_id,
            q.title,
            q.body,
            q.created_at AS question_time,

            a.answer_id,
            a.answer_text,
            a.created_at AS answer_time,

            s.name AS answered_by

        FROM Questions q

        LEFT JOIN Answers a
        ON q.question_id = a.question_id

        LEFT JOIN Students s
        ON a.answered_by_student_id = s.student_id

        WHERE q.asked_by_student_id = ?

        ORDER BY
            q.question_id,
            a.created_at;
        """;

        PreparedStatement ps = connection.prepareStatement(query);

        ps.setInt(1, Menu.loggedInStudent.getStudentId());

        ResultSet rs = ps.executeQuery();

        int previousQuestionId = -1;
        int answerCount = 1;
        boolean hasAnswer = false;
        boolean hasQuestion = false;

        while (rs.next()) {

            hasQuestion = true;

            int currentQuestionId = rs.getInt("question_id");

            if (currentQuestionId != previousQuestionId) {

                if (previousQuestionId != -1 && !hasAnswer) {
                    System.out.println("\nNo Answers Yet.");
                }

                hasAnswer = false;
                answerCount = 1;

                System.out.println("\n========================================================");
                System.out.println("Question ID : " + currentQuestionId);
                System.out.println("Title       : " + rs.getString("title"));
                System.out.println("Description : " + rs.getString("body"));
                System.out.println("Asked On    : " + rs.getTimestamp("question_time"));
                System.out.println("--------------------------------------------------------");
                System.out.println("Answers :\n");

                previousQuestionId = currentQuestionId;
            }

            if (rs.getObject("answer_id") != null) {

                hasAnswer = true;

                System.out.println(answerCount + ".");

                System.out.println("Answered By : " + rs.getString("answered_by"));
                System.out.println("Answer      : " + rs.getString("answer_text"));
                System.out.println("Answered On : " + rs.getTimestamp("answer_time"));

                System.out.println("------------------------------------------");

                answerCount++;
            }
        }

        if (!hasQuestion) {

            System.out.println("\nYou haven't asked any questions yet.");

        } else if (!hasAnswer) {

            System.out.println("\nNo Answers Yet.");
        }

        rs.close();
        ps.close();
        connection.close();

        return hasQuestion;
    }

    void deleteMyQuestion() throws SQLException {

        Connection connection = DBConnection.getConnection();

        connection.setAutoCommit(false);

        // If no questions exist, stop here.
        if (!myQuestions()) {
            connection.close();
            return;
        }

        System.out.println("\n============== DELETE MY QUESTION ==============\n");

        int questionId;
        String questionTitle = "";

        while (true) {

            System.out.print("Enter Question ID : ");

            questionId = InputUtil.readInt("");

            PreparedStatement check = connection.prepareStatement("""
            SELECT title
            FROM Questions
            WHERE question_id = ?
            AND asked_by_student_id = ?
            """);

            check.setInt(1, questionId);
            check.setInt(2, Menu.loggedInStudent.getStudentId());

            ResultSet rs = check.executeQuery();

            if (rs.next()) {

                questionTitle = rs.getString("title");

                rs.close();
                check.close();
                break;
            }

            rs.close();
            check.close();

            System.out.println("Invalid Question ID.");
        }

        while (true) {

            String choice = InputUtil.readString(
                    "Are you sure you want to delete this question? (Y/N) : ");

            if (choice.equalsIgnoreCase("Y")) {
                break;
            }

            if (choice.equalsIgnoreCase("N")) {

                System.out.println("Question deletion cancelled.");

                connection.rollback();
                connection.close();
                return;
            }

            System.out.println("Please enter Y or N.");
        }

        try {

            PreparedStatement deleteQuestion = connection.prepareStatement("""
            DELETE FROM Questions
            WHERE question_id = ?
            """);

            deleteQuestion.setInt(1, questionId);

            int rows = deleteQuestion.executeUpdate();

            deleteQuestion.close();

            if (rows == 0) {

                connection.rollback();

                System.out.println("Failed to delete question.");

                connection.close();
                return;
            }

            PreparedStatement history = connection.prepareStatement("""
            INSERT INTO request_history(event_description)
            VALUES(?)
            """);

            history.setString(1,
                    Menu.loggedInStudent.getName()
                            + " (Student ID : "
                            + Menu.loggedInStudent.getStudentId()
                            + ") deleted Question ID "
                            + questionId
                            + " : "
                            + questionTitle);

            history.executeUpdate();

            history.close();

            connection.commit();

            System.out.println("\n======================================");
            System.out.println("Question Deleted Successfully.");
            System.out.println("======================================");

        } catch (SQLException e) {

            connection.rollback();

            System.out.println("Something went wrong.");
            System.out.println("Transaction Rolled Back.");

            throw e;

        } finally {

            connection.setAutoCommit(true);

            if (!connection.isClosed()) {
                connection.close();
            }
        }
    }
}