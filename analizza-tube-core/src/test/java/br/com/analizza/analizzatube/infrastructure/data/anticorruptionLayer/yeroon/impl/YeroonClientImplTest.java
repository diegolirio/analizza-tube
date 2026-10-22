package br.com.analizza.analizzatube.infrastructure.data.anticorruptionLayer.yeroon.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.analizza.analizzatube.infrastructure.configuration.YeroonProperties;
import br.com.analizza.analizzatube.infrastructure.data.anticorruptionLayer.yeroon.*;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class YeroonClientImplTest {

    private static final String KEY = "yk_teste";
    private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private HttpServer server;
    private int status;
    private String responseBody;
    private final AtomicReference<String> method = new AtomicReference<>();
    private final AtomicReference<String> path = new AtomicReference<>();
    private final AtomicReference<String> auth = new AtomicReference<>();
    private final AtomicReference<String> body = new AtomicReference<>();

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            method.set(exchange.getRequestMethod());
            path.set(exchange.getRequestURI().getPath());
            auth.set(exchange.getRequestHeaders().getFirst("Authorization"));
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] out = responseBody == null ? new byte[0] : responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, out.length == 0 ? -1 : out.length);
            if (out.length > 0) {
                exchange.getResponseBody().write(out);
            }
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private YeroonClientImpl client() {
        return client(URI.create("http://127.0.0.1:" + server.getAddress().getPort()));
    }

    private YeroonClientImpl client(URI url) {
        return new YeroonClientImpl(new YeroonProperties(url, KEY, "whsec", Duration.ofSeconds(2)));
    }

    private void respond(int status, String body) {
        this.status = status;
        this.responseBody = body;
    }

    @Test
    void initUploadMandaBearerECorpo() {
        respond(201, "{\"videoId\":\"" + ID + "\",\"partSizeBytes\":8388608,\"partCount\":3}");

        YeroonUpload upload = client().initUpload("aula.mp4", "video/mp4", 20_000_000L);

        assertThat(method.get()).isEqualTo("POST");
        assertThat(path.get()).isEqualTo("/v1/videos/uploads");
        assertThat(auth.get()).isEqualTo("Bearer " + KEY);
        assertThat(body.get()).contains("\"fileName\":\"aula.mp4\"", "\"contentType\":\"video/mp4\"", "\"sizeBytes\":20000000");
        assertThat(upload).isEqualTo(new YeroonUpload(ID, 8_388_608L, 3));
    }

    @Test
    void partUrlsMapeiaAsUrls() {
        respond(200, "{\"urls\":[{\"partNumber\":1,\"url\":\"https://s3/x\"}]}");

        List<YeroonPartUrl> urls = client().partUrls(ID, List.of(1));

        assertThat(path.get()).isEqualTo("/v1/videos/" + ID + "/upload/part-urls");
        assertThat(body.get()).contains("\"partNumbers\":[1]");
        assertThat(urls).containsExactly(new YeroonPartUrl(1, URI.create("https://s3/x")));
    }

    @Test
    void uploadedPartsLeAsPartes() {
        respond(200, "{\"parts\":[{\"partNumber\":1,\"etag\":\"abc\",\"sizeBytes\":8388608}]}");

        List<YeroonUploadedPart> parts = client().uploadedParts(ID);

        assertThat(method.get()).isEqualTo("GET");
        assertThat(path.get()).isEqualTo("/v1/videos/" + ID + "/upload/parts");
        assertThat(parts).containsExactly(new YeroonUploadedPart(1, "abc", 8_388_608L));
    }

    @Test
    void completeEnviaAsPartes() {
        respond(200, "{\"videoId\":\"" + ID + "\",\"status\":\"ENVIADO\"}");

        YeroonCompleted done = client().complete(ID, List.of(new YeroonPart(1, "abc")));

        assertThat(path.get()).isEqualTo("/v1/videos/" + ID + "/upload/complete");
        assertThat(body.get()).contains("\"partNumber\":1", "\"etag\":\"abc\"");
        assertThat(done).isEqualTo(new YeroonCompleted(ID, "ENVIADO"));
    }

    @Test
    void cancelFazDelete() {
        respond(204, null);

        client().cancel(ID);

        assertThat(method.get()).isEqualTo("DELETE");
        assertThat(path.get()).isEqualTo("/v1/videos/" + ID + "/upload");
    }

    @Test
    void getEListMapeiam() {
        respond(200, "{\"videoId\":\"" + ID + "\",\"fileName\":\"a.mp4\",\"status\":\"PRONTO\",\"failureReason\":null,"
                + "\"renditions\":[\"720p\"],\"thumbnailUrl\":\"https://t/x.jpg\"}");
        YeroonVideo video = client().get(ID);
        assertThat(video).isEqualTo(new YeroonVideo(ID, "a.mp4", "PRONTO", null, List.of("720p"), URI.create("https://t/x.jpg")));

        respond(200, "{\"videos\":[{\"videoId\":\"" + ID + "\",\"fileName\":\"a.mp4\",\"status\":\"PRONTO\","
                + "\"createdAt\":\"2026-10-21T10:00:00Z\",\"thumbnailUrl\":null}]}");
        assertThat(client().list()).containsExactly(
                new YeroonVideoSummary(ID, "a.mp4", "PRONTO", Instant.parse("2026-10-21T10:00:00Z"), null));
        assertThat(path.get()).isEqualTo("/v1/videos");
    }

    @Test
    void playbackLeEmbedUrl() {
        respond(200, "{\"videoId\":\"" + ID + "\",\"token\":\"eyJ\",\"expiresAt\":\"2026-10-22T04:00:00Z\","
                + "\"embedUrl\":\"https://play/embed/eyJ\",\"hlsUrl\":\"https://play/m.m3u8\"}");

        YeroonPlayback playback = client().playbackToken(ID);

        assertThat(method.get()).isEqualTo("POST");
        assertThat(path.get()).isEqualTo("/v1/videos/" + ID + "/playback-token");
        assertThat(playback).isEqualTo(
                new YeroonPlayback(URI.create("https://play/embed/eyJ"), Instant.parse("2026-10-22T04:00:00Z")));
    }

    @Test
    void erro422ViraRejectedComOsErros() {
        respond(422, "{\"errors\":[{\"code\":\"video.size\",\"message\":\"grande demais\"}]}");

        assertThatThrownBy(() -> client().initUpload("a.mp4", "video/mp4", 1))
                .isInstanceOfSatisfying(YeroonRejectedException.class, e ->
                        assertThat(e.errors()).containsExactly(new YeroonError("video.size", "grande demais")));
    }

    @Test
    void erro404ViraNotFound() {
        respond(404, null);
        assertThatThrownBy(() -> client().get(ID)).isInstanceOf(YeroonNotFoundException.class);
    }

    @Test
    void erro409ViraConflict() {
        respond(409, null);
        assertThatThrownBy(() -> client().playbackToken(ID)).isInstanceOf(YeroonConflictException.class);
    }

    @Test
    void erro401ViraUnavailableSemAChaveNaMensagem() {
        respond(401, null);
        assertThatThrownBy(() -> client().list())
                .isInstanceOf(YeroonUnavailableException.class)
                .satisfies(e -> assertThat(e.getMessage()).doesNotContain(KEY));
    }

    @Test
    void erro500ViraUnavailable() {
        respond(500, null);
        assertThatThrownBy(() -> client().list()).isInstanceOf(YeroonUnavailableException.class);
    }

    @Test
    void servidorForaViraUnavailable() throws IOException {
        int closed;
        try (ServerSocket socket = new ServerSocket(0, 0, java.net.InetAddress.getByName("127.0.0.1"))) {
            closed = socket.getLocalPort();
        }
        assertThatThrownBy(() -> client(URI.create("http://127.0.0.1:" + closed)).list())
                .isInstanceOf(YeroonUnavailableException.class)
                .satisfies(e -> assertThat(e.getMessage()).doesNotContain(KEY));
    }
}
