package com.library.controller;

import com.library.dto.request.CreateBranchRequest;
import com.library.dto.request.TransferBookRequest;
import com.library.model.Book;
import com.library.model.Branch;
import com.library.service.BranchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for multi-branch management.
 *
 * Base path: /api/branches
 */
@RestController
@RequestMapping("/api/branches")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    /** Create a new library branch. */
    @PostMapping
    public ResponseEntity<Branch> createBranch(@Valid @RequestBody CreateBranchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(branchService.createBranch(request));
    }

    /** Get a branch by ID. */
    @GetMapping("/{branchId}")
    public ResponseEntity<Branch> getBranch(@PathVariable String branchId) {
        return ResponseEntity.ok(branchService.getBranchById(branchId));
    }

    /** List all branches. */
    @GetMapping
    public ResponseEntity<List<Branch>> getAllBranches() {
        return ResponseEntity.ok(branchService.getAllBranches());
    }

    /** Transfer a book copy from its current branch to a target branch. */
    @PostMapping("/transfer")
    public ResponseEntity<Book> transferBook(@Valid @RequestBody TransferBookRequest request) {
        return ResponseEntity.ok(branchService.transferBook(request));
    }
}
