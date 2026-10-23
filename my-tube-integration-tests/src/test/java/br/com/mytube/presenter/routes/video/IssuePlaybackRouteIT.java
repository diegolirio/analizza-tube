package br.com.mytube.presenter.routes.video;

import static org.junit.jupiter.api.Assertions.*;

import br.com.mytube.domain.Video;
import br.com.mytube.domain.repositories.VideoRepository;
import br.com.mytube.infrastructure.data.anticorruptionLayer.yeroon.YeroonConflictException;
import br.com.mytube.support.BaseIntegrationTest;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class IssuePlaybackRouteIT extends BaseIntegrationTest {

    @Autowired
    private VideoRepository videos;

    @Test
    void devolveSoEmbedUrlEExpiresAt() {
        UUID id = UUID.randomUUID();
        videos.save(Video.register(id, Instant.now()));

        var body = http.post().uri("/videos/{id}/playback", id).exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Cache-Control", "no-store")
                .expectBody()
                .jsonPath("$.embedUrl").exists()
                .jsonPath("$.expiresAt").exists()
                .jsonPath("$.token").doesNotExist()
                .jsonPath("$.hlsUrl").doesNotExist()
                .returnResult().getResponseBody();
        assertFalse(new String(body).contains("token"));
    }

    @Test
    void conflitoDaV1Retorna409() {
        UUID id = UUID.randomUUID();
        videos.save(Video.register(id, Instant.now()));
        yeroon.failNext(new YeroonConflictException());

        http.post().uri("/videos/{id}/playback", id).exchange().expectStatus().isEqualTo(409);
    }

    @Test
    void idForaDaTabelaRetorna404() {
        UUID id = UUID.randomUUID();

        var body = http.post().uri("/videos/{id}/playback", id).exchange().expectStatus().isNotFound()
                .expectBody().returnResult().getResponseBody();
        assertEquals(0, yeroon.calls());
        assertFalse(new String(body).contains(id.toString()));
    }
}
