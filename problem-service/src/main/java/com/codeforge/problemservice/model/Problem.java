package com.codeforge.problemservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import  com.codeforge.problemservice.model.TestCase;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String difficulty;

    @ElementCollection
    private List<String> tags;

    private String sampleInput;
    private String sampleOutput;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "problem_testcase", joinColumns = @JoinColumn(name = "problem_id"))
    private List<TestCase> testCases = new ArrayList<>();


    private String methodName;
    private String returnType;
    private String methodSignature; // ví dụ: "int[] nums, int target"
    @Lob
    private String templateCode;    // code mẫu, ví dụ như class Solution có sẵn

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProblemTemplate> templates = new ArrayList<>();
    @Column(columnDefinition = "LONGTEXT")
    private String examples; // Lưu dưới dạng Markdown hoặc đoạn text


}
