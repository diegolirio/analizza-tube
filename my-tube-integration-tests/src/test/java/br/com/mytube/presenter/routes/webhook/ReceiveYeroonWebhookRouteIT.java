package br.com.mytube.presenter.routes.webhook;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import br.com.mytube.support.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

class ReceiveYeroonWebhookRouteIT extends BaseIntegrationTest {

    private static final String SECRET = "whsec_teste";

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void limpar() {
        jdbc.update("delete from webhook_receipt");
    }

    private static String sign(String secret, long t, String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return "t=" + t + ",v1=" + HexFormat.of().formatHex(
                mac.doFinal((t + "." + body).getBytes(StandardCharsets.UTF_8)));
    }

    private static String body(UUID videoId) {
        return body(videoId, "evt");
    }

    private static String body(UUID videoId, String eventId) {
        return "{\"id\":\"" + eventId + "\",\"type\":\"video.ready\",\"createdAt\":\"2026-10-21T10:00:00Z\","
                + "\"data\":{\"videoId\":\"" + videoId + "\",\"status\":\"PRONTO\",\"renditions\":[\"720p\"]}}";
    }

    private void post(String body, String signature, String delivery, boolean expectOk) {
        var spec = http.post().uri("/webhooks/yeroon")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Yeroon-Event", "video.ready")
                .header("X-Yeroon-Delivery", delivery)
                .header("X-Yeroon-Signature", signature)
                .body(body)
                .exchange();
        if (expectOk) spec.expectStatus().isOk(); else spec.expectStatus().isUnauthorized();
    }

    private int count() {
        return jdbc.queryForObject("select count(*) from webhook_receipt", Integer.class);
    }

    @Test
    void avisoValidoRegistraEResponde200() throws Exception {
        UUID video = UUID.randomUUID();
        String body = body(video, "evt-1");
        post(body, sign(SECRET, Instant.now().getEpochSecond(), body), "evt-1", true);

        assertEquals(1, count());
        assertEquals(video, jdbc.queryForObject(
                "select video_id from webhook_receipt where delivery_id = 'evt-1'", UUID.class));
    }

    @Test
    void avisoRepetidoResponde200SemSegundaLinha() throws Exception {
        String body = body(UUID.randomUUID(), "evt-2");
        String sig = sign(SECRET, Instant.now().getEpochSecond(), body);
        post(body, sig, "evt-2", true);
        post(body, sig, "evt-2", true);

        assertEquals(1, count());
    }

    @Test
    void mesmoCorpoReenviadoComOutroCabecalhoDeEntregaDiverge400SemSegundaLinha() throws Exception {
        String body = body(UUID.randomUUID(), "evt-4");
        String sig = sign(SECRET, Instant.now().getEpochSecond(), body);
        post(body, sig, "evt-4", true);
        http.post().uri("/webhooks/yeroon")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Yeroon-Event", "video.ready")
                .header("X-Yeroon-Delivery", "outro-id")
                .header("X-Yeroon-Signature", sig)
                .body(body)
                .exchange()
                .expectStatus().isBadRequest();

        assertEquals(1, count());
    }

    @Test
    void corpoAlteradoResponde401() throws Exception {
        String body = body(UUID.randomUUID(), "evt-5");
        String sig = sign(SECRET, Instant.now().getEpochSecond(), body);
        post(body.replace("PRONTO", "FALHOU"), sig, "evt-5", false);

        assertEquals(0, count());
    }

    @Test
    void marcaDeTempoForaDaJanelaDe300sResponde401() throws Exception {
        String body = body(UUID.randomUUID(), "evt-6");
        post(body, sign(SECRET, Instant.now().getEpochSecond() - 301, body), "evt-6", false);

        assertEquals(0, count());
    }

    @Test
    void assinaturaErradaResponde401SemRegistrar() throws Exception {
        String body = body(UUID.randomUUID());
        post(body, sign("outro", Instant.now().getEpochSecond(), body), "evt", false);

        assertEquals(0, count());
    }
}
