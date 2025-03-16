package org.example.dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.example.database.DatabaseConnection;
import org.example.models.Book;
import org.example.models.Borrowing;
import org.example.utils.Logger;

public class BorrowingDAOImpl implements BorrowingDAO {
    private final List<Borrowing> borrowings = new ArrayList<>();
    private final BookDAO bookDAO;
    private static final String FILE_PATH = "borrowings.txt";

    public BorrowingDAOImpl() {
        this.bookDAO = new BookDAOImpl();
        loadBorrowingsFromFile();
        loadBorrowingsFromDatabase();
    }

    @Override
    public void borrowBook(int bookId, int memberId) {

        Book book = findBookById(bookId);
        if (book == null) {
            System.out.println(" Book with ID " + bookId + " not found.");
            return;
        }
        
        if (book.getAvailableCopies() <= 0) {
            System.out.println(" No copies of this book are available for borrowing.");
            return;
        }
        

        if (isBookBorrowedByMember(bookId, memberId)) {
            System.out.println(" This book is already borrowed by you.");
            return;
        }

        try {

            updateBookCopies(bookId, -1); // Decrease by 1
            

            int borrowingId = borrowings.size() + 1;
            Date borrowDate = new Date();
            Date returnDate = null;

            Borrowing borrowing = new Borrowing(borrowingId, bookId, memberId, borrowDate, returnDate);
            borrowings.add(borrowing);
            

            saveBorrowingToDatabase(borrowing);
            saveBorrowingToFile(borrowing);
            
            System.out.println(" Book borrowed successfully.");
            Logger.log("Member ID " + memberId + " borrowed Book ID " + bookId);
        } catch (Exception e) {
            System.out.println(" Error borrowing book: " + e.getMessage());
        }
    }

    @Override
    public void returnBook(int bookId, int memberId) {
        // Find the borrowing record
        Borrowing borrowing = findBorrowing(bookId, memberId);
        if (borrowing == null) {
            System.out.println(" No borrowing record found for this book and member.");
            return;
        }
        
        try {

            updateBookCopies(bookId, 1);
            

            borrowing.setReturnDate(new Date());
            

            updateBorrowingInDatabase(borrowing);
            updateBorrowingInFile(borrowing);
            

            
            System.out.println(" Book returned successfully.");
            Logger.log("Member ID " + memberId + " returned Book ID " + bookId);
        } catch (Exception e) {
            System.out.println(" Error returning book: " + e.getMessage());
        }
    }

    @Override
    public List<Borrowing> getAllBorrowings() {
        return borrowings;
    }

    @Override
    public boolean isBookBorrowed(int bookId) {
        return borrowings.stream()
                .anyMatch(b -> b.getBookId() == bookId && b.getReturnDate() == null);
    }
    
    private boolean isBookBorrowedByMember(int bookId, int memberId) {
        return borrowings.stream()
                .anyMatch(b -> b.getBookId() == bookId && b.getMemberId() == memberId && b.getReturnDate() == null);
    }

    @Override
    public void deleteBorrowing(int id) {
        // Remove from in-memory list
        borrowings.removeIf(b -> b.getId() == id);
        
        // Delete from database and file
        deleteBorrowingFromDatabase(id);
        deleteBorrowingFromFile(id);
        
        System.out.println(" Borrowing record deleted successfully.");
        Logger.log("Deleted borrowing record with ID: " + id);
    }
    
    private Book findBookById(int bookId) {
        // This should use the BookDAO to get the book
        try {
            return ((BookDAOImpl) bookDAO).getBookById(bookId);
        } catch (Exception e) {
            return null;
        }
    }
    
    private Borrowing findBorrowing(int bookId, int memberId) {
        return borrowings.stream()
                .filter(b -> b.getBookId() == bookId && b.getMemberId() == memberId && b.getReturnDate() == null)
                .findFirst()
                .orElse(null);
    }
    
