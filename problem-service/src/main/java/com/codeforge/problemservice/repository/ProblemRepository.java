package com.codeforge.problemservice.repository;

import com.codeforge.problemservice.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
    List<Problem> findByDifficulty(String difficulty);
    List<Problem> findByTagsContaining(String tag);
    List<Problem> findByTagsContainingIgnoreCase(String tag);
    @Query(value = "SELECT DISTINCT tags FROM problem_tags", nativeQuery = true)
    List<String> findAllTags();

    // Tìm theo tag
    @Query(value = "SELECT problem_id FROM problem_tags WHERE tags = :tag", nativeQuery = true)
    List<Long> findProblemIdsByTag(String tag);

}
