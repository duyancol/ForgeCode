package com.codeforge.submission_service.controller;

import com.codeforge.submission_service.dto.ProblemDetailDto;
import com.codeforge.submission_service.dto.ProblemTemplateDto;
import com.codeforge.submission_service.model.Submission;
import com.codeforge.submission_service.service.CodeBuilderService;
import com.codeforge.submission_service.service.SubmissionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    @Autowired
    private SubmissionService service;

    @Autowired
    private CodeBuilderService codeBuilderService;

    @PostMapping
    public ResponseEntity<Submission> submit(@RequestBody Submission submission) {
        Submission saved = service.create(submission);

        try {
            RestTemplate restTemplate = new RestTemplate();
            String judgeUrl = "http://localhost:8082/api/judge";
            String problemUrl = "http://localhost:8080/api/problems/" + submission.getProblemId();

            ProblemDetailDto problemDto = restTemplate.getForObject(problemUrl, ProblemDetailDto.class);

            System.out.println("🚨 methodName = " + problemDto.getMethodName());
            System.out.println("🚨 methodSignature = " + problemDto.getMethodSignature());
            System.out.println("🚨 returnType = " + problemDto.getReturnType());

            ObjectMapper objectMapper = new ObjectMapper();
            List<TestCase> testCases = objectMapper.readValue(problemDto.getTestCases(), new TypeReference<>() {});
            String rawInput = testCases.get(0).input;
            String formattedInput = normalizeInput(rawInput);
            String expectedOutput = testCases.get(0).output;

            System.out.println("📥 formattedInput gửi xuống: " + formattedInput);
            if (problemDto.getTemplates() == null) {
                throw new IllegalStateException("Templates is null in problemDto");
            }

            String language = submission.getLanguage().toLowerCase();
            String mainCode = problemDto.getTemplates().stream()
                    .filter(t -> t.language().equalsIgnoreCase(language))
                    .map(ProblemTemplateDto::mainCode)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No mainCode found for language: " + language));

            // 🔧 Build solutionCode tùy theo ngôn ngữ
            String solutionCode;
            if (language.equals("java")) {
                solutionCode = codeBuilderService.buildFullCode(submission.getCode(), problemDto);
            } else if (language.equals("c")) {
                solutionCode = codeBuilderService.buildFullCodeC(submission.getCode(), problemDto);
            } else {
                throw new IllegalArgumentException("Unsupported language: " + language);
            }

            System.out.println("✅ Solution code:\n" + solutionCode);
            System.out.println("✅ Main code from DB:\n" + mainCode);

            var request = new JudgeRequest(solutionCode, mainCode, formattedInput, expectedOutput, language);
            var response = restTemplate.postForObject(judgeUrl, request, JudgeResponse.class);

            service.updateStatus(saved.getId(), response.status, response.output, response.error);

        } catch (Exception e) {
            e.printStackTrace();
            service.updateStatus(saved.getId(), "ERROR", "", e.getMessage());
        }

        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public List<Submission> getAll() {
        return service.getAll();
    }

    @GetMapping("/user/{userId}")
    public List<Submission> getByUser(@PathVariable String userId) {
        return service.getByUser(userId);
    }

    @GetMapping("/problem/{problemId}")
    public List<Submission> getByProblem(@PathVariable Long problemId) {
        return service.getByProblem(problemId);
    }

    private String normalizeInput(String rawInput) {
        try {
            String[] lines = rawInput.trim().split("\\r?\\n");
            if (lines.length == 2) {
                return "[" + lines[0].trim() + "]," + lines[1].trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rawInput.trim();
    }

    // --- DTO nội bộ ---
    record JudgeRequest(String solutionCode, String mainCode, String input, String expectedOutput, String language) {}
    record JudgeResponse(String status, String output, String error) {}
    record TestCase(String input, String output) {}
}
