package com.library.strategy;

import com.library.model.Book;
import com.library.model.Patron;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Recommends books by authors the patron has already borrowed from.
 * Uses frequency scoring: authors borrowed more often rank higher.
 *
 * Design Pattern: Strategy (Concrete Strategy)
 */
@Component
public class AuthorBasedRecommendationStrategy implements RecommendationStrategy {

    @Override
    public List<Book> recommend(Patron patron, List<Book> allBooks, int limit) {
        Set<String> alreadyBorrowed = new HashSet<>(patron.getBorrowingHistory());

        // Build author frequency map from borrowing history
        Map<String, Long> authorFrequency = allBooks.stream()
                .filter(b -> alreadyBorrowed.contains(b.getBookId()))
                .collect(Collectors.groupingBy(Book::getAuthor, Collectors.counting()));

        if (authorFrequency.isEmpty()) return Collections.emptyList();

        // Score candidate books by author popularity
        Map<Book, Long> scores = new HashMap<>();
        for (Book book : allBooks) {
            if (alreadyBorrowed.contains(book.getBookId())) continue;
            if (!book.isAvailable()) continue;

            long score = authorFrequency.getOrDefault(book.getAuthor(), 0L);
            if (score > 0) scores.put(book, score);
        }

        return scores.entrySet().stream()
                .sorted(Map.Entry.<Book, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public String strategyName() {
        return "AUTHOR_BASED";
    }
}
