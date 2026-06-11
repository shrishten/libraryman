package com.library.repository;

import com.library.enums.LoanStatus;
import com.library.model.Loan;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory repository for Loan entities.
 */
@Repository
public class LoanRepository {

    private final Map<String, Loan> store = new ConcurrentHashMap<>();

    public Loan save(Loan loan) {
        store.put(loan.getLoanId(), loan);
        return loan;
    }

    public Optional<Loan> findById(String loanId) {
        return Optional.ofNullable(store.get(loanId));
    }

    public List<Loan> findAll() {
        return new ArrayList<>(store.values());
    }

    public List<Loan> findByPatronId(String patronId) {
        return store.values().stream()
                .filter(l -> patronId.equals(l.getPatronId()))
                .collect(Collectors.toList());
    }

    public Optional<Loan> findActiveByBookId(String bookId) {
        return store.values().stream()
                .filter(l -> bookId.equals(l.getBookId())
                        && LoanStatus.ACTIVE.equals(l.getStatus()))
                .findFirst();
    }

    public List<Loan> findByStatus(LoanStatus status) {
        return store.values().stream()
                .filter(l -> status.equals(l.getStatus()))
                .collect(Collectors.toList());
    }

    public List<Loan> findByPatronIdAndStatus(String patronId, LoanStatus status) {
        return store.values().stream()
                .filter(l -> patronId.equals(l.getPatronId()) && status.equals(l.getStatus()))
                .collect(Collectors.toList());
    }
}
