package br.com.mytube.application.video.completeUpload;

import br.com.mytube.application.ResultCommand;
import java.util.List;
import java.util.UUID;

public record CompleteUploadCommand(UUID videoId, List<Part> parts) implements ResultCommand<CompleteUploadResult> {
    public record Part(int partNumber, String etag) {}
}
