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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.example.database.DatabaseConnection;
import org.example.models.Book;

public class BookDAOImpl implements BookDAO {
    private final List<Book> bookList = new ArrayList<>();
    private static final String FILE_PATH = "books.txt";

    public BookDAOImpl() {
        loadBooksFromFile();    
        loadBooksFromDatabase(); 
    }

    @Override
    public void addBook(Book book) {
        if (bookList.contains(book)) {
            System.out.println("Book already exists.");
            return;
        }
        bookList.add(book);
        saveBookToFile(book);      
        saveBookToDatabase(book);
        System.out.println("Book added: " + book.getTitle());
    }

    @Override
    public List<Book> getAllBooks() {
        if (bookList.isEmpty()) {
            System.out.println("No books found.");
        }
        return bookList;
    }

    @Override
    public Book searchBookByTitle(String title) {
        return bookList.stream()
                .filter(book -> book.getTitle().equalsIgnoreCase(title))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(" Book not found: " + title));
    }

    public Book getBookById(int bookId) {
        return bookList.stream()
                .filter(book -> book.getBookId() == bookId)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Book not found: ID " + bookId));
    }

    @Override
    public List<Book> searchBooksByAuthor(String author) {
        List<Book> result = bookList.stream()
                .filter(book -> book.getAuthor().toLowerCase().contains(author.toLowerCase()))
                .toList();
        
        if (result.isEmpty()) {
            System.out.println("No books found by author: " + author);
        }
        return result;
    }

    @Override
    public List<Book> searchBooksByGenre(String genre) {
        List<Book> result = bookList.stream()
                .filter(book -> book.getGenre().toLowerCase().contains(genre.toLowerCase()))
                .toList();
        
        if (result.isEmpty()) {
            System.out.println(" No books found in genre: " + genre);
        }
        return result;
    }

    @Override
    public List<Book> sortBooksByTitle() {
        if (bookList.isEmpty()) {
            System.out.println(" No books available to sort.");
            return new ArrayList<>();
        }
        List<Book> sortedList = new ArrayList<>(bookList);
        sortedList.sort(Comparator.comparing(Book::getTitle));
        return sortedList;
    }
    
    @Override
    public List<Book> sortBooksByGenre() {
        if (bookList.isEmpty()) {
            System.out.println(" No books available to sort.");
            return new ArrayList<>();
        }
        List<Book> sortedList = new ArrayList<>(bookList);
        sortedList.sort(Comparator.comparing(Book::getGenre));
        return sortedList;
    }

    @Override
    public void updateBook(Book updatedBook) {
        for (Book book : bookList) {
            if (book.getBookId() == updatedBook.getBookId()) {
                book.setTitle(updatedBook.getTitle());
                book.setAuthor(updatedBook.getAuthor());
                book.setGenre(updatedBook.getGenre());
                book.setAvailableCopies(updatedBook.getAvailableCopies());
                updateBookInDataBase(book);
                updateBookInFile(book);
                System.out.println("Book updated successfully: " + book);
                return;
            }
        }
        System.out.println("Book with ID " + updatedBook.getBookId() + " not found.");
    }
    
    @Override
    public void deleteBook(int bookId) {
        Iterator<Book> iterator = bookList.iterator();
        boolean bookFound = false;
        
        while (iterator.hasNext()) {
            Book book = iterator.next();
            if (book.getBookId() == bookId) {
                iterator.remove();
                deleteBookFromDatabase(bookId);
                deleteBookFromFile(bookId);
                System.out.println(" Book deleted successfully: " + book.getTitle());
                bookFound = true;
                break;
            }
        }
        
        if (!bookFound) {
            System.out.println(" Book with ID " + bookId + " not found.");
        }
    }


    private void saveBookToFile(Book book) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(book.getBookId() +"," + book.getTitle() + "," + book.getAuthor() + "," + book.getGenre() + "," + book.getAvailableCopies());
            writer.newLine();
        } catch (IOException e) {
            System.out.println(" Error writing to file: " + e.getMessage());
        }
    }


    private void loadBooksFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    int bookId = Integer.parseInt(parts[0]);
                    Book book = new Book(bookId, parts[1], parts[2], parts[3], Integer.parseInt(parts[3]));
                    bookList.add(book);
                }
            }
        } catch (IOException e) {
            System.out.println(" No existing book file found, creating a new one.");
        }
    }


    private void saveBookToDatabase(Book book) {
        String sql = "INSERT INTO books (title, author, genre, available_copies, book_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getGenre());
            stmt.setInt(4, book.getAvailableCopies());
            stmt.setInt(5, book.getBookId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(" Error saving book to database: " + e.getMessage());
        }
    }


    private void loadBooksFromDatabase() {
        String sql = "SELECT * FROM books";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Book book = new Book(
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("genre"),
                        rs.getInt("available_copies")
                );
                bookList.add(book);
            }
        } catch (SQLException e) {
            System.out.println(" Error loading books from database: " + e.getMessage());
        }
    }
    
    private void updateBookInDataBase(Book updatedBook) {
        String sql = "UPDATE books SET title = ?, author = ?, genre = ?, available_copies = ? WHERE book_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, updatedBook.getTitle());
            pstmt.setString(2, updatedBook.getAuthor());
            pstmt.setString(3, updatedBook.getGenre());
            pstmt.setInt(4, updatedBook.getAvailableCopies());
            pstmt.setInt(5, updatedBook.getBookId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println(" Book updated successfully: " + updatedBook);
            } else {
                System.out.println(" Book with ID " + updatedBook.getBookId() + " not found.");
            }
        } catch (SQLException e) {
            System.out.println(" Error updating book: " + e.getMessage());
        }
    }
    
    private void updateBookInFile(Book updatedBook) {
        File file = new File("books.txt");
        List<String> fileLines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean found = false;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                int bookId = Integer.parseInt(parts[0]);

                if (bookId == updatedBook.getBookId()) {
                    // Replace with updated details
                    line = updatedBook.getBookId() + "," +
                            updatedBook.getTitle() + "," +
                            updatedBook.getAuthor() + "," +
                            updatedBook.getGenre() + "," +
                            updatedBook.getAvailableCopies();
                    found = true;
                }
                fileLines.add(line);
            }

            if (!found) {
                System.out.println(" Book not found in file.");
                return;
            }

        } catch (IOException e) {
            System.out.println(" Error reading file: " + e.getMessage());
            return;
        }

        // Write updated content back to file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : fileLines) {
                writer.write(line);
                writer.newLine();
            }
            System.out.println(" Book updated successfully in file.");
        } catch (IOException e) {
            System.out.println(" Error writing file: " + e.getMessage());
        }
    }
    
    private void deleteBookFromDatabase(int bookId) {
        String sql = "DELETE FROM books WHERE book_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println(" Book deleted from database.");
            } else {
                System.out.println(" Book not found in database.");
            }
        } catch (SQLException e) {
            System.out.println(" Error deleting book from database: " + e.getMessage());
        }
    }
    
    private void deleteBookFromFile(int bookId) {
        File file = new File(FILE_PATH);
        List<String> fileLines = new ArrayList<>();
        boolean bookFound = false;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 0) {
                    int currentBookId = Integer.parseInt(parts[0]);
                    if (currentBookId != bookId) {
                        fileLines.add(line);
                    } else {
                        bookFound = true;
                    }
                }
            }
            
        } catch (IOException e) {
            System.out.println("️ Error reading file: " + e.getMessage());
            return;
        }
        

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : fileLines) {
                writer.write(line);
                writer.newLine();
            }
            
            if (bookFound) {
                System.out.println(" Book deleted from file.");
            } else {
                System.out.println(" Book not found in file.");
            }
        } catch (IOException e) {
            System.out.println(" Error writing file: " + e.getMessage());
        }
    }
}
