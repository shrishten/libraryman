package com.library.service;

import com.library.model.Book;

import java.util.List;

public interface RecommendationService {

    List<Book> getRecommendations(String patronId, String strategy, int limit);

    List<String> getAvailableStrategies();
}