    private void updateBookCopies(int bookId, int change) {
        String sql = "UPDATE books SET available_copies = available_copies + ? WHERE book_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, change);
            stmt.setInt(2, bookId);
            stmt.executeUpdate();
            
            // Also update the in-memory book object if needed
            Book book = findBookById(bookId);
            if (book != null) {
                book.setAvailableCopies(book.getAvailableCopies() + change);
            }
        } catch (SQLException e) {
            System.out.println(" Error updating book copies: " + e.getMessage());
        }
    }
    
    private void loadBorrowingsFromDatabase() {
        String sql = "SELECT * FROM borrowings";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Borrowing borrowing = new Borrowing(
                        rs.getInt("id"),
                        rs.getInt("book_id"),
                        rs.getInt("member_id"),
                        rs.getTimestamp("borrow_date"),
                        rs.getTimestamp("return_date")
                );
                borrowings.add(borrowing);
            }
        } catch (SQLException e) {
            // Table might not exist yet, create it
            createBorrowingsTable();
        }
    }
    
    private void createBorrowingsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS borrowings ("
                + "id SERIAL PRIMARY KEY,"
                + "book_id INTEGER NOT NULL,"
                + "member_id INTEGER NOT NULL,"
                + "borrow_date TIMESTAMP NOT NULL,"
                + "return_date TIMESTAMP,"
                + "FOREIGN KEY (book_id) REFERENCES books(book_id),"
                + "FOREIGN KEY (member_id) REFERENCES members(member_id)"
                + ")";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(" Error creating borrowings table: " + e.getMessage());
        }
    }
    
    private void saveBorrowingToDatabase(Borrowing borrowing) {
        String sql = "INSERT INTO borrowings (book_id, member_id, borrow_date, return_date) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, borrowing.getBookId());
            stmt.setInt(2, borrowing.getMemberId());
            stmt.setTimestamp(3, new Timestamp(borrowing.getBorrowDate().getTime()));
            
            if (borrowing.getReturnDate() != null) {
                stmt.setTimestamp(4, new Timestamp(borrowing.getReturnDate().getTime()));
            } else {
                stmt.setNull(4, Types.TIMESTAMP);
            }
            
            stmt.executeUpdate();
            
            // Get the generated ID
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    borrowing.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.out.println(" Error saving borrowing to database: " + e.getMessage());
        }
    }
    
    private void updateBorrowingInDatabase(Borrowing borrowing) {
        String sql = "UPDATE borrowings SET return_date = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (borrowing.getReturnDate() != null) {
                stmt.setTimestamp(1, new Timestamp(borrowing.getReturnDate().getTime()));
            } else {
                stmt.setNull(1, Types.TIMESTAMP);
            }
            
            stmt.setInt(2, borrowing.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(" Error updating borrowing in database: " + e.getMessage());
        }
    }
    
    private void deleteBorrowingFromDatabase(int id) {
        String sql = "DELETE FROM borrowings WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(" Error deleting borrowing from database: " + e.getMessage());
        }
    }
    

    
    private void loadBorrowingsFromFile() {
        File file = new File(FILE_PATH);
        
        if (!file.exists()) {
            System.out.println("📂 Borrowings file not found. Will be created when adding borrowings.");
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    try {
                        int id = Integer.parseInt(parts[0]);
                        int bookId = Integer.parseInt(parts[1]);
                        int memberId = Integer.parseInt(parts[2]);
                        Date borrowDate = new Date(Long.parseLong(parts[3]));
                        Date returnDate = parts[4].equals("null") ? null : new Date(Long.parseLong(parts[4]));
                        
                        Borrowing borrowing = new Borrowing(id, bookId, memberId, borrowDate, returnDate);
                        

                        if (borrowings.stream().noneMatch(b -> b.getId() == id)) {
                            borrowings.add(borrowing);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println(" Invalid data format in borrowings file: " + line);
                    }
                }
            }
            
            System.out.println(" Loaded " + borrowings.size() + " borrowing records from file.");
        } catch (IOException e) {
            System.out.println(" Error reading borrowings file: " + e.getMessage());
        }
    }
    
    private void saveBorrowingToFile(Borrowing borrowing) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(formatBorrowingForFile(borrowing));
            writer.newLine();
            System.out.println(" Borrowing record saved to file.");
        } catch (IOException e) {
            System.out.println(" Error saving borrowing to file: " + e.getMessage());
        }
    }
    
    private void updateBorrowingInFile(Borrowing updatedBorrowing) {
        File file = new File(FILE_PATH);
        List<String> fileLines = new ArrayList<>();
        boolean borrowingFound = false;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 0) {
                    try {
                        int id = Integer.parseInt(parts[0]);
                        if (id == updatedBorrowing.getId()) {
                            // Replace with updated details
                            line = formatBorrowingForFile(updatedBorrowing);
                            borrowingFound = true;
                        }
                    } catch (NumberFormatException e) {
                        // Keep invalid lines
                    }
                }
                fileLines.add(line);
            }
            
        } catch (IOException e) {
            System.out.println(" Error reading borrowings file: " + e.getMessage());
            return;
        }
        

        if (!borrowingFound) {
            fileLines.add(formatBorrowingForFile(updatedBorrowing));
        }
        

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : fileLines) {
                writer.write(line);
                writer.newLine();
            }
            System.out.println(" Borrowing record updated in file.");
        } catch (IOException e) {
            System.out.println(" Error updating borrowings file: " + e.getMessage());
        }
    }
    
    private void deleteBorrowingFromFile(int borrowingId) {
        File file = new File(FILE_PATH);
        List<String> fileLines = new ArrayList<>();
        boolean borrowingFound = false;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 0) {
                    try {
                        int id = Integer.parseInt(parts[0]);
                        if (id == borrowingId) {
                            borrowingFound = true;
                            continue;
                        }
                    } catch (NumberFormatException e) {

                    }
                }
                fileLines.add(line);
            }
            
        } catch (IOException e) {
            System.out.println(" Error reading borrowings file: " + e.getMessage());
            return;
        }
        

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : fileLines) {
                writer.write(line);
                writer.newLine();
            }
            
            if (borrowingFound) {
                System.out.println(" Borrowing record deleted from file.");
            } else {
                System.out.println(" Borrowing record with ID " + borrowingId + " not found in file.");
            }
        } catch (IOException e) {
            System.out.println(" Error updating borrowings file: " + e.getMessage());
        }
    }
    
    private String formatBorrowingForFile(Borrowing borrowing) {
        return borrowing.getId() + "," +
               borrowing.getBookId() + "," +
               borrowing.getMemberId() + "," +
               borrowing.getBorrowDate().getTime() + "," +
               (borrowing.getReturnDate() == null ? "null" : borrowing.getReturnDate().getTime());
    }
}
