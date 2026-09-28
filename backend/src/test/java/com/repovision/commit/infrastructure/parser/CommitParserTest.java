package com.repovision.commit.infrastructure.parser;

import com.repovision.commit.domain.Commit;
import com.repovision.git.GitCommandResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CommitParserTest {

    private final CommitParser commitParser =
            new CommitParser();

    @Test
    void shouldParseGitCommitOutput() {

        UUID repositoryId = UUID.randomUUID();

        String output =
                "6837b869d3ae40c9a54866eff680d17c81fa9ab9\u001F"
                        + "vasanthan-hub\u001F"
                        + "vasanthanr2004@gmail.com\u001F"
                        + "2026-06-28T09:47:51+05:30\u001F"
                        + "feat: implemented activity logs\n"

                        + "7a94bd9e60d14868495077166d547e801c571a46\u001F"
                        + "vasanthan-hub\u001F"
                        + "vasanthanr2004@gmail.com\u001F"
                        + "2026-06-17T19:21:42+05:30\u001F"
                        + "feat: implemented task comments";

        GitCommandResult result =
                new GitCommandResult(
                        0,
                        output,
                        ""
                );

        List<ParsedCommit> parsedCommits =
                commitParser.parse(
                        repositoryId,
                        result
                );

        assertEquals(2, parsedCommits.size());

        List<Commit> commits =
                parsedCommits.stream()
                        .map(ParsedCommit::commit)
                        .toList();

        Commit first = commits.get(0);

        assertEquals(
                "6837b869d3ae40c9a54866eff680d17c81fa9ab9",
                first.getHash()
        );

        assertEquals(
                "vasanthan-hub",
                first.getAuthorName()
        );

        assertEquals(
                "vasanthanr2004@gmail.com",
                first.getAuthorEmail()
        );

        assertEquals(
                "feat: implemented activity logs",
                first.getSubject()
        );

        assertEquals(
                repositoryId,
                first.getRepositoryId()
        );
    }
}