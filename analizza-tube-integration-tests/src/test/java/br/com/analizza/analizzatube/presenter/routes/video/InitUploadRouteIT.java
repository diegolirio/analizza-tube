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

import br.com.analizza.analizzatube.infrastructure.data.anticorruptionLayer.yeroon.YeroonError;
import br.com.analizza.analizzatube.infrastructure.data.anticorruptionLayer.yeroon.YeroonRejectedException;

class InitUploadRouteIT extends BaseIntegrationTest {

    @Autowired
    private VideoRepository videos;

    @Test
    void criaOUploadERegistraOVideo() {
        var body = http.post().uri("/videos/uploads").contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("fileName", "a.mp4", "contentType", "video/mp4", "sizeBytes", 20_000_000))
                .exchange().expectStatus().isCreated()
                .expectBody().jsonPath("$.partSizeBytes").isEqualTo(8388608)
                .jsonPath("$.partCount").isEqualTo(3)
                .returnResult().getResponseBody();
        assertNotNull(body);
        UUID id = yeroon.videos.keySet().iterator().next();
        assertTrue(new String(body).contains(id.toString()));
        assertTrue(videos.existsById(id));
    }

    @Test
    void rejeicaoDoYeroonVira422ComCodigo() {
        yeroon.failNext(new YeroonRejectedException(List.of(new YeroonError("video.size", "grande demais"))));

        http.post().uri("/videos/uploads").contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("fileName", "a.mp4", "contentType", "video/mp4", "sizeBytes", 1))
                .exchange().expectStatus().isEqualTo(422)
                .expectBody().jsonPath("$.errors[0].code").isEqualTo("video.size");
        assertTrue(videos.findAllIds().stream().noneMatch(yeroon.videos::containsKey));
    }
}
