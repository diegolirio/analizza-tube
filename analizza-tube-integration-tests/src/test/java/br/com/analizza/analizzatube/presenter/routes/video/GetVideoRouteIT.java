package br.com.analizza.analizzatube.presenter.routes.video;

import static org.junit.jupiter.api.Assertions.*;

import br.com.analizza.analizzatube.domain.Video;
import br.com.analizza.analizzatube.domain.repositories.VideoRepository;
import br.com.analizza.analizzatube.infrastructure.data.anticorruptionLayer.yeroon.YeroonVideo;
import br.com.analizza.analizzatube.support.BaseIntegrationTest;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class GetVideoRouteIT extends BaseIntegrationTest {

    @Autowired
    private VideoRepository videos;

    @Test
    void devolveOEstadoDaV1() {
        UUID id = UUID.randomUUID();
        yeroon.videos.put(id, new YeroonVideo(id, "a.mp4", "READY", null, List.of("720p", "1080p"), URI.create("https://cdn.example/a.jpg")));
        videos.save(Video.register(id, Instant.now()));

        http.get().uri("/videos/{id}", id).exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath("$.videoId").isEqualTo(id.toString())
                .jsonPath("$.fileName").isEqualTo("a.mp4")
                .jsonPath("$.status").isEqualTo("READY")
                .jsonPath("$.renditions[1]").isEqualTo("1080p")
                .jsonPath("$.thumbnailUrl").isEqualTo("https://cdn.example/a.jpg");
    }

    @Test
    void idForaDaTabelaRetorna404SemChamarAV1() {
        UUID id = UUID.randomUUID();

        var body = http.get().uri("/videos/{id}", id).exchange().expectStatus().isNotFound()
                .expectBody().returnResult().getResponseBody();
        assertEquals(0, yeroon.calls());
        assertFalse(new String(body).contains(id.toString()));
    }
}
