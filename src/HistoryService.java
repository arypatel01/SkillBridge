import DS.Stack;
import Database.DBConnection;
import Util.DateUtil;
import IO.HistoryLogWriter;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryService {
    private final Stack historyStack;

    public HistoryService() {
        this.historyStack = new Stack();
    }

    public void logEvent(String event) {
        String timestampedEvent = "[" + DateUtil.getCurrentDateTime() + "] " + event;
        historyStack.push(timestampedEvent);

        // Also persist to database
        try {
            String sql = "INSERT INTO request_history (event_description) VALUES (?)";
            Connection con = DBConnection.getConnection();
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1,timestampedEvent);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("  Warning: Could not save log to database.");
        }
    }

    public List<String> getAllEvents() {
        return historyStack.displayAll();
    }

    public String peekLatest() {
        return historyStack.peek();
    }

    public String exportToFile() throws IOException {
        HistoryLogWriter writer = new HistoryLogWriter();
        return writer.writeHistoryLogs(historyStack);
    }

    public void loadHistoryFromDatabase() {
        try {
            String sql = "SELECT event_description, created_at FROM request_history ORDER BY created_at DESC";

            List<String> logs = new ArrayList<>();
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                logs.add("[" + rs.getString("created_at") + "] " + rs.getString("event_description"));
            }
            // Push them in reverse order so newest ends up on top
            for (int i = logs.size() - 1; i >= 0; i--) {
                historyStack.push(logs.get(i));
            }
        } catch (SQLException e) {
            System.out.println("  Warning: Could not load history from database.");
        }
    }
}
