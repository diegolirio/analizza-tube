package br.com.mytube.infrastructure.data.anticorruptionLayer.yeroon;

import java.net.URI;
import java.util.List;
import java.util.UUID;

public record YeroonVideo(
        UUID videoId, String fileName, String status, String failureReason, List<String> renditions, URI thumbnailUrl) {}
