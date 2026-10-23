package br.com.mytube.application.video.issuePlayback;

import java.time.Instant;

public record IssuePlaybackResult(String embedUrl, Instant expiresAt) {}
