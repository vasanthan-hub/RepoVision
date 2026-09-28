package com.repovision.git;

public record GitCommandResult(
        int exitCode,
        String standardOutput,
        String standardError
) {

    public boolean isSuccessful() {
        return exitCode == 0;
    }
}