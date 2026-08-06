import Database.DBConnection;
import Util.InputUtil;

public class Main{
    public static void main(String[] args) throws Exception {
        HistoryService historyService = new HistoryService();
        historyService.loadHistoryFromDatabase();
        Menu m = new Menu(historyService);
        m.start();

        DBConnection.closeConnection();
        System.out.println("SkillBridge closed. Goodbye!");
    }
}