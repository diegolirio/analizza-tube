package br.com.mytube.presenter.routes.video.cancelUpload;

import br.com.mytube.application.video.cancelUpload.*;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class CancelUploadRoute {
    private final CancelUploadHandler handler;

    public CancelUploadRoute(CancelUploadHandler handler) {
        this.handler = handler;
    }

    @DeleteMapping("/videos/{id}/upload")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handle(@PathVariable UUID id) {
        handler.handle(new CancelUploadCommand(id));
    }
}
