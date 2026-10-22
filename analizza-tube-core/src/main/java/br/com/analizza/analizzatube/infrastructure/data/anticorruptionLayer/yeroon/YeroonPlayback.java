package br.com.analizza.analizzatube.infrastructure.data.anticorruptionLayer.yeroon;

import java.net.URI;
import java.time.Instant;

public record YeroonPlayback(URI embedUrl, Instant expiresAt) {}
