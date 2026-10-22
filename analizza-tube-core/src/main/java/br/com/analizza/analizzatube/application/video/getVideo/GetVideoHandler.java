package br.com.analizza.analizzatube.application.video.getVideo;

import br.com.analizza.analizzatube.application.QueryHandler;
import br.com.analizza.analizzatube.domain.VideoNotFoundException;
import br.com.analizza.analizzatube.domain.repositories.VideoRepository;
import br.com.analizza.analizzatube.infrastructure.data.anticorruptionLayer.yeroon.YeroonClient;
import br.com.analizza.analizzatube.infrastructure.data.anticorruptionLayer.yeroon.YeroonVideo;
import org.springframework.stereotype.Component;

@Component
public class GetVideoHandler implements QueryHandler<GetVideoQuery, GetVideoResult> {
    private final YeroonClient yeroon;
    private final VideoRepository videos;

    public GetVideoHandler(YeroonClient yeroon, VideoRepository videos) {
        this.yeroon = yeroon;
        this.videos = videos;
    }

    @Override
    public GetVideoResult handle(GetVideoQuery query) {
        if (!videos.existsById(query.videoId())) throw new VideoNotFoundException();
        YeroonVideo v = yeroon.get(query.videoId());
        return new GetVideoResult(
                v.videoId(),
                v.fileName(),
                v.status(),
                v.failureReason(),
                v.renditions(),
                v.thumbnailUrl() == null ? null : v.thumbnailUrl().toString());
    }
}
