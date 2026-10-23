package br.com.mytube.infrastructure;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.mytube.domain.Video;
import br.com.mytube.domain.repositories.VideoRepository;
import br.com.mytube.domain.repositories.WebhookReceiptRepository;
import br.com.mytube.support.BaseIntegrationTest;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class VideoRepositoryIT extends BaseIntegrationTest {

    @Autowired
    private VideoRepository videos;

    @Autowired
    private WebhookReceiptRepository receipts;

    @Test
    void salvaELeOsIds() {
        Video a = Video.register(UUID.randomUUID(), Instant.now());
        Video b = Video.register(UUID.randomUUID(), Instant.now());

        videos.save(a);
        videos.save(b);

        var ids = videos.findAllIds();
        assertTrue(ids.contains(a.getId()));
        assertTrue(ids.contains(b.getId()));
    }

    @Test
    void existsById() {
        Video video = Video.register(UUID.randomUUID(), Instant.now());
        videos.save(video);

        assertTrue(videos.existsById(video.getId()));
        assertFalse(videos.existsById(UUID.randomUUID()));
    }

    @Test
    void registerIfNewSoAceitaAPrimeiraVez() {
        String deliveryId = "delivery-" + UUID.randomUUID();
        UUID videoId = UUID.randomUUID();

        assertTrue(receipts.registerIfNew(deliveryId, "video.ready", videoId, Instant.now()));
        assertFalse(receipts.registerIfNew(deliveryId, "video.ready", videoId, Instant.now()));
    }
}
