package com.repovision.repository.application;

import com.repovision.common.exception.InvalidRepositoryException;
import com.repovision.git.GitClient;
import com.repovision.git.GitCommandResult;
import com.repovision.repository.domain.Repository;
import com.repovision.repository.api.dto.RegisterRepositoryRequest;
import com.repovision.repository.api.dto.RepositoryResponse;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class RepositoryService {

    private final RepositoryStore repositoryStore;
    private final GitClient gitClient;

    public RepositoryService(
            RepositoryStore repositoryStore,
            GitClient gitClient
    ) {
        this.repositoryStore = repositoryStore;
        this.gitClient = gitClient;
    }

    public RepositoryResponse registerRepository(
            RegisterRepositoryRequest request
    ) {

        Path repositoryPath = Path.of(request.localPath());

        GitCommandResult result = gitClient.execute(
                repositoryPath,
                List.of(
                        "rev-parse",
                        "--is-inside-work-tree"
                )
        );

        if (!result.isSuccessful()) {
            throw new InvalidRepositoryException(
                    "The specified path is not a Git repository"
            );
        }

        Repository repository = new Repository(
                UUID.randomUUID(),
                request.name(),
                repositoryPath,
                request.defaultBranch()
        );

        Repository savedRepository =
                repositoryStore.save(repository);

        return RepositoryResponse.from(savedRepository);
    }

    public List<RepositoryResponse> getAllRepositories() {
        return repositoryStore.findAll()
                .stream()
                .map(RepositoryResponse::from)
                .toList();
    }

    public RepositoryResponse getRepository(UUID id) {
        return repositoryStore.findById(id)
                .map(RepositoryResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Repository not found with id: " + id));
    }

    public Repository findEntityById(UUID id) {
        return repositoryStore.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Repository not found with id: " + id));
    }

    public void updateStatus(UUID id, com.repovision.repository.domain.AnalysisStatus status) {
        Repository repository = findEntityById(id);
        if (status == com.repovision.repository.domain.AnalysisStatus.ANALYZING) {
            if (repository.getAnalysisStatus() != com.repovision.repository.domain.AnalysisStatus.ANALYZING) {
                repository.startAnalysis();
            }
        } else if (status == com.repovision.repository.domain.AnalysisStatus.COMPLETED) {
            if (repository.getAnalysisStatus() != com.repovision.repository.domain.AnalysisStatus.ANALYZING) {
                repository.startAnalysis();
            }
            repository.markAnalysisCompleted();
        } else if (status == com.repovision.repository.domain.AnalysisStatus.FAILED) {
            if (repository.getAnalysisStatus() != com.repovision.repository.domain.AnalysisStatus.ANALYZING) {
                repository.startAnalysis();
            }
            repository.markAnalysisFailed();
        }
        repositoryStore.save(repository);
    }

    public void deleteRepository(UUID id) {
        findEntityById(id);
        repositoryStore.deleteById(id);
    }
}