package br.com.analizza.analizzatube.presenter.routes.video.presignParts;

import br.com.analizza.analizzatube.application.video.presignParts.*;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
public class PresignUploadPartsRoute {
    private final PresignPartsHandler handler;

    public PresignUploadPartsRoute(PresignPartsHandler handler) {
        this.handler = handler;
    }

    public record PresignUploadPartsRequest(List<Integer> partNumbers) {}

    @PostMapping("/videos/{id}/upload/part-urls")
    public PresignPartsResult handle(@PathVariable UUID id, @RequestBody PresignUploadPartsRequest request) {
        return handler.handle(new PresignPartsCommand(id, request.partNumbers()));
    }
}
