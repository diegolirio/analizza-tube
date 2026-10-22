package br.com.analizza.analizzatube.infrastructure.data.anticorruptionLayer.yeroon;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

public record YeroonVideoSummary(UUID videoId, String fileName, String status, Instant createdAt, URI thumbnailUrl) {}
