package org.example.utils;

import org.example.database.DatabaseConnection;
import org.example.models.Book;
import org.example.models.Member;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CSVExporter {

    public static void exportBooksToCSV(String fileName, List<Book> books) {
        try (FileWriter writer = new FileWriter(fileName)) {
            // Write header
            writer.append("Book ID,Title,Author,Genre,Available Copies,Export Date\n");
            
            // Write data rows
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            for (Book book : books) {
                writer.append(String.valueOf(book.getBookId())).append(",")
                      .append(escapeCsvField(book.getTitle())).append(",")
                      .append(escapeCsvField(book.getAuthor())).append(",")
                      .append(escapeCsvField(book.getGenre())).append(",")
                      .append(String.valueOf(book.getAvailableCopies())).append(",")
                      .append(timestamp).append("\n");
            }
            
            System.out.println("\n\u2705 Books exported to " + fileName);
            Logger.log("Exported " + books.size() + " books to CSV file: " + fileName);
            
        } catch (IOException e) {
            System.out.println("\n\u274c Error exporting books to CSV: " + e.getMessage());
        }
    }

    public static void exportMembersToCSV(String fileName, List<Member> members) {
        try (FileWriter writer = new FileWriter(fileName)) {
            // Write header
            writer.append("Member ID,Name,Email,Phone,Export Date\n");
            
            // Write data rows
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            for (Member member : members) {
                writer.append(String.valueOf(member.getId())).append(",")
                      .append(escapeCsvField(member.getName())).append(",")
                      .append(escapeCsvField(member.getEmail())).append(",")
                      .append(escapeCsvField(member.getPhone())).append(",")
                      .append(timestamp).append("\n");
            }
            
            System.out.println("\n\u2705 Members exported to " + fileName);
            Logger.log("Exported " + members.size() + " members to CSV file: " + fileName);
            
        } catch (IOException e) {
            System.out.println("\n\u274c Error exporting members to CSV: " + e.getMessage());
        }
    }
    
    // Helper method to escape fields that might contain commas
    private static String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            // Escape quotes by doubling them and wrap in quotes
            return "\""+field.replaceAll("\"", "\"\"")+"\"";
        }
        return field;
    }
}
