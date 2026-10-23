package br.com.mytube.application.video.getVideo;

import java.util.List;
import java.util.UUID;

public record GetVideoResult(
        UUID videoId, String fileName, String status, String failureReason, List<String> renditions, String thumbnailUrl) {}
