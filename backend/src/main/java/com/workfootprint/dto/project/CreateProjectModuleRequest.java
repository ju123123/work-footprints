package com.workfootprint.dto.project;

import jakarta.validation.constraints.NotBlank;

public class CreateProjectModuleRequest {
    @NotBlank
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

