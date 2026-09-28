package com.repovision.commit.application;

import com.repovision.commit.api.dto.CommitResponse;
import com.repovision.commit.infrastructure.parser.CommitParser;
import com.repovision.git.GitClient;
import com.repovision.git.GitCommandResult;
import com.repovision.repository.application.RepositoryStore;
import com.repovision.repository.domain.Repository;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommitServiceTest {

    @Test
    void shouldReadCommitsFromGitRepository() {

        // Arrange

        RepositoryStore repositoryStore =
                mock(RepositoryStore.class);

        GitClient gitClient =
                mock(GitClient.class);
        CommitStore commitStore =
                mock(CommitStore.class);
        FileChangeStore fileChangeStore =
                mock(FileChangeStore.class);

        CommitParser commitParser =
                new CommitParser();

        CommitService commitService =
                new CommitService(
                        repositoryStore,
                        commitStore,
                        fileChangeStore,
                        gitClient,
                        commitParser
                );

        UUID repositoryId =
                UUID.randomUUID();

        Path repositoryPath =
                Path.of("C:\\enterprise-platform");

        Repository repository =
                new Repository(
                        repositoryId,
                        "enterprise-platform",
                        repositoryPath,
                        "main"
                );

        when(repositoryStore.findById(repositoryId))
                .thenReturn(Optional.of(repository));

        String gitOutput =
                "6837b869d3ae40c9a54866eff680d17c81fa9ab9\u001F"
                        + "vasanthan-hub\u001F"
                        + "vasanthanr2004@gmail.com\u001F"
                        + "2026-06-28T09:47:51+05:30\u001F"
                        + "feat: implemented activity logs";

        GitCommandResult gitResult =
                new GitCommandResult(
                        0,
                        gitOutput,
                        ""
                );

        when(gitClient.execute(
                eq(repositoryPath),
                anyList()
        )).thenReturn(gitResult);

        when(commitStore.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act

        List<CommitResponse> commits =
                commitService.readCommits(
                        repositoryId
                );

        // Assert

        assertEquals(
                1,
                commits.size()
        );

        CommitResponse commit =
                commits.get(0);

        assertEquals(
                "6837b869d3ae40c9a54866eff680d17c81fa9ab9",
                commit.hash()
        );

        assertEquals(
                "vasanthan-hub",
                commit.authorName()
        );

        assertEquals(
                "vasanthanr2004@gmail.com",
                commit.authorEmail()
        );

        assertEquals(
                "feat: implemented activity logs",
                commit.subject()
        );

        assertEquals(
                repositoryId,
                commit.repositoryId()
        );

        verify(repositoryStore)
                .findById(repositoryId);

        verify(gitClient)
                .execute(
                        eq(repositoryPath),
                        anyList()
                );

        verify(commitStore)
                .saveAll(anyList());
    }
}
