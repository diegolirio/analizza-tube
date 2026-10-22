package br.com.analizza.analizzatube.infrastructure.repositories;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

interface WebhookReceiptJpaRepository extends JpaRepository<VideoJpaEntity, UUID> {
    @Modifying
    @Transactional
    @Query(value = """
            insert into webhook_receipt (delivery_id, event_type, video_id, received_at)
            values (:deliveryId, :eventType, :videoId, :at)
            on conflict (delivery_id) do nothing
            """, nativeQuery = true)
    int insertIfNew(@Param("deliveryId") String deliveryId,
                    @Param("eventType") String eventType,
                    @Param("videoId") UUID videoId,
                    @Param("at") Instant at);
}
