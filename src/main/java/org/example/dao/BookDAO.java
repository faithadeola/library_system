package org.example.dao;

import org.example.models.Book;
import java.util.List;

public interface BookDAO {
    void addBook(Book book);
    List<Book> getAllBooks();
    Book searchBookByTitle(String title);
    List<Book> searchBooksByAuthor(String author);
    List<Book> searchBooksByGenre(String genre);
    List<Book> sortBooksByTitle();
    List<Book> sortBooksByGenre();
    void updateBook(Book book);
    void deleteBook(int bookId);
}
