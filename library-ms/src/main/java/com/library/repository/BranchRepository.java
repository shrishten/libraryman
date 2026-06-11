package com.library.repository;

import com.library.model.Branch;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory repository for Branch entities.
 */
@Repository
public class BranchRepository {

    private final Map<String, Branch> store = new ConcurrentHashMap<>();

    public Branch save(Branch branch) {
        store.put(branch.getBranchId(), branch);
        return branch;
    }

    public Optional<Branch> findById(String branchId) {
        return Optional.ofNullable(store.get(branchId));
    }

    public List<Branch> findAll() {
        return new ArrayList<>(store.values());
    }

    public boolean existsById(String branchId) {
        return store.containsKey(branchId);
    }

    public void deleteById(String branchId) {
        store.remove(branchId);
    }
}
