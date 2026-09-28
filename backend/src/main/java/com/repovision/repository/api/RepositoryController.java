package com.repovision.repository.api;

import com.repovision.repository.application.RepositoryService;
import com.repovision.repository.api.dto.RegisterRepositoryRequest;
import com.repovision.repository.api.dto.RepositoryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/repositories")
public class RepositoryController {

    private final RepositoryService repositoryService;

    public RepositoryController(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RepositoryResponse registerRepository(
            @Valid @RequestBody RegisterRepositoryRequest request
    ) {
        return repositoryService.registerRepository(request);
    }

    @GetMapping
    public java.util.List<RepositoryResponse> getAllRepositories() {
        return repositoryService.getAllRepositories();
    }

    @GetMapping("/{id}")
    public RepositoryResponse getRepository(@PathVariable java.util.UUID id) {
        return repositoryService.getRepository(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRepository(@PathVariable java.util.UUID id) {
        repositoryService.deleteRepository(id);
    }
}