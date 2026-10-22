package br.com.analizza.analizzatube.presenter.configuration.security;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;

class YeroonWebhookSignatureTest {

    private static final String SECRET = "whsec_teste";
    private static final String BODY = "{\"id\":\"1\",\"type\":\"video.ready\"}";
    private static final long T = 1_700_000_000L;

    static String hmac(String secret, String payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    }

    static String header(String secret, long t, String body) throws Exception {
        return "t=" + t + ",v1=" + hmac(secret, t + "." + body);
    }

    @Test
    void assinaturaValidaPassa() throws Exception {
        assertTrue(YeroonWebhookSignature.verify(SECRET, header(SECRET, T, BODY), BODY, Instant.ofEpochSecond(T)));
    }

    @Test
    void corpoAlteradoFalha() throws Exception {
        assertFalse(YeroonWebhookSignature.verify(
                SECRET, header(SECRET, T, BODY), BODY + " ", Instant.ofEpochSecond(T)));
    }

    @Test
    void epochComMaisDe300sFalha() throws Exception {
        String h = header(SECRET, T, BODY);
        assertFalse(YeroonWebhookSignature.verify(SECRET, h, BODY, Instant.ofEpochSecond(T + 301)));
        assertFalse(YeroonWebhookSignature.verify(SECRET, h, BODY, Instant.ofEpochSecond(T - 301)));
        assertTrue(YeroonWebhookSignature.verify(SECRET, h, BODY, Instant.ofEpochSecond(T + 300)));
    }

    @Test
    void headerMalformadoFalha() {
        Instant now = Instant.ofEpochSecond(T);
        assertFalse(YeroonWebhookSignature.verify(SECRET, "v1=abc", BODY, now));
        assertFalse(YeroonWebhookSignature.verify(SECRET, "", BODY, now));
        assertFalse(YeroonWebhookSignature.verify(SECRET, null, BODY, now));
        assertFalse(YeroonWebhookSignature.verify(SECRET, "t=abc,v1=zz", BODY, now));
    }

    @Test
    void segredoVazioFalhaSempre() throws Exception {
        Instant now = Instant.ofEpochSecond(T);
        assertFalse(YeroonWebhookSignature.verify("", header(SECRET, T, BODY), BODY, now));
        assertFalse(YeroonWebhookSignature.verify(null, header(SECRET, T, BODY), BODY, now));
    }
}
