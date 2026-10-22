package br.com.analizza.analizzatube.infrastructure.data.anticorruptionLayer.yeroon;

import java.util.UUID;

public record YeroonUpload(UUID videoId, long partSizeBytes, int partCount) {}
