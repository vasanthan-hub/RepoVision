package com.repovision.commit.infrastructure.parser;

import com.repovision.commit.domain.Commit;
import com.repovision.commit.domain.FileChange;

import java.util.List;

public record ParsedCommit(
        Commit commit,
        List<FileChange> fileChanges
) {}