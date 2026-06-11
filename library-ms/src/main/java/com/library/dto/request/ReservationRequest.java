package com.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReservationRequest {

    @NotBlank(message = "Patron ID is required")
    private String patronId;

    @NotBlank(message = "ISBN is required")
    private String isbn;

    @NotBlank(message = "Branch ID is required")
    private String branchId;
}
