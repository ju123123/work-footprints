package com.workfootprint.dto.record;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class CreateWorkRecordRequest {
    @NotBlank
    private String rawContent;
    private LocalDate recordDate;

    public String getRawContent() {
        return rawContent;
    }

    public void setRawContent(String rawContent) {
        this.rawContent = rawContent;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }
}

