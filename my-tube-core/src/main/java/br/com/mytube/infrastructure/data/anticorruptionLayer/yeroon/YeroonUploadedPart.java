package br.com.mytube.infrastructure.data.anticorruptionLayer.yeroon;

public record YeroonUploadedPart(int partNumber, String etag, long sizeBytes) {}
