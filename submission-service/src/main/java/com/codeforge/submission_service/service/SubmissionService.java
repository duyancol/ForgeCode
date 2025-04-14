package com.codeforge.submission_service.service;

import com.codeforge.submission_service.model.Submission;
import com.codeforge.submission_service.repository.SubmissionRepository;
import com.codeforge.submission_service.model.Submission;
import com.codeforge.submission_service.repository.SubmissionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SubmissionService {

    private final SubmissionRepository repo;

    public SubmissionService(SubmissionRepository repo) {
        this.repo = repo;
    }

    public Submission create(Submission submission) {
        submission.setStatus("PENDING");
        submission.setCreatedAt(LocalDateTime.now());
        return repo.save(submission);
    }

    public List<Submission> getAll() {
        return repo.findAll();
    }

    public List<Submission> getByUser(String userId) {
        return repo.findByUserId(userId);
    }

    public List<Submission> getByProblem(Long problemId) {
        return repo.findByProblemId(problemId);
    }

    public Optional<Submission> getById(Long id) {
        return repo.findById(id);
    }

    public Submission updateStatus(Long id, String status, String output, String errorMessage) {
        return repo.findById(id).map(sub -> {
            sub.setStatus(status);
            sub.setOutput(output);
            sub.setErrorMessage(errorMessage);
            return repo.save(sub);
        }).orElseThrow();
    }
}
