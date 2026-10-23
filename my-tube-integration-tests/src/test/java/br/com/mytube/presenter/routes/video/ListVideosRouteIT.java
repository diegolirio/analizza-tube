package br.com.mytube.presenter.routes.video;

import static org.junit.jupiter.api.Assertions.*;

import br.com.mytube.domain.Video;
import br.com.mytube.domain.repositories.VideoRepository;
import br.com.mytube.infrastructure.data.anticorruptionLayer.yeroon.YeroonUnavailableException;
import br.com.mytube.infrastructure.data.anticorruptionLayer.yeroon.YeroonVideo;
import br.com.mytube.support.BaseIntegrationTest;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ListVideosRouteIT extends BaseIntegrationTest {

    @Autowired
    private VideoRepository videos;

    @Test
    void listaSoOsVideosPresentesNaTabela() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        yeroon.videos.put(a, new YeroonVideo(a, "a.mp4", "READY", null, List.of("720p"), URI.create("https://cdn.example/a.jpg")));
        yeroon.videos.put(b, new YeroonVideo(b, "b.mp4", "READY", null, List.of("720p"), null));
        videos.save(Video.register(a, Instant.now()));

        http.get().uri("/videos").exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath("$.videos.length()").isEqualTo(1)
                .jsonPath("$.videos[0].videoId").isEqualTo(a.toString())
                .jsonPath("$.videos[0].fileName").isEqualTo("a.mp4")
                .jsonPath("$.videos[0].status").isEqualTo("READY")
                .jsonPath("$.videos[0].createdAt").exists()
                .jsonPath("$.videos[0].thumbnailUrl").isEqualTo("https://cdn.example/a.jpg");
    }

    @Test
    void v1IndisponivelRetorna502SemVazarChave() {
        yeroon.failNext(new YeroonUnavailableException("boom yk_secret"));

        var body = http.get().uri("/videos").exchange().expectStatus().isEqualTo(502)
                .expectBody().returnResult().getResponseBody();
        assertFalse(new String(body).contains("yk_"));
    }
}
