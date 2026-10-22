package br.com.analizza.analizzatube.presenter.routes.video.listVideos;

import br.com.analizza.analizzatube.application.video.listVideos.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class ListVideosRoute {
    private final ListVideosHandler handler;

    public ListVideosRoute(ListVideosHandler handler) {
        this.handler = handler;
    }

    @GetMapping("/videos")
    public ListVideosResult handle() {
        return handler.handle(new ListVideosQuery());
    }
}
