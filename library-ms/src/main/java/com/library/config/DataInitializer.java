package com.library.config;

import com.library.dto.request.*;
import com.library.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds the in-memory store with sample branches, books, and patrons
 * so the API is immediately explorable after startup.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final BranchService branchService;
    private final BookService bookService;
    private final PatronService patronService;

    public DataInitializer(BranchService branchService,
                           BookService bookService,
                           PatronService patronService) {
        this.branchService = branchService;
        this.bookService = bookService;
        this.patronService = patronService;
    }

    @Override
    public void run(String... args) {
        log.info("=== Seeding sample data ===");

        // ── Branches ──────────────────────────────────────────────────────────
        var central = branchService.createBranch(branch("Central Branch", "123 Main St", "555-0100"));
        var north   = branchService.createBranch(branch("North Branch",   "456 Oak Ave",  "555-0200"));

        // ── Books at Central ─────────────────────────────────────────────────
        bookService.addBook(book("978-0-06-112008-4", "To Kill a Mockingbird",      "Harper Lee",          1960, "Fiction",       central.getBranchId()));
        bookService.addBook(book("978-0-7432-7356-5", "1984",                       "George Orwell",       1949, "Dystopian",     central.getBranchId()));
        bookService.addBook(book("978-0-7432-7356-5", "1984",                       "George Orwell",       1949, "Dystopian",     central.getBranchId())); // second copy
        bookService.addBook(book("978-0-14-028329-7", "The Great Gatsby",           "F. Scott Fitzgerald", 1925, "Fiction",       central.getBranchId()));
        bookService.addBook(book("978-0-06-093546-9", "To Kill a Mockingbird",      "Harper Lee",          1960, "Fiction",       central.getBranchId())); // alt edition
        bookService.addBook(book("978-0-525-55360-5", "The Midnight Library",       "Matt Haig",           2020, "Fiction",       central.getBranchId()));
        bookService.addBook(book("978-0-385-33348-1", "The Hitchhiker's Guide",     "Douglas Adams",       1979, "Science Fiction", central.getBranchId()));

        // ── Books at North ────────────────────────────────────────────────────
        bookService.addBook(book("978-0-14-028329-7", "The Great Gatsby",           "F. Scott Fitzgerald", 1925, "Fiction",       north.getBranchId()));
        bookService.addBook(book("978-0-385-33348-1", "The Hitchhiker's Guide",     "Douglas Adams",       1979, "Science Fiction", north.getBranchId()));
        bookService.addBook(book("978-0-316-76948-0", "The Catcher in the Rye",     "J.D. Salinger",       1951, "Fiction",       north.getBranchId()));
        bookService.addBook(book("978-1-250-31776-7", "Project Hail Mary",          "Andy Weir",           2021, "Science Fiction", north.getBranchId()));

        // ── Patrons ───────────────────────────────────────────────────────────
        patronService.registerPatron(patron("Alice Johnson", "alice@example.com", "555-1001",
                java.util.List.of("Fiction", "Dystopian")));
        patronService.registerPatron(patron("Bob Smith",    "bob@example.com",   "555-1002",
                java.util.List.of("Science Fiction")));
        patronService.registerPatron(patron("Carol White",  "carol@example.com", "555-1003",
                java.util.List.of("Fiction")));

        log.info("=== Sample data seeded: 2 branches, 11 books, 3 patrons ===");
    }

    private CreateBranchRequest branch(String name, String address, String phone) {
        var r = new CreateBranchRequest();
        r.setName(name);
        r.setAddress(address);
        r.setPhone(phone);
        return r;
    }

    private CreateBookRequest book(String isbn, String title, String author,
                                   int year, String genre, String branchId) {
        var r = new CreateBookRequest();
        r.setIsbn(isbn);
        r.setTitle(title);
        r.setAuthor(author);
        r.setPublicationYear(year);
        r.setGenre(genre);
        r.setBranchId(branchId);
        return r;
    }

    private CreatePatronRequest patron(String name, String email, String phone,
                                       java.util.List<String> genres) {
        var r = new CreatePatronRequest();
        r.setName(name);
        r.setEmail(email);
        r.setPhone(phone);
        r.setPreferredGenres(genres);
        return r;
    }
}
