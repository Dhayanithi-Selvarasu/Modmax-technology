package com.novadigital.chat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ChatRequest(
    @NotBlank @Size(max=1000) String message,
    @Size(max=10) List<@Valid HistoryItem> history
) {
    public record HistoryItem(
        @NotBlank String role,
        @NotBlank @Size(max=4000) String content
    ) {}
}
