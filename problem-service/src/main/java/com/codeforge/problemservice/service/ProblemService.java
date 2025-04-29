package com.codeforge.problemservice.service;

import com.codeforge.problemservice.dto.ProblemDetailDto;
import com.codeforge.problemservice.dto.ProblemTemplateDto;
import com.codeforge.problemservice.model.Problem;
import com.codeforge.problemservice.repository.ProblemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProblemService {
    private final ProblemRepository repo;
    ObjectMapper mapper = new ObjectMapper();
    public ProblemService(ProblemRepository repo) {
        this.repo = repo;
    }

    public List<ProblemDetailDto> getAllProblems() {
        return repo.findAll().stream().map(p -> {
            List<ProblemTemplateDto> templates = p.getTemplates().stream()
                    .map(t -> new ProblemTemplateDto(t.getLanguage(), t.getMainCode()))
                    .toList();
            String testCaseJson = "";
            try {
                testCaseJson = mapper.writeValueAsString(p.getTestCases()); // 👈 chuyển List<TestCase> về JSON
            } catch (Exception e) {
                e.printStackTrace();
            }

            return new ProblemDetailDto(
                    p.getId(),
                    p.getTitle(),
                    p.getDescription(),
                    p.getDifficulty(),
                    p.getTags(),
                    p.getSampleInput(),
                    p.getSampleOutput(),
                    testCaseJson,
                    p.getMethodName(),
                    p.getMethodSignature(),
                    p.getReturnType(),
                    templates,
                    p.getExamples()
            );
        }).toList();
    }

    public List<String> getAllTags() {
        return repo.findAllTags();
    }
    public Optional<Problem> getById(Long id) {
        return repo.findById(id);
    }

    public List<Problem> getByDifficulty(String d) {
        return repo.findByDifficulty(d);
    }

    public List<Problem> getByTag(String tag) {
        return repo.findByTagsContaining(tag);
    }

    public Problem create(Problem p) {
        return repo.save(p);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public Problem update(Long id, Problem newP) {
        return repo.findById(id).map(p -> {
            p.setTitle(newP.getTitle());
            p.setDescription(newP.getDescription());
            p.setDifficulty(newP.getDifficulty());
            p.setTags(newP.getTags());
            p.setSampleInput(newP.getSampleInput());
            p.setSampleOutput(newP.getSampleOutput());
            p.setTestCases(newP.getTestCases());
            p.setMethodName(newP.getMethodName());
            p.setMethodSignature(newP.getMethodSignature());
            p.setReturnType(newP.getReturnType());
            return repo.save(p);
        }).orElseThrow();
    }

    // ✅ Hàm bổ sung: trả DTO chi tiết bao gồm templates
    public Optional<ProblemDetailDto> getDetailById(Long id) {
        return repo.findById(id).map(p -> {
            List<ProblemTemplateDto> templates = p.getTemplates().stream()
                    .map(t -> new ProblemTemplateDto(t.getLanguage(), t.getMainCode()))
                    .toList();
            String testCaseJson = "";
            try {
                testCaseJson = mapper.writeValueAsString(p.getTestCases()); // 👈 chuyển List<TestCase> về JSON
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new ProblemDetailDto(
                    p.getId(),
                    p.getTitle(),
                    p.getDescription(),
                    p.getDifficulty(),
                    p.getTags(),
                    p.getSampleInput(),
                    p.getSampleOutput(),
                    testCaseJson,
                    p.getMethodName(),
                    p.getMethodSignature(),
                    p.getReturnType(),
                    templates,
                    p.getExamples()
            );
        });
    }

    // ✅ Hàm lấy danh sách bài theo tag + kèm templates
    public List<ProblemDetailDto> getByTagWithTemplate(String tag) {
        return repo.findByTagsContainingIgnoreCase(tag).stream().map(p -> {
            List<ProblemTemplateDto> templates = p.getTemplates().stream()
                    .map(t -> new ProblemTemplateDto(t.getLanguage(), t.getMainCode()))
                    .toList();
            String testCaseJson = "";
            try {
                testCaseJson = mapper.writeValueAsString(p.getTestCases()); // 👈 chuyển List<TestCase> về JSON
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new ProblemDetailDto(
                    p.getId(),
                    p.getTitle(),
                    p.getDescription(),
                    p.getDifficulty(),
                    p.getTags(),
                    p.getSampleInput(),
                    p.getSampleOutput(),
                    testCaseJson,
                    p.getMethodName(),
                    p.getMethodSignature(),
                    p.getReturnType(),
                    templates,
                    p.getExamples()
            );
        }).collect(Collectors.toList());
    }
}
