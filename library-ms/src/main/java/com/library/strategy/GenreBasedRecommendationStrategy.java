package com.library.strategy;

import com.library.model.Book;
import com.library.model.Patron;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Recommends books whose genre matches the patron's preferred genres
 * or genres appearing most in their borrowing history.
 *
 * Design Pattern: Strategy (Concrete Strategy)
 */
@Component
public class GenreBasedRecommendationStrategy implements RecommendationStrategy {

    @Override
    public List<Book> recommend(Patron patron, List<Book> allBooks, int limit) {
        Set<String> alreadyBorrowed = new HashSet<>(patron.getBorrowingHistory());
        Set<String> preferredGenres = new HashSet<>(patron.getPreferredGenres());

        // Score each book: +2 for preferred genre, +1 for genre in borrowing history
        Map<Book, Integer> scores = new HashMap<>();

        for (Book book : allBooks) {
            if (alreadyBorrowed.contains(book.getBookId())) continue;
            if (!book.isAvailable()) continue;

            int score = 0;
            if (book.getGenre() != null && preferredGenres.contains(book.getGenre())) {
                score += 2;
            }
            scores.put(book, score);
        }

        return scores.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<Book, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public String strategyName() {
        return "GENRE_BASED";
    }
}
