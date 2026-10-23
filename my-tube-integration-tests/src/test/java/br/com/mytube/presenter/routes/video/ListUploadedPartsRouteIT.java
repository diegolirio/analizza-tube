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

import br.com.mytube.support.FakeYeroonClient;

class ListUploadedPartsRouteIT extends BaseIntegrationTest {

    @Autowired
    private VideoRepository videos;

    @Test
    void listaAsPartesJaEnviadas() {
        UUID id = UUID.randomUUID();
        videos.save(Video.register(id, Instant.now()));

        http.get().uri("/videos/{id}/upload/parts", id)
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.parts[0].partNumber").isEqualTo(1)
                .jsonPath("$.parts[0].etag").isEqualTo(FakeYeroonClient.ETAG)
                .jsonPath("$.parts[0].sizeBytes").isEqualTo(1024);
    }

    @Test
    void idForaDaTabelaRetorna404SemChamarAV1() {
        UUID id = UUID.randomUUID();

        var body = http.get().uri("/videos/{id}/upload/parts", id)
                .exchange().expectStatus().isNotFound()
                .expectBody().returnResult().getResponseBody();
        assertEquals(0, yeroon.calls());
        assertFalse(new String(body).contains(id.toString()));
    }
}
