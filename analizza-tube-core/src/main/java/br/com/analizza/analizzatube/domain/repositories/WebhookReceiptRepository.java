package br.com.analizza.analizzatube.domain.repositories;

import java.time.Instant;
import java.util.UUID;

public interface WebhookReceiptRepository {
    /** Devolve true so na primeira vez que o deliveryId e registrado. */
    boolean registerIfNew(String deliveryId, String eventType, UUID videoId, Instant at);
}
