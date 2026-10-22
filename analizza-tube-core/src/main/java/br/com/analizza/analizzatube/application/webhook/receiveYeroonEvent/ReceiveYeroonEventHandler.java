package br.com.analizza.analizzatube.application.webhook.receiveYeroonEvent;

import br.com.analizza.analizzatube.application.CommandHandler;
import br.com.analizza.analizzatube.domain.repositories.WebhookReceiptRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ReceiveYeroonEventHandler implements CommandHandler<ReceiveYeroonEventCommand> {
    private static final Logger log = LoggerFactory.getLogger(ReceiveYeroonEventHandler.class);

    private final WebhookReceiptRepository receipts;

    public ReceiveYeroonEventHandler(WebhookReceiptRepository receipts) {
        this.receipts = receipts;
    }

    @Override
    public void handle(ReceiveYeroonEventCommand command) {
        boolean nova = receipts.registerIfNew(
                command.deliveryId(), command.eventType(), command.videoId(), Instant.now());
        log.info("yeroon {} video={} delivery={} nova={}",
                command.eventType(), command.videoId(), command.deliveryId(), nova);
    }
}
