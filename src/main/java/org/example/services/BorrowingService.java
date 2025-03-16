package org.example.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.example.dao.BookDAO;
import org.example.dao.BookDAOImpl;
import org.example.dao.BorrowingDAO;
import org.example.dao.BorrowingDAOImpl;
import org.example.dao.MemberDAO;
import org.example.dao.MemberDAOImpl;
import org.example.models.Book;
import org.example.models.Borrowing;
import org.example.models.Member;
import org.example.utils.Logger;

public class BorrowingService {
    private final BorrowingDAO borrowingDAO;
    private final BookDAO bookDAO;
    private final MemberDAO memberDAO;

    public BorrowingService() {
        this.borrowingDAO = new BorrowingDAOImpl();
        this.bookDAO = new BookDAOImpl();
        this.memberDAO = new MemberDAOImpl();
    }

    public int findMemberByEmail(String email) {
        List<Member> allMembers = memberDAO.getAllMembers();
        for (Member member : allMembers) {
            if (member.getEmail().equalsIgnoreCase(email)) {
                return member.getId();
            }
        }
        return -1; // Member not found
    }

    public boolean borrowBook(int bookId, int memberId) {
        try {
            // Validate member
            Member member = memberDAO.getMemberById(memberId);
            if (member == null) {
                System.out.println("❌ Member not found.");
                return false;
            }

            Book book = ((BookDAOImpl) bookDAO).getBookById(bookId);
            if (book == null) {
                System.out.println("❌ Book not found.");
                return false;
            }

            if (book.getAvailableCopies() <= 0) {
                System.out.println("❌ No copies available for borrowing.");
                return false;
            }

            // Check if already borrowed by this member
            if (isBookBorrowedByMember(bookId, memberId)) {
                System.out.println("❌ You have already borrowed this book.");
                return false;
            }

            // Process borrowing
            borrowingDAO.borrowBook(bookId, memberId);
            Logger.log("Book '" + book.getTitle() + "' borrowed by " + member.getName());
            return true;
        } catch (Exception e) {
            System.out.println("❌ Error in borrowing process: " + e.getMessage());
            return false;
        }
    }

    public boolean returnBook(int bookId, int memberId) {
        try {
            // Validate member
            Member member = memberDAO.getMemberById(memberId);
            if (member == null) {
                System.out.println("❌ Member not found.");
                return false;
            }

            // Check if book exists
            Book book = ((BookDAOImpl) bookDAO).getBookById(bookId);
            if (book == null) {
                System.out.println("❌ Book not found.");
                return false;
            }

            // Check if book is actually borrowed by this member
            if (!isBookBorrowedByMember(bookId, memberId)) {
                System.out.println("❌ This book is not borrowed by you.");
                return false;
            }

            // Process return
            borrowingDAO.returnBook(bookId, memberId);
            Logger.log("Book '" + book.getTitle() + "' returned by " + member.getName());
            return true;
        } catch (Exception e) {
            System.out.println("❌ Error in return process: " + e.getMessage());
            return false;
        }
    }

    public List<Borrowing> getAllActiveBorrowings() {
        return borrowingDAO.getAllBorrowings().stream()
                .filter(b -> b.getReturnDate() == null)
                .collect(Collectors.toList());
    }

    public List<Borrowing> getAllBorrowings() {
        return borrowingDAO.getAllBorrowings();
    }

    public List<Integer> getBorrowedBooksByMember(int memberId) {
        return borrowingDAO.getAllBorrowings().stream()
                .filter(b -> b.getMemberId() == memberId && b.getReturnDate() == null)
                .map(Borrowing::getBookId)
                .collect(Collectors.toList());
    }

    public boolean isBookBorrowedByMember(int bookId, int memberId) {
        return borrowingDAO.getAllBorrowings().stream()
                .anyMatch(b -> b.getBookId() == bookId && 
                               b.getMemberId() == memberId && 
                               b.getReturnDate() == null);
    }

    public List<Book> getBorrowedBookDetailsByMember(int memberId) {
        List<Book> result = new ArrayList<>();
        List<Integer> borrowedBookIds = getBorrowedBooksByMember(memberId);
        
        for (int bookId : borrowedBookIds) {
            try {
                Book book = ((BookDAOImpl) bookDAO).getBookById(bookId);
                result.add(book);
            } catch (Exception e) {
                // Skip if book not found
            }
        }
        
        return result;
    }

    public List<String> getBorrowingDetails() {
        List<String> details = new ArrayList<>();
        List<Borrowing> borrowings = getAllBorrowings();
        
        for (Borrowing borrowing : borrowings) {
            try {
                Book book = ((BookDAOImpl) bookDAO).getBookById(borrowing.getBookId());
                Member member = memberDAO.getMemberById(borrowing.getMemberId());
                
                String status = borrowing.getReturnDate() == null ? "Active" : "Returned";
                String returnDate = borrowing.getReturnDate() == null ? "Not returned" : 
                                   borrowing.getReturnDate().toString();
                
                String detail = String.format(
                    "Borrowing ID: %d | Book: %s | Member: %s | Status: %s | Borrowed: %s | Return: %s",
                    borrowing.getId(),
                    book != null ? book.getTitle() : "Unknown Book",
                    member != null ? member.getName() : "Unknown Member",
                    status,
                    borrowing.getBorrowDate(),
                    returnDate
                );
                
                details.add(detail);
            } catch (Exception e) {
                // Skip if there's an error getting details
            }
        }
        
        return details;
    }
}
