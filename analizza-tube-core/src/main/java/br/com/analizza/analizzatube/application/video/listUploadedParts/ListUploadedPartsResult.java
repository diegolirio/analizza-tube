package br.com.analizza.analizzatube.application.video.listUploadedParts;

import java.util.List;

public record ListUploadedPartsResult(List<Part> parts) {
    public record Part(int partNumber, String etag, long sizeBytes) {}
}
