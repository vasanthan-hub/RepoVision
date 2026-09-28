package com.repovision.commit.api;

import com.repovision.commit.application.CommitService;
import com.repovision.commit.api.dto.CommitResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/repositories")
public class CommitController {

    private final CommitService commitService;

    public CommitController(
            CommitService commitService
    ) {
        this.commitService = commitService;
    }

    @GetMapping("/{repositoryId}/commits")
    public List<CommitResponse> getCommits(
            @PathVariable UUID repositoryId
    ) {
        return commitService.readCommits(repositoryId);
    }
}