package org.example;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.example.dao.BookDAO;
import org.example.dao.BookDAOImpl;
import org.example.dao.MemberDAO;
import org.example.dao.MemberDAOImpl;
import org.example.models.Book;
import org.example.models.Member;
import org.example.services.BookService;
import org.example.services.BorrowingService;
import org.example.services.MemberService;
import org.example.utils.CSVExporter;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.println("\n----- Library Management System -----");
            System.out.println("1. Borrow/Return books");
            System.out.println("2. Manage books");
            System.out.println("3. Manage members");
            System.out.println("4. Exit");
            System.out.print("\nEnter your choice: ");
            
            int feature = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (feature) {
                case 1 -> manageBorrowings();
                case 2 -> manageBook();
                case 3 -> manageMembers();
                case 4 -> {
                    System.out.println("Thank you for using Library Management System. Goodbye!");
                    scanner.close();
                    System.exit(0);
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    public static void manageBook() {
        Scanner scanner = new Scanner(System.in);
        BookDAO bookDAO = new BookDAOImpl();
        BookService bookService = new BookService(bookDAO);

        while (true) {
            System.out.println("\n----- Book Management Menu -----");
            System.out.println("1. Add a book");
            System.out.println("2. Update book details");
            System.out.println("3. Delete a book");
            System.out.println("4. Search books");
            System.out.println("5. Show all books");
            System.out.println("6. Sort books");
            System.out.println("7. Export books to CSV");
            System.out.println("8. Return to main menu");
            System.out.print("\nEnter your choice: ");

            int bookAction = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (bookAction) {
                case 1 -> {
                    System.out.print("Enter book title: ");
                    String title = scanner.nextLine();
                    System.out.print("Enter author: ");
                    String author = scanner.nextLine();
                    System.out.print("Enter genre: ");
                    String genre = scanner.nextLine();
                    System.out.print("Enter available copies: ");
                    int copies = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    bookService.addBook(title, author, genre, copies);
                }
                case 2 -> {
                    System.out.print("Enter Book ID to update: ");
                    int bookId = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    System.out.print("Enter new title: ");
                    String newTitle = scanner.nextLine();

                    System.out.print("Enter new author: ");
                    String newAuthor = scanner.nextLine();

                    System.out.print("Enter new genre: ");
                    String newGenre = scanner.nextLine();

                    System.out.print("Enter new available copies: ");
                    int newAvailableCopies = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    bookService.updateBook(bookId, newTitle, newAuthor, newGenre, newAvailableCopies);
                }
                case 3 -> {
                    System.out.print("Enter Book ID to delete: ");
                    int bookId = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    bookService.deleteBook(bookId);
                }
                case 4 -> {
                    System.out.println("\n----- Search Options -----");
                    System.out.println("1. Search by title");
                    System.out.println("2. Search by author");
                    System.out.println("3. Search by genre");
                    System.out.print("Enter your choice: ");

                    int searchOption = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    switch (searchOption) {
                        case 1 -> {
                            System.out.print("Enter book title to search: ");
                            String searchTitle = scanner.nextLine();
                            Book book = bookService.searchBookByTitle(searchTitle);
                            if (book != null) {
                                System.out.println("\nBook found:\n" + book);
                            }
                        }
                        case 2 -> {
                            System.out.print("Enter author name to search: ");
                            String searchAuthor = scanner.nextLine();
                            List<Book> books = bookService.searchBooksByAuthor(searchAuthor);
                            if (!books.isEmpty()) {
                                System.out.println("\nBooks by author '" + searchAuthor + "':");
                                books.forEach(book -> System.out.println("- " + book));
                            }
                        }
                        case 3 -> {
                            System.out.print("Enter genre to search: ");
                            String searchGenre = scanner.nextLine();
                            List<Book> books = bookService.searchBooksByGenre(searchGenre);
                            if (!books.isEmpty()) {
                                System.out.println("\nBooks in genre '" + searchGenre + "':");
                                books.forEach(book -> System.out.println("- " + book));
                            }
                        }
                        default -> System.out.println("Invalid search option.");
                    }
                }
                case 5 -> {
                    List<Book> allBooks = bookService.getAllBooks();
                    if (!allBooks.isEmpty()) {
                        System.out.println("\nAll Books in Library:");
                        allBooks.forEach(book -> System.out.println("- " + book));
                    } else {
                        System.out.println("No books in the library.");
                    }
                }
                case 6 -> {
                    System.out.println("\n----- Sort Options -----");
                    System.out.println("1. Sort by title");
                    System.out.println("2. Sort by genre");
                    System.out.print("Enter your choice: ");

                    int sortOption = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    List<Book> sortedBooks;
                    switch (sortOption) {
                        case 1 -> {
                            sortedBooks = bookService.sortBooksByTitle();
                            if (!sortedBooks.isEmpty()) {
                                System.out.println("\nBooks sorted by title:");
                                sortedBooks.forEach(book -> System.out.println("- " + book));
                            }
                        }
                        case 2 -> {
                            sortedBooks = bookService.sortBooksByGenre();
                            if (!sortedBooks.isEmpty()) {
                                System.out.println("\nBooks sorted by genre:");
                                sortedBooks.forEach(book -> System.out.println("- " + book));
                            }
                        }
                        default -> System.out.println("Invalid sort option.");
                    }
                }
                case 7 -> {
                    // Export books to CSV
                    List<Book> allBooks = bookService.getAllBooks();
                    if (!allBooks.isEmpty()) {
                        CSVExporter.exportBooksToCSV("books.csv", allBooks);
                    } else {
                        System.out.println("No books in the library to export.");
                    }
                }
                case 8 -> {
                    return; // Return to main menu
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }
    
    public static void manageMembers() {
        Scanner scanner = new Scanner(System.in);
        MemberDAO memberDAO = new MemberDAOImpl();
        MemberService memberService = new MemberService(memberDAO);

        while (true) {
            System.out.println("\n----- Member Management Menu -----");
            System.out.println("1. Add a new member");
            System.out.println("2. Update member details");
            System.out.println("3. Delete a member");
            System.out.println("4. Find member by ID");
            System.out.println("5. Display all members");
            System.out.println("6. Export members to CSV");
            System.out.println("7. Return to main menu");
            System.out.print("\nEnter your choice: ");

            int memberAction = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (memberAction) {
                case 1 -> {
                    System.out.print("Enter member name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter email: ");
                    String email = scanner.nextLine();
                    System.out.print("Enter phone number: ");
                    String phone = scanner.nextLine();

                    memberService.addMember(name, email, phone);
                }
                case 2 -> {
                    System.out.print("Enter Member ID to update: ");
                    int memberId = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    Member existingMember = memberService.getMemberById(memberId);
                    if (existingMember != null) {
                        System.out.print("Enter new name (current: " + existingMember.getName() + "): ");
                        String newName = scanner.nextLine();

                        System.out.print("Enter new email (current: " + existingMember.getEmail() + "): ");
                        String newEmail = scanner.nextLine();

                        System.out.print("Enter new phone (current: " + existingMember.getPhone() + "): ");
                        String newPhone = scanner.nextLine();

                        memberService.updateMember(memberId, newName, newEmail, newPhone);
                    }
                }
                case 3 -> {
                    System.out.print("Enter Member ID to delete: ");
                    int memberId = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    System.out.print("Are you sure you want to delete this member? (y/n): ");
                    String confirmation = scanner.nextLine();

                    if (confirmation.equalsIgnoreCase("y")) {
                        memberService.deleteMember(memberId);
                    } else {
                        System.out.println("Deletion cancelled.");
                    }
                }
                case 4 -> {
                    System.out.print("Enter Member ID to find: ");
                    int memberId = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    Member member = memberService.getMemberById(memberId);
                    if (member != null) {
                        System.out.println("\nMember found:");
                        System.out.println("ID: " + member.getId());
                        System.out.println("Name: " + member.getName());
                        System.out.println("Email: " + member.getEmail());
                        System.out.println("Phone: " + member.getPhone());
                    }
                }
                case 5 -> {
                    List<Member> allMembers = memberService.getAllMembers();
                    if (!allMembers.isEmpty()) {
                        System.out.println("\nAll Library Members:");
                        for (Member member : allMembers) {
                            System.out.println("\nID: " + member.getId());
                            System.out.println("Name: " + member.getName());
                            System.out.println("Email: " + member.getEmail());
                            System.out.println("Phone: " + member.getPhone());
                            System.out.println("------------------------");
                        }
                    }
                }
                case 6 -> {
                    // Export members to CSV
                    List<Member> allMembers = memberService.getAllMembers();
                    if (!allMembers.isEmpty()) {
                        CSVExporter.exportMembersToCSV("members.csv", allMembers);
                    } else {
                        System.out.println("No members in the system to export.");
                    }
                }
                case 7 -> {
                    return; // Return to main menu
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }
    
    public static void manageBorrowings() {
        Scanner scanner = new Scanner(System.in);
        BorrowingService borrowingService = new BorrowingService();
        BookDAO bookDAO = new BookDAOImpl();
        BookService bookService = new BookService(bookDAO);

        while (true) {
            System.out.println("\n----- Borrowing Management Menu -----");
            System.out.println("1. Borrow a book");
            System.out.println("2. Return a book");
            System.out.println("3. View all borrowing records");
            System.out.println("4. View member's borrowed books");
            System.out.println("5. Return to main menu");
            System.out.print("\nEnter your choice: ");

            int action = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (action) {
                case 1 -> {
                    // Borrow a book
                    System.out.print("Enter member email: ");
                    String email = scanner.nextLine();
                    
                    // Verify member exists
                    int memberId = borrowingService.findMemberByEmail(email);
                    if (memberId == -1) {
                        System.out.println("\n❌ Member not found with that email. Please register first.");
                        continue;
                    }
                    
                    // Show available books
                    List<Book> availableBooks = bookService.getAllBooks().stream()
                            .filter(book -> book.getAvailableCopies() > 0)
                            .collect(Collectors.toList());
                    
                    if (availableBooks.isEmpty()) {
                        System.out.println("\n❌ No books available for borrowing.");
                        continue;
                    }
                    
                    System.out.println("\nAvailable Books:");
                    availableBooks.forEach(book -> {
                        System.out.println("ID: " + book.getBookId() + 
                                          " | Title: " + book.getTitle() + 
                                          " | Author: " + book.getAuthor() +
                                          " | Available Copies: " + book.getAvailableCopies());
                    });
                    
                    System.out.print("\nEnter Book ID to borrow: ");
                    int bookId = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    
                    boolean success = borrowingService.borrowBook(bookId, memberId);
                    if (success) {
                        System.out.println("\n✅ Book borrowed successfully!");
                    }
                }
                case 2 -> {
                    // Return a book
                    System.out.print("Enter member email: ");
                    String email = scanner.nextLine();
                    
                    // Verify member exists
                    int memberId = borrowingService.findMemberByEmail(email);
                    if (memberId == -1) {
                        System.out.println("\n❌ Member not found with that email.");
                        continue;
                    }
                    
                    // Show books borrowed by this member
                    List<Book> borrowedBooks = borrowingService.getBorrowedBookDetailsByMember(memberId);
                    
                    if (borrowedBooks.isEmpty()) {
                        System.out.println("\n❌ No books currently borrowed by this member.");
                        continue;
                    }
                    
                    System.out.println("\nBorrowed Books:");
                    borrowedBooks.forEach(book -> {
                        System.out.println("ID: " + book.getBookId() + 
                                          " | Title: " + book.getTitle() + 
                                          " | Author: " + book.getAuthor());
                    });
                    
                    System.out.print("\nEnter Book ID to return: ");
                    int bookId = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    
                    boolean success = borrowingService.returnBook(bookId, memberId);
                    if (success) {
                        System.out.println("\n✅ Book returned successfully!");
                    }
                }
                case 3 -> {
                    // View all borrowing records
                    List<String> borrowingDetails = borrowingService.getBorrowingDetails();
                    
                    if (borrowingDetails.isEmpty()) {
                        System.out.println("\nℹ️ No borrowing records found.");
                    } else {
                        System.out.println("\nAll Borrowing Records:");
                        borrowingDetails.forEach(System.out::println);
                    }
                }
                case 4 -> {
                    // View member's borrowed books
                    System.out.print("Enter member email: ");
                    String email = scanner.nextLine();
                    
                    // Verify member exists
                    int memberId = borrowingService.findMemberByEmail(email);
                    if (memberId == -1) {
                        System.out.println("\n❌ Member not found with that email.");
                        continue;
                    }
                    
                    // Show books borrowed by this member
                    List<Book> borrowedBooks = borrowingService.getBorrowedBookDetailsByMember(memberId);
                    
                    if (borrowedBooks.isEmpty()) {
                        System.out.println("\nℹ️ No books currently borrowed by this member.");
                    } else {
                        System.out.println("\nBooks Currently Borrowed:");
                        borrowedBooks.forEach(book -> {
                            System.out.println("ID: " + book.getBookId() + 
                                              " | Title: " + book.getTitle() + 
                                              " | Author: " + book.getAuthor());
                        });
                    }
                }
                case 5 -> {
                    return; // Return to main menu
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }
}
