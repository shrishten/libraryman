package com.library.factory;

import com.library.strategy.RecommendationStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory that provides the correct RecommendationStrategy by name.
 * Auto-discovers all strategy beans via Spring DI — adding a new strategy
 * requires zero changes here (Open/Closed Principle).
 *
 * Design Pattern: Factory
 */
@Component
public class RecommendationStrategyFactory {

    private final Map<String, RecommendationStrategy> strategies;

    public RecommendationStrategyFactory(List<RecommendationStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        RecommendationStrategy::strategyName,
                        Function.identity()
                ));
    }

    /**
     * Returns the strategy for the given name, or the first available as fallback.
     *
     * @param strategyName e.g. "GENRE_BASED", "AUTHOR_BASED", "POPULARITY_BASED"
     */
    public RecommendationStrategy getStrategy(String strategyName) {
        RecommendationStrategy strategy = strategies.get(strategyName.toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException(
                    "Unknown recommendation strategy: " + strategyName
                    + ". Available: " + strategies.keySet());
        }
        return strategy;
    }

    /**
     * Returns all available strategy names.
     */
    public List<String> availableStrategies() {
        return List.copyOf(strategies.keySet());
    }
}
