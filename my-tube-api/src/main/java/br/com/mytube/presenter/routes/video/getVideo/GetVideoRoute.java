package br.com.mytube.presenter.routes.video.getVideo;

import br.com.mytube.application.video.getVideo.*;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
public class GetVideoRoute {
    private final GetVideoHandler handler;

    public GetVideoRoute(GetVideoHandler handler) {
        this.handler = handler;
    }

    @GetMapping("/videos/{id}")
    public GetVideoResult handle(@PathVariable UUID id) {
        return handler.handle(new GetVideoQuery(id));
    }
}
