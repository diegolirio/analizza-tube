package br.com.analizza.analizzatube.domain.repositories;

import br.com.analizza.analizzatube.domain.Video;
import java.util.Set;
import java.util.UUID;

public interface VideoRepository {
    void save(Video video);

    boolean existsById(UUID id);

    Set<UUID> findAllIds();
}
