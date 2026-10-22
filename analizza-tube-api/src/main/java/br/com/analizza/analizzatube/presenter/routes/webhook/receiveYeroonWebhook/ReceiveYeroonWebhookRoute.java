package br.com.analizza.analizzatube.presenter.routes.webhook.receiveYeroonWebhook;

import br.com.analizza.analizzatube.application.webhook.receiveYeroonEvent.*;
import br.com.analizza.analizzatube.presenter.configuration.security.YeroonWebhookSignature;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@RestController
public class ReceiveYeroonWebhookRoute {
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final ReceiveYeroonEventHandler handler;
    // Lido direto da property: o -api nao importa infrastructure/.
    private final String secret;

    public ReceiveYeroonWebhookRoute(ReceiveYeroonEventHandler handler,
                                     @Value("${yeroon.webhook-secret:}") String secret) {
        this.handler = handler;
        this.secret = secret;
    }

    @PostMapping("/webhooks/yeroon")
    public ResponseEntity<?> handle(@RequestBody String rawBody,
                                    @RequestHeader(value = "X-Yeroon-Signature", required = false) String signature,
                                    @RequestHeader(value = "X-Yeroon-Delivery", required = false) String deliveryId,
                                    @RequestHeader(value = "X-Yeroon-Event", required = false) String eventType) {
        if (!YeroonWebhookSignature.verify(secret, signature, rawBody, Instant.now())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED));
        }
        UUID videoId;
        try {
            JsonNode id = JSON.readTree(rawBody).path("data").path("videoId");
            videoId = UUID.fromString(id.asString(""));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ProblemDetail.forStatus(HttpStatus.BAD_REQUEST));
        }
        if (deliveryId == null || deliveryId.isBlank() || eventType == null || eventType.isBlank()) {
            return ResponseEntity.badRequest().body(ProblemDetail.forStatus(HttpStatus.BAD_REQUEST));
        }
        handler.handle(new ReceiveYeroonEventCommand(deliveryId, eventType, videoId));
        return ResponseEntity.ok().build();
    }
}
