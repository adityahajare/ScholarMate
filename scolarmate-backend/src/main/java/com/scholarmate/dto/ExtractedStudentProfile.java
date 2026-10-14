package com.scholarmate.dto;

public record ExtractedStudentProfile(
        String fullName,
        String course,
        String college,
        Double cgpa,
        Double familyIncome,
        String category,
        String state,
        Integer yearOfStudy) {
}