package br.com.mytube.infrastructure.repositories;

import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface VideoJpaRepository extends JpaRepository<VideoJpaEntity, UUID> {
    @Query("select v.id from VideoJpaEntity v")
    Set<UUID> findAllIds();
}
