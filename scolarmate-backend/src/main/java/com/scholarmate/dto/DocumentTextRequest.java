package com.scholarmate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DocumentTextRequest(
        @NotBlank @Size(max = 100000) String text) {
}