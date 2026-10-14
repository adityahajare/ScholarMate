package com.scholarmate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationResponse {

    @Builder.Default
    private List<String> eligible = new ArrayList<>();

    @Builder.Default
    private List<String> possible = new ArrayList<>();

    @Builder.Default
    private List<String> unknown = new ArrayList<>();

    @Builder.Default
    private List<String> notEligible = new ArrayList<>();
}
