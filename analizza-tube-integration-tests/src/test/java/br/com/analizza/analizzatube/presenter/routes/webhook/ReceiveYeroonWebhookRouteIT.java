package br.com.analizza.analizzatube.presenter.routes.webhook;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import br.com.analizza.analizzatube.support.BaseIntegrationTest;
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
        return "{\"id\":\"evt\",\"type\":\"video.ready\",\"createdAt\":\"2026-10-21T10:00:00Z\","
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
        String body = body(video);
        post(body, sign(SECRET, Instant.now().getEpochSecond(), body), "d-1", true);

        assertEquals(1, count());
        assertEquals(video, jdbc.queryForObject(
                "select video_id from webhook_receipt where delivery_id = 'd-1'", UUID.class));
    }

    @Test
    void avisoRepetidoResponde200SemSegundaLinha() throws Exception {
        String body = body(UUID.randomUUID());
        String sig = sign(SECRET, Instant.now().getEpochSecond(), body);
        post(body, sig, "d-2", true);
        post(body, sig, "d-2", true);

        assertEquals(1, count());
    }

    @Test
    void assinaturaErradaResponde401SemRegistrar() throws Exception {
        String body = body(UUID.randomUUID());
        post(body, sign("outro", Instant.now().getEpochSecond(), body), "d-3", false);

        assertEquals(0, count());
    }
}
