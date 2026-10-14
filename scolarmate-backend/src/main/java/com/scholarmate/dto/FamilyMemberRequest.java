package com.scholarmate.dto;
public record FamilyMemberRequest(String relationship, String name, String occupation, Double annualIncome, Boolean dependent, Integer beneficiaryCount) { }
