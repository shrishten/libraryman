package com.library.service.impl;

import com.library.factory.RecommendationStrategyFactory;
import com.library.model.Book;
import com.library.model.Patron;
import com.library.repository.BookRepository;
import com.library.repository.PatronRepository;
import com.library.service.RecommendationService;
import com.library.exception.ResourceNotFoundException;
import com.library.strategy.RecommendationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Delegates to the appropriate RecommendationStrategy selected via the Factory.
 *
 * Design Patterns used: Strategy (selects algorithm at runtime) + Factory (resolves strategy by name)
 */
@Service
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationServiceImpl.class);
    private static final int DEFAULT_LIMIT = 5;

    private final PatronRepository patronRepository;
    private final BookRepository bookRepository;
    private final RecommendationStrategyFactory strategyFactory;

    public RecommendationServiceImpl(PatronRepository patronRepository,
                                     BookRepository bookRepository,
                                     RecommendationStrategyFactory strategyFactory) {
        this.patronRepository = patronRepository;
        this.bookRepository = bookRepository;
        this.strategyFactory = strategyFactory;
    }

    @Override
    public List<Book> getRecommendations(String patronId, String strategy, int limit) {
        Patron patron = patronRepository.findById(patronId)
                .orElseThrow(() -> new ResourceNotFoundException("Patron not found: " + patronId));

        List<Book> allBooks = bookRepository.findAll();
        int effectiveLimit = limit > 0 ? limit : DEFAULT_LIMIT;

        RecommendationStrategy selectedStrategy = strategyFactory.getStrategy(strategy);
        List<Book> recommendations = selectedStrategy.recommend(patron, allBooks, effectiveLimit);

        log.info("Generated {} recommendations for patron={} using strategy={}",
                recommendations.size(), patronId, strategy);
        return recommendations;
    }

    @Override
    public List<String> getAvailableStrategies() {
        return strategyFactory.availableStrategies();
    }
}
