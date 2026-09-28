package com.repovision.commit.infrastructure.parser;

public record ParsedFileChange(
        String filePath,
        long additions,
        long deletions
) {
}