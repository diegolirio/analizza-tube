package br.com.mytube.application.video.initUpload;

import java.util.UUID;

public record InitUploadResult(UUID videoId, long partSizeBytes, int partCount) {}
