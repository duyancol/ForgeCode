package com.codeforge.judge_service.controller;

import com.codeforge.judge_service.dto.JudgeRequest;
import com.codeforge.judge_service.dto.JudgeResponse;
import com.codeforge.judge_service.runer.DockerRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/judge")
public class JudgeController {

    @PostMapping
    public ResponseEntity<JudgeResponse> judge(@RequestBody JudgeRequest request) {
        try {
            JudgeResponse result = DockerRunner.run(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new JudgeResponse("ERROR", "", e.getMessage()));
        }
    }
}
