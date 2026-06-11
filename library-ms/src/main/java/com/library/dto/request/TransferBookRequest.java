package com.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TransferBookRequest {

    @NotBlank(message = "Book ID is required")
    private String bookId;

    @NotBlank(message = "Target branch ID is required")
    private String targetBranchId;
}
