package br.com.analizza.analizzatube.presenter.routes.video;

import static org.junit.jupiter.api.Assertions.*;

import br.com.analizza.analizzatube.domain.Video;
import br.com.analizza.analizzatube.domain.repositories.VideoRepository;
import br.com.analizza.analizzatube.support.BaseIntegrationTest;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class PresignUploadPartsRouteIT extends BaseIntegrationTest {

    @Autowired
    private VideoRepository videos;

    @Test
    void devolveUmaUrlPorParte() {
        UUID id = UUID.randomUUID();
        videos.save(Video.register(id, Instant.now()));

        http.post().uri("/videos/{id}/upload/part-urls", id).contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("partNumbers", List.of(1, 2)))
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.urls[0].partNumber").isEqualTo(1)
                .jsonPath("$.urls[1].url").isEqualTo("https://r2.example/" + id + "/2");
    }

    @Test
    void idForaDaTabelaRetorna404SemChamarAV1() {
        UUID id = UUID.randomUUID();

        var body = http.post().uri("/videos/{id}/upload/part-urls", id).contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("partNumbers", List.of(1)))
                .exchange().expectStatus().isNotFound()
                .expectBody().returnResult().getResponseBody();
        assertEquals(0, yeroon.calls());
        assertFalse(new String(body).contains(id.toString()));
    }

    @Test
    void partNumbersAusenteVira422() {
        UUID id = UUID.randomUUID();
        videos.save(Video.register(id, Instant.now()));

        http.post().uri("/videos/{id}/upload/part-urls", id).contentType(MediaType.APPLICATION_JSON)
                .body(Map.of())
                .exchange().expectStatus().isEqualTo(422)
                .expectBody().jsonPath("$.errors[0].code").isEqualTo("required.partNumbers")
                .jsonPath("$.errors[0].message").isNotEmpty();
    }
}
