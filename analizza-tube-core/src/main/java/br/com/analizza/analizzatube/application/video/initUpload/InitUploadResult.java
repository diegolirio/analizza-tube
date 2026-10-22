package br.com.analizza.analizzatube.application.video.initUpload;

import java.util.UUID;

public record InitUploadResult(UUID videoId, long partSizeBytes, int partCount) {}
