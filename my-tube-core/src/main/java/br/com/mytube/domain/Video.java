package br.com.mytube.domain;

import java.time.Instant;
import java.util.UUID;

public class Video extends Entity {
    private final UUID id;
    private final Instant createdAt;

    private Video(UUID id, Instant createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public static Video register(UUID yeroonVideoId, Instant now) {
        return new Video(yeroonVideoId, now);
    }

    public UUID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
