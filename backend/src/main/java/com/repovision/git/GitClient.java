package com.repovision.git;

import java.nio.file.Path;
import java.util.List;

public interface GitClient {

    GitCommandResult execute(
            Path repositoryPath,
            List<String> arguments
    );
}