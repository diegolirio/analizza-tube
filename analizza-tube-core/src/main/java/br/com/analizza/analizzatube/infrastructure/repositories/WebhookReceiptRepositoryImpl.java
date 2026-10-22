package br.com.analizza.analizzatube.infrastructure.repositories;

import br.com.analizza.analizzatube.domain.repositories.WebhookReceiptRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class WebhookReceiptRepositoryImpl implements WebhookReceiptRepository {
    private final WebhookReceiptJpaRepository jpa;

    WebhookReceiptRepositoryImpl(WebhookReceiptJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public boolean registerIfNew(String deliveryId, String eventType, UUID videoId, Instant at) {
        return jpa.insertIfNew(deliveryId, eventType, videoId, at) == 1;
    }
}
