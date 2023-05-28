package com.practice.catalog.catalogservice.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record Book(
        @NotBlank(message = "The book ISBN must not be blank")
        @Pattern(
                regexp = "^([0-9]{10} | [0-9]{13})$",
                message = "The isbn format must be valid. "
        )
        String isbn,

        @NotBlank(message = "The book ISBN must not be blank")
        String title,

        @NotBlank(message = "The book ISBN must not be blank")
        String author,

        @NotNull(message = "the book price must be defined .")
                @Positive(
                        message = "The book price must be greater than zero"
                )
        Double price) {}
