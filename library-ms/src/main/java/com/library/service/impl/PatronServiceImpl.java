package com.library.service.impl;

import com.library.dto.request.CreatePatronRequest;
import com.library.dto.request.UpdatePatronRequest;
import com.library.enums.LoanStatus;
import com.library.exception.BusinessException;
import com.library.exception.ResourceNotFoundException;
import com.library.model.Loan;
import com.library.model.Patron;
import com.library.repository.LoanRepository;
import com.library.repository.PatronRepository;
import com.library.service.PatronService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PatronServiceImpl implements PatronService {

    private static final Logger log = LoggerFactory.getLogger(PatronServiceImpl.class);

    private final PatronRepository patronRepository;
    private final LoanRepository loanRepository;

    public PatronServiceImpl(PatronRepository patronRepository, LoanRepository loanRepository) {
        this.patronRepository = patronRepository;
        this.loanRepository = loanRepository;
    }

    @Override
    public Patron registerPatron(CreatePatronRequest request) {
        patronRepository.findByEmail(request.getEmail()).ifPresent(p -> {
            throw new BusinessException("A patron with email '" + request.getEmail() + "' already exists.");
        });

        Patron patron = Patron.builder()
                .patronId(UUID.randomUUID().toString())
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .preferredGenres(request.getPreferredGenres() != null
                        ? request.getPreferredGenres() : new ArrayList<>())
                .memberSince(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        patronRepository.save(patron);
        log.info("Patron registered: id={}, email={}", patron.getPatronId(), patron.getEmail());
        return patron;
    }

    @Override
    public Patron getPatronById(String patronId) {
        return patronRepository.findById(patronId)
                .orElseThrow(() -> new ResourceNotFoundException("Patron not found: " + patronId));
    }

    @Override
    public List<Patron> getAllPatrons() {
        return patronRepository.findAll();
    }

    @Override
    public Patron updatePatron(String patronId, UpdatePatronRequest request) {
        Patron patron = getPatronById(patronId);

        if (request.getName() != null)           patron.setName(request.getName());
        if (request.getPhone() != null)          patron.setPhone(request.getPhone());
        if (request.getPreferredGenres() != null) patron.setPreferredGenres(request.getPreferredGenres());

        if (request.getEmail() != null && !request.getEmail().equals(patron.getEmail())) {
            patronRepository.findByEmail(request.getEmail()).ifPresent(other -> {
                throw new BusinessException("Email '" + request.getEmail() + "' is already in use.");
            });
            patron.setEmail(request.getEmail());
        }

        patron.setUpdatedAt(LocalDateTime.now());
        patronRepository.save(patron);
        log.info("Patron updated: id={}", patronId);
        return patron;
    }

    @Override
    public List<Loan> getBorrowingHistory(String patronId) {
        getPatronById(patronId); // validate existence
        return loanRepository.findByPatronId(patronId);
    }

    @Override
    public List<Loan> getActiveLoans(String patronId) {
        getPatronById(patronId);
        return loanRepository.findByPatronIdAndStatus(patronId, LoanStatus.ACTIVE);
    }
}
