package com.devAssist.backend.tool;

import java.time.Instant;

public record GitHistoryResult(
        String commitId,
        String shortMessage,
        String author,
        Instant date
) {
}