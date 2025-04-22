package com.codeforge.submission_service.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestResult {
    private String input;
    private String expectedOutput;
    private String actualOutput;
    private boolean passed;
}

