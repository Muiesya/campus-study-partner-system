package com.campus.partner.dto;

import jakarta.validation.constraints.NotBlank;

public class StudentRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String major;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }
}
