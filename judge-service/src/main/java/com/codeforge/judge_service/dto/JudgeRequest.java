package com.codeforge.judge_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class JudgeRequest {
    private String solutionCode;     // class Solution
    private String mainCode;         // class Main
    private String input;
    private String expectedOutput;
    private String language; // java, c, cpp
}
