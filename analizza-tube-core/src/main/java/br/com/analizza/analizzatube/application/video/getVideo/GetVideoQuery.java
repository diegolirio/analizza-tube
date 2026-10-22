package br.com.analizza.analizzatube.application.video.getVideo;

import br.com.analizza.analizzatube.application.QueryBase;
import java.util.UUID;

public class GetVideoQuery extends QueryBase<GetVideoResult> {
    private final UUID videoId;

    public GetVideoQuery(UUID videoId) {
        this.videoId = videoId;
    }

    public UUID videoId() {
        return videoId;
    }
}
