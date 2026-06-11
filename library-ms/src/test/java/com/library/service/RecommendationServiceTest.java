package com.library.service;

import com.library.enums.BookStatus;
import com.library.factory.RecommendationStrategyFactory;
import com.library.model.Book;
import com.library.model.Patron;
import com.library.repository.BookRepository;
import com.library.repository.PatronRepository;
import com.library.service.impl.RecommendationServiceImpl;
import com.library.strategy.GenreBasedRecommendationStrategy;
import com.library.strategy.RecommendationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecommendationServiceTest {

    private PatronRepository patronRepository;
    private BookRepository bookRepository;
    private RecommendationStrategyFactory strategyFactory;
    private RecommendationService recommendationService;

    private static final String PATRON_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        patronRepository = mock(PatronRepository.class);
        bookRepository   = mock(BookRepository.class);
        strategyFactory  = mock(RecommendationStrategyFactory.class);
        recommendationService = new RecommendationServiceImpl(
                patronRepository, bookRepository, strategyFactory);
    }

    @Test
    @DisplayName("getRecommendations: delegates to correct strategy and returns results")
    void getRecommendations_delegatesToStrategy() {
        Patron patron = Patron.builder()
                .patronId(PATRON_ID).name("Alice").email("a@b.com")
                .memberSince(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .preferredGenres(List.of("Fiction"))
                .build();

        Book book = Book.builder()
                .bookId("b1").isbn("x").title("Fictional Story")
                .author("Some Author").publicationYear(2020)
                .genre("Fiction").branchId("br1")
                .status(BookStatus.AVAILABLE)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        RecommendationStrategy mockStrategy = mock(RecommendationStrategy.class);
        when(mockStrategy.recommend(any(), any(), eq(5))).thenReturn(List.of(book));
        when(mockStrategy.strategyName()).thenReturn("GENRE_BASED");

        when(patronRepository.findById(PATRON_ID)).thenReturn(Optional.of(patron));
        when(bookRepository.findAll()).thenReturn(List.of(book));
        when(strategyFactory.getStrategy("GENRE_BASED")).thenReturn(mockStrategy);

        List<Book> results = recommendationService.getRecommendations(PATRON_ID, "GENRE_BASED", 5);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Fictional Story");
        verify(mockStrategy).recommend(eq(patron), anyList(), eq(5));
    }

    @Test
    @DisplayName("GenreBasedStrategy: only recommends available books in preferred genres")
    void genreStrategy_filtersCorrectly() {
        Patron patron = Patron.builder()
                .patronId(PATRON_ID).name("Bob").email("b@c.com")
                .memberSince(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .preferredGenres(List.of("Science Fiction"))
                .build();

        Book sciFiAvailable = Book.builder().bookId("b1").isbn("a").title("Dune")
                .author("Frank Herbert").publicationYear(1965).genre("Science Fiction")
                .branchId("br1").status(BookStatus.AVAILABLE)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        Book sciFiBorrowed = Book.builder().bookId("b2").isbn("b").title("Foundation")
                .author("Asimov").publicationYear(1951).genre("Science Fiction")
                .branchId("br1").status(BookStatus.BORROWED)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        Book fictionAvailable = Book.builder().bookId("b3").isbn("c").title("Pride and Prejudice")
                .author("Austen").publicationYear(1813).genre("Fiction")
                .branchId("br1").status(BookStatus.AVAILABLE)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        GenreBasedRecommendationStrategy strategy = new GenreBasedRecommendationStrategy();
        List<Book> results = strategy.recommend(patron, List.of(sciFiAvailable, sciFiBorrowed, fictionAvailable), 10);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Dune");
    }
}
