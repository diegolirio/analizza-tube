package br.com.mytube.infrastructure.data.anticorruptionLayer.yeroon;

import java.util.UUID;

public record YeroonCompleted(UUID videoId, String status) {}
