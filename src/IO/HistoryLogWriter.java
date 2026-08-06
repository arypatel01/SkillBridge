package IO;

import DS.Stack;
import Util.DateUtil;

import java.io.*;
import java.util.*;

public class HistoryLogWriter {
    public String writeHistoryLogs(Stack stack) throws IOException {

        // Build path to workspace root (project directory)
        String workingDir = System.getProperty("user.dir");
        String filePath   = workingDir + File.separator + "HistoryLogs.txt";

        // FileWriter in append=false mode (overwrites file each time)
        FileWriter fw = new FileWriter(filePath, false);
        BufferedWriter bw = new BufferedWriter(fw);

        // Write header
        bw.write("============================================================");
        bw.newLine();
        bw.write("           SKILLBRIDGE - ADMIN HISTORY LOGS                ");
        bw.newLine();
        bw.write("============================================================");
        bw.newLine();
        bw.newLine();

        // Get all events from stack (newest first)
        List<String> events = stack.displayAll();

        if (events.isEmpty()) {
            bw.write("  No history events recorded yet.");
            bw.newLine();
        } else {
            bw.write("  Total Events: " + events.size());
            bw.newLine();
            bw.newLine();

            int counter = 1;
            for (String event : events) {
                bw.write(counter + ". " + event);
                bw.newLine();
                counter++;
            }
        }
        bw.newLine();
        bw.write("============================================================");
        bw.newLine();
        bw.write("  Generated On: " + DateUtil.getCurrentDateTime());
        bw.newLine();
        bw.write("============================================================");
        bw.newLine();

        // Close the writer (flushes buffer and releases file)
        bw.close();

        return filePath;
    }
}
