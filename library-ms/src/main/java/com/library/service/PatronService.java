package com.library.service;

import com.library.dto.request.CreatePatronRequest;
import com.library.dto.request.UpdatePatronRequest;
import com.library.model.Loan;
import com.library.model.Patron;

import java.util.List;

public interface PatronService {

    Patron registerPatron(CreatePatronRequest request);

    Patron getPatronById(String patronId);

    List<Patron> getAllPatrons();

    Patron updatePatron(String patronId, UpdatePatronRequest request);

    List<Loan> getBorrowingHistory(String patronId);

    List<Loan> getActiveLoans(String patronId);
}
