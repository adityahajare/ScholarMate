package com.scholarmate.service;

import com.scholarmate.dto.ExtractedStudentProfile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class StudentProfileDocumentParser {

    private static final List<String> INDIAN_STATES = List.of(
            "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
            "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka",
            "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram",
            "Nagaland", "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu",
            "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal"
    );

    public ExtractedStudentProfile parse(String text) {
        return new ExtractedStudentProfile(
                labeledValue(text, "(?:student\\s+)?name"),
                labeledValue(text, "course|program|degree"),
                labeledValue(text, "college|institution|university"),
                numericValue(text, "(?:CGPA|GPA)"),
                numericValue(text, "(?:(?:annual|family|household)\\s+){0,2}income"),
                firstMatch(text, "(?i)\\b(General|EWS|OBC(?:-NCL)?|SC|ST)\\b"),
                findState(text),
                yearOfStudy(text)
        );
    }

    private String labeledValue(String text, String labels) {
        Matcher matcher = Pattern.compile(
                "(?im)^\\s*(?:" + labels + ")\\s*[:=-]\\s*(.+?)\\s*$")
                .matcher(text);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private Double numericValue(String text, String labelPattern) {
        Matcher matcher = Pattern.compile(
                "(?i)\\b" + labelPattern + "\\b\\s*[:=-]?\\s*(?:INR|Rs\\.?|₹)?\\s*([0-9][0-9,]*(?:\\.[0-9]+)?)")
                .matcher(text);
        if (!matcher.find()) {
            return null;
        }
        try {
            return Double.parseDouble(matcher.group(1).replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String firstMatch(String text, String expression) {
        Matcher matcher = Pattern.compile(expression).matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String findState(String text) {
        return INDIAN_STATES.stream()
                .filter(state -> Pattern.compile("(?i)\\b" + Pattern.quote(state) + "\\b")
                        .matcher(text).find())
                .findFirst()
                .orElse(null);
    }

    private Integer yearOfStudy(String text) {
        Matcher matcher = Pattern.compile(
                "(?i)\\b(?:year\\s+of\\s+study|current\\s+year|study\\s+year)\\s*[:=-]?\\s*(1[0-2]|[1-9])(?:st|nd|rd|th)?\\b")
                .matcher(text);
        if (!matcher.find()) {
            return null;
        }
        try {
            return Integer.valueOf(matcher.group(1));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}