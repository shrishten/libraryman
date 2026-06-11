package com.library.service.impl;

import com.library.dto.request.CreateBranchRequest;
import com.library.dto.request.TransferBookRequest;
import com.library.enums.BookStatus;
import com.library.exception.BusinessException;
import com.library.exception.ResourceNotFoundException;
import com.library.model.Book;
import com.library.model.Branch;
import com.library.observer.LibraryEvent;
import com.library.observer.LibraryEventPublisher;
import com.library.repository.BookRepository;
import com.library.repository.BranchRepository;
import com.library.service.BranchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BranchServiceImpl implements BranchService {

    private static final Logger log = LoggerFactory.getLogger(BranchServiceImpl.class);

    private final BranchRepository branchRepository;
    private final BookRepository bookRepository;
    private final LibraryEventPublisher eventPublisher;

    public BranchServiceImpl(BranchRepository branchRepository,
                              BookRepository bookRepository,
                              LibraryEventPublisher eventPublisher) {
        this.branchRepository = branchRepository;
        this.bookRepository = bookRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Branch createBranch(CreateBranchRequest request) {
        Branch branch = Branch.builder()
                .branchId(UUID.randomUUID().toString())
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .createdAt(LocalDateTime.now())
                .build();

        branchRepository.save(branch);
        log.info("Branch created: id={}, name={}", branch.getBranchId(), branch.getName());
        return branch;
    }

    @Override
    public Branch getBranchById(String branchId) {
        return branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found: " + branchId));
    }

    @Override
    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }

    @Override
    public Book transferBook(TransferBookRequest request) {
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + request.getBookId()));

        Branch targetBranch = branchRepository.findById(request.getTargetBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Target branch not found: " + request.getTargetBranchId()));

        if (book.getBranchId().equals(request.getTargetBranchId())) {
            throw new BusinessException("Book is already at the target branch.");
        }

        if (BookStatus.BORROWED.equals(book.getStatus())) {
            throw new BusinessException("Cannot transfer a book that is currently borrowed.");
        }

        // Move from source to target branch
        branchRepository.findById(book.getBranchId()).ifPresent(source -> {
            source.removeBook(book.getBookId());
            branchRepository.save(source);
        });

        targetBranch.addBook(book.getBookId());
        branchRepository.save(targetBranch);

        String previousBranch = book.getBranchId();
        book.setBranchId(request.getTargetBranchId());
        book.setUpdatedAt(LocalDateTime.now());
        bookRepository.save(book);

        eventPublisher.publish(LibraryEvent.builder()
                .type(LibraryEvent.Type.BOOK_TRANSFERRED)
                .bookId(book.getBookId())
                .isbn(book.getIsbn())
                .branchId(request.getTargetBranchId())
                .payload("Transferred from branch: " + previousBranch)
                .occurredAt(LocalDateTime.now())
                .build());

        log.info("Book transferred: bookId={}, from={}, to={}",
                book.getBookId(), previousBranch, request.getTargetBranchId());
        return book;
    }
}
