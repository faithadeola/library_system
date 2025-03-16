package org.example.services;

import org.example.dao.BookDAO;
import org.example.models.Book;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

public class BookService {
    private final BookDAO bookDAO;

    public BookService(BookDAO bookDAO) {
        this.bookDAO = bookDAO;
    }

    public void addBook(String title, String author, String genre, int availableCopies) {
        Random random = new Random();
        int bookId = 1000 + random.nextInt(9000);
        Book book = new Book(bookId, title, author, genre, availableCopies);
        bookDAO.addBook(book);
    }

    public void updateBook(int bookId, String newTitle, String newAuthor, String newGenre, int newAvailableCopies) {
        bookDAO.updateBook(new Book(bookId, newTitle, newAuthor, newGenre, newAvailableCopies));
    }


    public List<Book> getAllBooks() {
        return bookDAO.getAllBooks();
    }

    public Book searchBookByTitle(String title) {
        try {
            return bookDAO.searchBookByTitle(title);
        } catch (NoSuchElementException e) {
            System.out.println(" " + e.getMessage());
            return null;
        }
    }

    public List<Book> sortBooksByTitle() {
        return bookDAO.sortBooksByTitle();
    }
    
    public List<Book> sortBooksByGenre() {
        return bookDAO.sortBooksByGenre();
    }
    
    public List<Book> searchBooksByAuthor(String author) {
        return bookDAO.searchBooksByAuthor(author);
    }
    
    public List<Book> searchBooksByGenre(String genre) {
        return bookDAO.searchBooksByGenre(genre);
    }
    
    public void deleteBook(int bookId) {
        bookDAO.deleteBook(bookId);
    }
}
