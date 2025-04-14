package com.codeforge.problemservice.repository;
import com.codeforge.problemservice.model.ProblemTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemTemplateRepository extends JpaRepository<ProblemTemplate, Long> {
    List<ProblemTemplate> findByProblemId(Long problemId);
}
