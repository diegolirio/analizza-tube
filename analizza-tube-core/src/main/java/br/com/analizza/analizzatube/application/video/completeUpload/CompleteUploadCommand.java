package br.com.analizza.analizzatube.application.video.completeUpload;

import br.com.analizza.analizzatube.application.ResultCommand;
import java.util.List;
import java.util.UUID;

public record CompleteUploadCommand(UUID videoId, List<Part> parts) implements ResultCommand<CompleteUploadResult> {
    public record Part(int partNumber, String etag) {}
}
