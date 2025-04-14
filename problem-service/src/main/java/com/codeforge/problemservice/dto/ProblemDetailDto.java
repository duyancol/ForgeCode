package com.codeforge.problemservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProblemDetailDto {
    private Long id;
    private String title;
    private String description;
    private String difficulty;
    private List<String> tags;
    private String sampleInput;
    private String sampleOutput;
    private String testCases; // JSON test case
    private String methodName;
    private String returnType;
    private String methodSignature;
    private List<ProblemTemplateDto> templates = List.of();
    private String examples; // Lưu dưới dạng Markdown hoặc đoạn text

}
