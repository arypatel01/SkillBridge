package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection{
    private static final String URL      = "jdbc:mysql://localhost:3306/skillbridge";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    private static Connection connection = null;

    // Private constructor - no one should instantiate this class
    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        connection = DriverManager.getConnection(URL, USER, PASSWORD);
        return connection;
    }

    public static void closeConnection() {
        try {
            connection.close();
            System.out.println("Database connection closed.");

        } catch (SQLException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }
}