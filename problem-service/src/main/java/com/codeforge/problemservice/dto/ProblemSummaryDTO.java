package com.codeforge.problemservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProblemSummaryDTO {
    private Long id;
    private String title;
    private String difficulty;



    // Getters & Setters
}

