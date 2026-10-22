package br.com.analizza.analizzatube.infrastructure.repositories;

import br.com.analizza.analizzatube.domain.Video;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "video")
public class VideoJpaEntity {
    @Id
    private UUID id;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected VideoJpaEntity() {
    }

    VideoJpaEntity(UUID id, Instant createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    static VideoJpaEntity fromDomain(Video video) {
        return new VideoJpaEntity(video.getId(), video.getCreatedAt());
    }

    public UUID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
