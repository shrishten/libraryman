package com.library.strategy;

import com.library.model.Book;
import com.library.model.Patron;

import java.util.List;

/**
 * Strategy interface for the Strategy design pattern.
 * Different recommendation algorithms can be swapped at runtime.
 *
 * Design Pattern: Strategy (interface)
 */
public interface RecommendationStrategy {

    /**
     * Returns a list of recommended books for the given patron.
     *
     * @param patron    the patron to generate recommendations for
     * @param allBooks  full library inventory to pick from
     * @param limit     maximum number of recommendations to return
     * @return ordered list of recommended books
     */
    List<Book> recommend(Patron patron, List<Book> allBooks, int limit);

    /**
     * Human-readable name for this strategy (used in API responses).
     */
    String strategyName();
}
