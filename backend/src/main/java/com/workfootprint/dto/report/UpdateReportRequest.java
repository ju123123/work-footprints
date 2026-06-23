package com.workfootprint.dto.report;

import jakarta.validation.constraints.NotBlank;

public class UpdateReportRequest {
    @NotBlank
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

