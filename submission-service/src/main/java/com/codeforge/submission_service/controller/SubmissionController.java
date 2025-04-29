package com.codeforge.submission_service.controller;

import com.codeforge.submission_service.dto.ExecutionTimeStats;
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

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*")


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
            String judgeUrl = "http://14.225.205.6:8082/api/judge";
            //String judgeUrl = "http://localhost:8082/api/judge";
            String problemUrl = "http://14.225.205.6:8080/api/problems/" + submission.getProblemId();

            ProblemDetailDto problemDto = restTemplate.getForObject(problemUrl, ProblemDetailDto.class);

            ObjectMapper objectMapper = new ObjectMapper();
            List<TestCase> testCases = objectMapper.readValue(problemDto.getTestCases(), new TypeReference<>() {});

            if (problemDto.getTemplates() == null) {
                throw new IllegalStateException("Templates is null in problemDto");
            }

            String language = submission.getLanguage().toLowerCase();
            String mainCode = problemDto.getTemplates().stream()
                    .filter(t -> t.language().equalsIgnoreCase(language))
                    .map(ProblemTemplateDto::mainCode)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No mainCode found for language: " + language));

            // 🔧 Build full solution code
            String solutionCode = switch (language) {
                case "java" -> codeBuilderService.buildFullCode(submission.getCode(), problemDto);
                case "c" -> codeBuilderService.buildFullCodeC(submission.getCode(), problemDto);
                case "cpp" -> codeBuilderService.buildFullCodeCpp(submission.getCode(), problemDto);
                case "python" -> codeBuilderService.buildFullCodePython(submission.getCode(), problemDto);
                case "csharp" -> codeBuilderService.buildFullCodeCSharp(submission.getCode(), problemDto);
                default -> throw new IllegalArgumentException("Unsupported language: " + language);
            };

            // 🔁 Test each case
            List<TestResult> testResults = new ArrayList<>();
            String firstError = null;

            for (TestCase testCase : testCases) {
                String formattedInput = normalizeInput(testCase.input);
                JudgeRequest request = new JudgeRequest(solutionCode, mainCode, formattedInput, testCase.output, language);

                JudgeResponse response = restTemplate.postForObject(judgeUrl, request, JudgeResponse.class);
                if (response == null) continue;

                String actual = response.output != null ? response.output.trim() : "";
                String expected = testCase.output.trim();
                boolean passed = actual.equals(expected);
                long execTimeMs = response.executionTimeMs() != null ? response.executionTimeMs() : 0L;

                if (!passed && firstError == null && response.error != null && !response.error.isBlank()) {
                    firstError = response.error.trim();
                }

                testResults.add(new TestResult(
                        testCase.input,
                        expected,
                        actual,
                        passed,
                        response.error != null ? response.error.trim() : null,
                        execTimeMs
                ));
            }

            long passedCount = testResults.stream().filter(TestResult::passed).count();
            String finalStatus = passedCount == testCases.size() ? "PASS" : "FAIL";

            service.updateStatus(saved.getId(), finalStatus, testResults.toString(), firstError);
            Long firstExecTime = testResults.isEmpty() ? null : testResults.get(0).executionTimeMs();
            saved.setExecutionTimeMs(firstExecTime);
            service.save(saved);
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
    @GetMapping("/stats")
    public List<ExecutionTimeStats> getExecutionTimeStats(
            @RequestParam Long problemId,
            @RequestParam String language) {
        return service.getExecutionTimeStats(problemId, language);
    }
    @GetMapping("/user/{userId}/accepted-problems")
    public List<Long> getUserAcceptedProblems(@PathVariable String userId) {
        return service.getAcceptedProblems(userId);
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
    record JudgeResponse(String status, String output, String error, Long executionTimeMs) {}
    record TestCase(String input, String output) {}
    record TestResult(String input, String expectedOutput, String actualOutput, boolean passed, String errorMessage, Long executionTimeMs) {}
}
