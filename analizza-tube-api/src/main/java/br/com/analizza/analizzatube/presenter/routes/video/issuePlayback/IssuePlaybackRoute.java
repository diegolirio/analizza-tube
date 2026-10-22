package br.com.analizza.analizzatube.presenter.routes.video.issuePlayback;

import br.com.analizza.analizzatube.application.video.issuePlayback.*;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class IssuePlaybackRoute {
    private final IssuePlaybackHandler handler;

    public IssuePlaybackRoute(IssuePlaybackHandler handler) {
        this.handler = handler;
    }

    @PostMapping("/videos/{id}/playback")
    public ResponseEntity<IssuePlaybackResult> handle(@PathVariable UUID id) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(handler.handle(new IssuePlaybackCommand(id)));
    }
}
