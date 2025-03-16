package org.example.dao;

import org.example.models.Borrowing;
import java.util.List;

public interface BorrowingDAO {
    void borrowBook(int bookId, int memberId);
    void returnBook(int bookId, int memberId);
    List<Borrowing> getAllBorrowings();
    boolean isBookBorrowed(int bookId);
    void deleteBorrowing(int id);
}
