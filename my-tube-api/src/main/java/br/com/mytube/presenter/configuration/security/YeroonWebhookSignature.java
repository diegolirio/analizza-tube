package br.com.mytube.presenter.configuration.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** Confere o X-Yeroon-Signature: {@code t=<epoch>,v1=hex(HMAC-SHA256(segredo, "<epoch>.<corpo>"))}. */
public final class YeroonWebhookSignature {

    private static final long TOLERANCE_SECONDS = Duration.ofSeconds(300).toSeconds();

    private YeroonWebhookSignature() {
    }

    public static boolean verify(String secret, String header, String rawBody, Instant now) {
        if (secret == null || secret.isEmpty() || header == null || rawBody == null) return false;
        String t = null;
        String v1 = null;
        for (String part : header.split(",")) {
            String p = part.trim();
            if (p.startsWith("t=")) t = p.substring(2);
            else if (p.startsWith("v1=")) v1 = p.substring(3);
        }
        if (t == null || v1 == null) return false;
        long epoch;
        try {
            epoch = Long.parseLong(t);
        } catch (NumberFormatException e) {
            return false;
        }
        if (Math.abs(now.getEpochSecond() - epoch) > TOLERANCE_SECONDS) return false;
        byte[] provided;
        try {
            provided = HexFormat.of().parseHex(v1);
        } catch (IllegalArgumentException e) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expected = mac.doFinal((epoch + "." + rawBody).getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(expected, provided);
        } catch (GeneralSecurityException e) {
            return false;
        }
    }
}
