package br.com.analizza.analizzatube.application.video.cancelUpload;

import br.com.analizza.analizzatube.application.CommandBase;
import java.util.UUID;

public class CancelUploadCommand extends CommandBase {
    private final UUID videoId;

    public CancelUploadCommand(UUID videoId) {
        this.videoId = videoId;
    }

    public UUID videoId() {
        return videoId;
    }
}
