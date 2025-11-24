package com.campus.partner.dto;

import java.util.List;

public class RecommendationEntry {
    private Long studentId;
    private String studentName;
    private double score;
    private List<String> reasons;

    public RecommendationEntry(Long studentId, String studentName, double score, List<String> reasons) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.score = score;
        this.reasons = reasons;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public double getScore() {
        return score;
    }

    public List<String> getReasons() {
        return reasons;
    }
}
