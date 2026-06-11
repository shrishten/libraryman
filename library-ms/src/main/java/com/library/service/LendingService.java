package com.library.service;

import com.library.dto.request.CheckoutRequest;
import com.library.model.Loan;

public interface LendingService {

    Loan checkoutBook(CheckoutRequest request);

    Loan returnBook(String loanId);

    Loan getLoanById(String loanId);
}
