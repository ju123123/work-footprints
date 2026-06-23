package com.workfootprint.dto.project;

import jakarta.validation.constraints.NotBlank;

public class CreateProjectTaskRequest {
    @NotBlank
    private String name;
    private Long moduleId;
    @NotBlank
    private String status;
    private String progressSummary;
    private String riskSummary;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getModuleId() {
        return moduleId;
    }

    public void setModuleId(Long moduleId) {
        this.moduleId = moduleId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProgressSummary() {
        return progressSummary;
    }

    public void setProgressSummary(String progressSummary) {
        this.progressSummary = progressSummary;
    }

    public String getRiskSummary() {
        return riskSummary;
    }

    public void setRiskSummary(String riskSummary) {
        this.riskSummary = riskSummary;
    }
}
