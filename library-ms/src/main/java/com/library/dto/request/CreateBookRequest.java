package com.library.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateBookRequest {

    @NotBlank(message = "ISBN is required")
    private String isbn;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    @Min(value = 1000, message = "Publication year must be a valid year")
    @Max(value = 2100, message = "Publication year must be a valid year")
    private int publicationYear;

    private String genre;

    @NotBlank(message = "Branch ID is required")
    private String branchId;
}
