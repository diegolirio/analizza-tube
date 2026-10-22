package br.com.analizza.analizzatube.presenter.routes.video.initUpload;

import br.com.analizza.analizzatube.application.video.initUpload.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class InitUploadRoute {
    private final InitUploadHandler handler;

    public InitUploadRoute(InitUploadHandler handler) {
        this.handler = handler;
    }

    public record InitUploadRequest(String fileName, String contentType, Long sizeBytes) {}

    @PostMapping("/videos/uploads")
    @ResponseStatus(HttpStatus.CREATED)
    public InitUploadResult handle(@RequestBody InitUploadRequest request) {
        return handler.handle(new InitUploadCommand(request.fileName(), request.contentType(), request.sizeBytes()));
    }
}
