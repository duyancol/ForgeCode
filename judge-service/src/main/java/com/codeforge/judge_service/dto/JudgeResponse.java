package com.codeforge.judge_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JudgeResponse {
    private String status;       // PASS, FAIL, ERROR, TLE,...
    private String output;
    private String error;
    private long executionTimeMs; // Thêm field mới

}
