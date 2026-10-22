package br.com.analizza.analizzatube.infrastructure.repositories;

import br.com.analizza.analizzatube.domain.Video;
import br.com.analizza.analizzatube.domain.repositories.VideoRepository;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class VideoRepositoryImpl implements VideoRepository {
    private final VideoJpaRepository jpa;

    VideoRepositoryImpl(VideoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(Video video) {
        jpa.save(VideoJpaEntity.fromDomain(video));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpa.existsById(id);
    }

    @Override
    public Set<UUID> findAllIds() {
        return jpa.findAllIds();
    }
}
