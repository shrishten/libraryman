package com.library.dto.request;

import lombok.Data;

@Data
public class UpdateBookRequest {
    private String title;
    private String author;
    private Integer publicationYear;
    private String genre;
}
