package br.com.mytube.presenter.routes.video;

import static org.junit.jupiter.api.Assertions.*;

import br.com.mytube.domain.Video;
import br.com.mytube.domain.repositories.VideoRepository;
import br.com.mytube.support.BaseIntegrationTest;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class CancelUploadRouteIT extends BaseIntegrationTest {

    @Autowired
    private VideoRepository videos;

    @Test
    void cancelaOUpload() {
        UUID id = UUID.randomUUID();
        videos.save(Video.register(id, Instant.now()));

        http.delete().uri("/videos/{id}/upload", id)
                .exchange().expectStatus().isNoContent();
        assertEquals(1, yeroon.calls());
    }

    @Test
    void idForaDaTabelaRetorna404SemChamarAV1() {
        UUID id = UUID.randomUUID();

        var body = http.delete().uri("/videos/{id}/upload", id)
                .exchange().expectStatus().isNotFound()
                .expectBody().returnResult().getResponseBody();
        assertEquals(0, yeroon.calls());
        assertFalse(new String(body).contains(id.toString()));
    }
}
