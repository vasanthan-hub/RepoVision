package com.repovision.commit.infrastructure.parser;

import com.repovision.commit.domain.Commit;
import com.repovision.commit.domain.FileChange;
import com.repovision.git.GitCommandResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class CommitParser {

    private static final String FIELD_SEPARATOR = "\u001F";

    public List<ParsedCommit> parse(
            UUID repositoryId,
            GitCommandResult result
    ) {

        if (!result.isSuccessful()) {
            throw new IllegalStateException(
                    "Failed to read Git commit history: "
                            + result.standardError()
            );
        }

        String output = result.standardOutput();

        if (output == null || output.isBlank()) {
            return List.of();
        }

        String[] lines = output.split("\\R");

        List<ParsedCommit> parsedCommits =
                new ArrayList<>();

        String[] metadata = null;

        List<ParsedFileChange> parsedFileChanges =
                new ArrayList<>();

        long additions = 0;
        long deletions = 0;

        for (String line : lines) {

            if (line.isBlank()) {
                continue;
            }

            /*
             * Metadata line means we have reached
             * a new commit.
             */
            if (line.contains(FIELD_SEPARATOR)) {

                if (metadata != null) {

                    parsedCommits.add(
                            createParsedCommit(
                                    repositoryId,
                                    metadata,
                                    additions,
                                    deletions,
                                    parsedFileChanges
                            )
                    );
                }

                metadata = line.split(
                        FIELD_SEPARATOR,
                        -1
                );

                if (metadata.length != 5) {
                    throw new IllegalStateException(
                            "Invalid Git commit metadata: "
                                    + line
                    );
                }

                additions = 0;
                deletions = 0;

                parsedFileChanges =
                        new ArrayList<>();

                continue;
            }

            /*
             * numstat:
             *
             * additions <TAB> deletions <TAB> filePath
             */
            String[] fields = line.split(
                    "\\t",
                    -1
            );

            if (fields.length < 3) {
                continue;
            }

            String additionsValue = fields[0];
            String deletionsValue = fields[1];
            String filePath = fields[2];

            long fileAdditions = 0;
            long fileDeletions = 0;

            if (!additionsValue.equals("-")) {

                fileAdditions =
                        Long.parseLong(additionsValue);

                additions += fileAdditions;
            }

            if (!deletionsValue.equals("-")) {

                fileDeletions =
                        Long.parseLong(deletionsValue);

                deletions += fileDeletions;
            }

            String resolvedPath = resolveGitFilePath(filePath);

            parsedFileChanges.add(
                    new ParsedFileChange(
                            resolvedPath,
                            fileAdditions,
                            fileDeletions
                    )
            );
        }

        /*
         * Finish the final commit.
         */
        if (metadata != null) {

            parsedCommits.add(
                    createParsedCommit(
                            repositoryId,
                            metadata,
                            additions,
                            deletions,
                            parsedFileChanges
                    )
            );
        }

        return parsedCommits;
    }

    private ParsedCommit createParsedCommit(
            UUID repositoryId,
            String[] metadata,
            long additions,
            long deletions,
            List<ParsedFileChange> parsedFileChanges
    ) {

        UUID commitId =
                UUID.randomUUID();

        Commit commit =
                new Commit(
                        commitId,
                        repositoryId,
                        metadata[0],
                        metadata[1],
                        metadata[2],
                        Instant.parse(metadata[3]),
                        metadata[4],
                        additions,
                        deletions
                );

        List<FileChange> fileChanges =
                parsedFileChanges.stream()
                        .map(parsedFileChange ->
                                new FileChange(
                                        UUID.randomUUID(),
                                        commit.getId(),
                                        parsedFileChange.filePath(),
                                        parsedFileChange.additions(),
                                        parsedFileChange.deletions()
                                )
                        )
                        .toList();

        return new ParsedCommit(
                commit,
                fileChanges
        );
    }

    public static String resolveGitFilePath(String rawPath) {
        if (rawPath == null) return "";
        String path = rawPath.trim();
        if (!path.contains("=>")) {
            return path;
        }

        // Subpath rename: dir/{old => new}/sub or {old => new}
        if (path.contains("{") && path.contains("}")) {
            int braceStart = path.indexOf('{');
            int braceEnd = path.indexOf('}', braceStart);
            if (braceStart >= 0 && braceEnd > braceStart) {
                String prefix = path.substring(0, braceStart);
                String inside = path.substring(braceStart + 1, braceEnd);
                String suffix = path.substring(braceEnd + 1);

                String[] insideParts = inside.split("=>");
                String newPart = insideParts.length > 1 ? insideParts[1].trim() : "";

                String combined = prefix + newPart + suffix;
                return combined.replace("//", "/");
            }
        }

        // Full path rename: old_path => new_path
        String[] parts = path.split("=>");
        if (parts.length > 1) {
            return parts[1].trim();
        }

        return path;
    }
}