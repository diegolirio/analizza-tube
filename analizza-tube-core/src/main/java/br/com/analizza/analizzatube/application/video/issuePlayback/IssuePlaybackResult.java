package br.com.analizza.analizzatube.application.video.issuePlayback;

import java.time.Instant;

public record IssuePlaybackResult(String embedUrl, Instant expiresAt) {}
