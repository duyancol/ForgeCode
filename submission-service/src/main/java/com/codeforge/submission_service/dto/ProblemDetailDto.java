package com.codeforge.submission_service.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Data
@Getter
@Setter
public class ProblemDetailDto {
    private Long id;
    private String title;
    private String description;
    private String difficulty;
    private String sampleInput;
    private String sampleOutput;
    private String testCases;
    private String methodName;
    private String methodSignature;
    private String returnType;
    private List<ProblemTemplateDto> templates = new ArrayList<>();
}
