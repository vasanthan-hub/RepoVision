package com.repovision.commit.application;

import com.repovision.commit.api.dto.CommitResponse;
import com.repovision.commit.domain.Commit;
import com.repovision.commit.domain.FileChange;
import com.repovision.commit.infrastructure.parser.CommitParser;
import com.repovision.commit.infrastructure.parser.ParsedCommit;
import com.repovision.git.GitClient;
import com.repovision.git.GitCommandResult;
import com.repovision.repository.application.RepositoryStore;
import com.repovision.repository.domain.Repository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CommitService {

    private final RepositoryStore repositoryStore;
    private final CommitStore commitStore;
    private final FileChangeStore fileChangeStore;
    private final GitClient gitClient;
    private final CommitParser commitParser;

    public CommitService(
            RepositoryStore repositoryStore,
            CommitStore commitStore,
            FileChangeStore fileChangeStore,
            GitClient gitClient,
            CommitParser commitParser
    ) {
        this.repositoryStore = repositoryStore;
        this.commitStore = commitStore;
        this.fileChangeStore = fileChangeStore;
        this.gitClient = gitClient;
        this.commitParser = commitParser;
    }

    public List<CommitResponse> readCommits(
            UUID repositoryId
    ) {

        Repository repository =
                repositoryStore.findById(repositoryId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Repository not found"
                                )
                        );

        GitCommandResult result =
                gitClient.execute(
                        repository.getLocalPath(),
                        List.of(
                                "log",
                                "--pretty=format:%H%x1f%an%x1f%ae%x1f%aI%x1f%s",
                                "--numstat"
                        )
                );

        List<ParsedCommit> parsedCommits =
                commitParser.parse(
                        repositoryId,
                        result
                );

        /*
         * Extract the Commit objects from the
         * parsed results.
         */
        List<Commit> commits =
                parsedCommits.stream()
                        .map(ParsedCommit::commit)
                        .toList();

        /*
         * Save/update commits first.
         *
         * The returned commits contain the actual
         * persisted Commit IDs.
         */
        List<Commit> savedCommits =
                commitStore.saveAll(commits);

        /*
         * Create a lookup:
         *
         * Git commit hash → persisted Commit
         *
         * Example:
         *
         * abc123 → Commit(id = 111)
         */
        Map<String, Commit> savedCommitsByHash =
                savedCommits.stream()
                        .collect(
                                Collectors.toMap(
                                        Commit::getHash,
                                        Function.identity()
                                )
                        );

        /*
         * Rebuild FileChanges using the actual
         * persisted Commit ID.
         */
        List<FileChange> fileChanges =
                parsedCommits.stream()
                        .flatMap(parsedCommit -> {

                            Commit savedCommit =
                                    savedCommitsByHash.get(
                                            parsedCommit
                                                    .commit()
                                                    .getHash()
                                    );

                            if (savedCommit == null) {
                                throw new IllegalStateException(
                                        "Persisted commit not found for hash: "
                                                + parsedCommit
                                                .commit()
                                                .getHash()
                                );
                            }

                            return parsedCommit
                                    .fileChanges()
                                    .stream()
                                    .map(fileChange ->
                                            new FileChange(
                                                    fileChange.getId(),
                                                    savedCommit.getId(),
                                                    fileChange.getFilePath(),
                                                    fileChange.getAdditions(),
                                                    fileChange.getDeletions()
                                            )
                                    );
                        })
                        .toList();

        /*
         * Save/update FileChanges.
         */
        fileChangeStore.saveAll(
                fileChanges
        );

        /*
         * Return commits through the API.
         */
        return savedCommits.stream()
                .map(CommitResponse::from)
                .toList();
    }
}