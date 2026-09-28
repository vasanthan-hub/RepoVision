package com.repovision.intelligence.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/repositories")
public class RepositoryHealthController {

    private final RepositoryHealthService repositoryHealthService;

    public RepositoryHealthController(RepositoryHealthService repositoryHealthService) {
        this.repositoryHealthService = repositoryHealthService;
    }

    @GetMapping("/{repositoryId}/intelligence/health")
    public RepositoryHealthResponse getHealth(@PathVariable UUID repositoryId) {
        return repositoryHealthService.calculateHealth(repositoryId);
    }
}
