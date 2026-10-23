package br.com.mytube.infrastructure.data.anticorruptionLayer.yeroon;

import java.util.UUID;

public record YeroonUpload(UUID videoId, long partSizeBytes, int partCount) {}
