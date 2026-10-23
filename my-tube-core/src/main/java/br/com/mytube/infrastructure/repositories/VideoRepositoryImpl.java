package br.com.mytube.infrastructure.repositories;

import br.com.mytube.domain.Video;
import br.com.mytube.domain.repositories.VideoRepository;
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
