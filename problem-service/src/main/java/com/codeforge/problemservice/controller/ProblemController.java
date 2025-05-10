package com.codeforge.problemservice.controller;

import com.codeforge.problemservice.dto.ProblemDetailDto;
import com.codeforge.problemservice.dto.ProblemSummaryDTO;
import com.codeforge.problemservice.dto.ProblemTemplateDto;
import com.codeforge.problemservice.model.Problem;
import com.codeforge.problemservice.service.ProblemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@CrossOrigin(origins = "*")

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemService service;
// get
    public ProblemController(ProblemService service) {
        this.service = service;
    }

    // GET all problems
//    @GetMapping
//    public List<ProblemDetailDto> getAll() {
//        return service.getAllProblems();
//    }
    @GetMapping
    public List<ProblemSummaryDTO> getAll() {
        return service.getAllProblemSummaries();
    }
    // GET problem by ID with full detail including templates
    @GetMapping("/{id}")
    public ResponseEntity<ProblemDetailDto> getProblemById(@PathVariable Long id) {
        return service.getDetailById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    // GET by difficulty level
    @GetMapping("/difficulty/{level}")
    public List<Problem> getByDifficulty(@PathVariable String level) {
        return service.getByDifficulty(level);
    }
    @GetMapping("/tag")
    public List<String> getAllTags() {
        return service.getAllTags();
    }
    // GET by tag
    @GetMapping("/tag/{tag}")
    public List<Problem> getByTag(@PathVariable String tag) {
        return service.getByTag(tag);
    }

    // CREATE problem
    @PostMapping
    public Problem create(@RequestBody Problem p) {
        return service.create(p);
    }

    // UPDATE problem
    @PutMapping("/{id}")
    public Problem update(@PathVariable Long id, @RequestBody Problem p) {
        return service.update(id, p);
    }

    // DELETE problem
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
