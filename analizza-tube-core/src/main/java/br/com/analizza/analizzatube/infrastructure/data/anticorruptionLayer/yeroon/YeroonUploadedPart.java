package br.com.analizza.analizzatube.infrastructure.data.anticorruptionLayer.yeroon;

public record YeroonUploadedPart(int partNumber, String etag, long sizeBytes) {}
