package com.library.service;

import com.library.dto.request.CreatePatronRequest;
import com.library.dto.request.UpdatePatronRequest;
import com.library.exception.BusinessException;
import com.library.model.Patron;
import com.library.repository.LoanRepository;
import com.library.repository.PatronRepository;
import com.library.service.impl.PatronServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PatronServiceTest {

    private PatronRepository patronRepository;
    private LoanRepository   loanRepository;
    private PatronService    patronService;

    @BeforeEach
    void setUp() {
        patronRepository = mock(PatronRepository.class);
        loanRepository   = mock(LoanRepository.class);
        patronService    = new PatronServiceImpl(patronRepository, loanRepository);
    }

    @Test
    @DisplayName("registerPatron: creates patron with correct fields")
    void registerPatron_success() {
        when(patronRepository.findByEmail("alice@test.com")).thenReturn(Optional.empty());
        when(patronRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreatePatronRequest request = new CreatePatronRequest();
        request.setName("Alice"); request.setEmail("alice@test.com");
        request.setPhone("555-0001"); request.setPreferredGenres(List.of("Fiction"));

        Patron result = patronService.registerPatron(request);

        assertThat(result.getName()).isEqualTo("Alice");
        assertThat(result.getEmail()).isEqualTo("alice@test.com");
        assertThat(result.getPreferredGenres()).containsExactly("Fiction");
        assertThat(result.getPatronId()).isNotNull();
        verify(patronRepository).save(any());
    }

    @Test
    @DisplayName("registerPatron: throws BusinessException when email already exists")
    void registerPatron_duplicateEmail() {
        Patron existing = Patron.builder().patronId("p1").name("Other").email("alice@test.com")
                .memberSince(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        when(patronRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(existing));

        CreatePatronRequest request = new CreatePatronRequest();
        request.setName("Alice"); request.setEmail("alice@test.com");

        assertThatThrownBy(() -> patronService.registerPatron(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("updatePatron: updates only provided fields")
    void updatePatron_partialUpdate() {
        Patron patron = Patron.builder().patronId("p1").name("Alice").email("alice@test.com")
                .phone("111").memberSince(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(patronRepository.findById("p1")).thenReturn(Optional.of(patron));
        when(patronRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdatePatronRequest update = new UpdatePatronRequest();
        update.setPhone("999");

        Patron result = patronService.updatePatron("p1", update);

        assertThat(result.getPhone()).isEqualTo("999");
        assertThat(result.getName()).isEqualTo("Alice"); // unchanged
    }
}
