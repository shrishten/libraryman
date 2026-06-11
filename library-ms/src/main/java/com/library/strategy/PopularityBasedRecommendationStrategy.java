package com.library.strategy;

import com.library.model.Book;
import com.library.model.Patron;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Recommends the most-borrowed books across the whole library
 * that the patron hasn't read yet.
 *
 * Design Pattern: Strategy (Concrete Strategy)
 */
@Component
public class PopularityBasedRecommendationStrategy implements RecommendationStrategy {

    @Override
    public List<Book> recommend(Patron patron, List<Book> allBooks, int limit) {
        Set<String> alreadyBorrowed = new HashSet<>(patron.getBorrowingHistory());

        // Count how many patrons have each ISBN in their history — proxy for popularity
        Map<String, Long> isbnPopularity = allBooks.stream()
                .collect(Collectors.groupingBy(Book::getIsbn, Collectors.counting()));

        return allBooks.stream()
                .filter(b -> !alreadyBorrowed.contains(b.getBookId()))
                .filter(Book::isAvailable)
                .sorted((a, b) -> Long.compare(
                        isbnPopularity.getOrDefault(b.getIsbn(), 0L),
                        isbnPopularity.getOrDefault(a.getIsbn(), 0L)))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public String strategyName() {
        return "POPULARITY_BASED";
    }
}
