package br.com.analizza.analizzatube.application.webhook.receiveYeroonEvent;

import br.com.analizza.analizzatube.application.CommandBase;
import java.util.UUID;

public class ReceiveYeroonEventCommand extends CommandBase {
    private final String deliveryId;
    private final String eventType;
    private final UUID videoId;

    public ReceiveYeroonEventCommand(String deliveryId, String eventType, UUID videoId) {
        this.deliveryId = deliveryId;
        this.eventType = eventType;
        this.videoId = videoId;
    }

    public String deliveryId() {
        return deliveryId;
    }

    public String eventType() {
        return eventType;
    }

    public UUID videoId() {
        return videoId;
    }
}
