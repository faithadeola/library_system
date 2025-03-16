package org.example.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String LOG_FILE = "library_log.txt";

    public static void log(String message) {
        try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            writer.write("[" + timestamp + "] " + message + "\n");
            System.out.println(" Log written: " + message);
        } catch (IOException e) {
            System.out.println(" Error writing to log file: " + e.getMessage());
        }
    }
}
