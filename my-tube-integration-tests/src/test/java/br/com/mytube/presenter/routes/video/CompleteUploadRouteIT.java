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

class CompleteUploadRouteIT extends BaseIntegrationTest {

    @Autowired
    private VideoRepository videos;

    @Test
    void completaOUpload() {
        UUID id = UUID.randomUUID();
        videos.save(Video.register(id, Instant.now()));

        http.post().uri("/videos/{id}/upload/complete", id).contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("parts", List.of(Map.of("partNumber", 1, "etag", "\"e\""))))
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.videoId").isEqualTo(id.toString())
                .jsonPath("$.status").isEqualTo("PROCESSING");
    }

    @Test
    void idForaDaTabelaRetorna404SemChamarAV1() {
        UUID id = UUID.randomUUID();

        var body = http.post().uri("/videos/{id}/upload/complete", id).contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("parts", List.of(Map.of("partNumber", 1, "etag", "\"e\""))))
                .exchange().expectStatus().isNotFound()
                .expectBody().returnResult().getResponseBody();
        assertEquals(0, yeroon.calls());
        assertFalse(new String(body).contains(id.toString()));
    }

    @Test
    void partsAusenteVira422() {
        UUID id = UUID.randomUUID();
        videos.save(Video.register(id, Instant.now()));

        http.post().uri("/videos/{id}/upload/complete", id).contentType(MediaType.APPLICATION_JSON)
                .body(Map.of())
                .exchange().expectStatus().isEqualTo(422)
                .expectBody().jsonPath("$.errors[0].code").isEqualTo("required.parts")
                .jsonPath("$.errors[0].message").isNotEmpty();
    }
}
