package com.library.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.util.List;

@Data
public class UpdatePatronRequest {
    private String name;

    @Email(message = "Email must be valid")
    private String email;

    private String phone;

    private List<String> preferredGenres;
}
