package com.codeforge.problemservice.repository;

import com.codeforge.problemservice.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
    List<Problem> findByDifficulty(String difficulty);
    List<Problem> findByTagsContaining(String tag);
    List<Problem> findByTagsContainingIgnoreCase(String tag);
}
