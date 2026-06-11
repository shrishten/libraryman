package com.library.service;

import com.library.dto.request.CreateBranchRequest;
import com.library.dto.request.TransferBookRequest;
import com.library.model.Branch;
import com.library.model.Book;

import java.util.List;

public interface BranchService {

    Branch createBranch(CreateBranchRequest request);

    Branch getBranchById(String branchId);

    List<Branch> getAllBranches();

    Book transferBook(TransferBookRequest request);
}
