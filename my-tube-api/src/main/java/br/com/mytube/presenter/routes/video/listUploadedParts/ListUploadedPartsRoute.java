package br.com.mytube.presenter.routes.video.listUploadedParts;

import br.com.mytube.application.video.listUploadedParts.*;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
public class ListUploadedPartsRoute {
    private final ListUploadedPartsHandler handler;

    public ListUploadedPartsRoute(ListUploadedPartsHandler handler) {
        this.handler = handler;
    }

    @GetMapping("/videos/{id}/upload/parts")
    public ListUploadedPartsResult handle(@PathVariable UUID id) {
        return handler.handle(new ListUploadedPartsQuery(id));
    }
}
