package com.codeforge.submission_service.repository;


import com.codeforge.submission_service.dto.ExecutionTimeStats;
import com.codeforge.submission_service.model.Submission;
import com.codeforge.submission_service.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByUserId(String userId);
    List<Submission> findByProblemId(Long problemId);
    @Query(value = """
    SELECT execution_time_ms AS executionTimeMs, COUNT(*) AS count
    FROM submission
    WHERE problem_id = :problemId
      AND LOWER(language) = LOWER(:language)
      AND status = 'PASS'
      AND execution_time_ms IS NOT NULL
    GROUP BY execution_time_ms
    ORDER BY execution_time_ms
    """, nativeQuery = true)
    List<ExecutionTimeStats> countByExecutionTime(@Param("problemId") Long problemId,
                                                  @Param("language") String language);


}