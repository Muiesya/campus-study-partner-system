package com.campus.partner.dto;

import jakarta.validation.constraints.NotNull;

public class CollaborationRequest {
    @NotNull
    private Long studentId1;
    @NotNull
    private Long studentId2;

    public Long getStudentId1() {
        return studentId1;
    }

    public void setStudentId1(Long studentId1) {
        this.studentId1 = studentId1;
    }

    public Long getStudentId2() {
        return studentId2;
    }

    public void setStudentId2(Long studentId2) {
        this.studentId2 = studentId2;
    }
}
