package com.repovision.git;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class GitCliClient implements GitClient {

    @Override
    public GitCommandResult execute(
            Path repositoryPath,
            List<String> arguments
    ) {

        List<String> command = new ArrayList<>();

        command.add("git");
        command.add("-C");
        command.add(repositoryPath.toString());
        command.addAll(arguments);

        ProcessBuilder processBuilder =
                new ProcessBuilder(command);

        try {

            Process process = processBuilder.start();

            String standardOutput =
                    new String(
                            process.getInputStream().readAllBytes()
                    );

            String standardError =
                    new String(
                            process.getErrorStream().readAllBytes()
                    );

            int exitCode = process.waitFor();

            return new GitCommandResult(
                    exitCode,
                    standardOutput,
                    standardError
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Failed to execute Git command",
                    e
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Git command was interrupted",
                    e
            );
        }
    }

}