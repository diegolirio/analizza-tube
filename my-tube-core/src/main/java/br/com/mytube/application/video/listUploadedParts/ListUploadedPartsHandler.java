package br.com.mytube.application.video.listUploadedParts;

import br.com.mytube.application.QueryHandler;
import br.com.mytube.domain.VideoNotFoundException;
import br.com.mytube.domain.repositories.VideoRepository;
import br.com.mytube.infrastructure.data.anticorruptionLayer.yeroon.YeroonClient;
import org.springframework.stereotype.Component;

@Component
public class ListUploadedPartsHandler implements QueryHandler<ListUploadedPartsQuery, ListUploadedPartsResult> {
    private final YeroonClient yeroon;
    private final VideoRepository videos;

    public ListUploadedPartsHandler(YeroonClient yeroon, VideoRepository videos) {
        this.yeroon = yeroon;
        this.videos = videos;
    }

    @Override
    public ListUploadedPartsResult handle(ListUploadedPartsQuery query) {
        if (!videos.existsById(query.videoId())) throw new VideoNotFoundException();
        return new ListUploadedPartsResult(yeroon.uploadedParts(query.videoId()).stream()
                .map(p -> new ListUploadedPartsResult.Part(p.partNumber(), p.etag(), p.sizeBytes()))
                .toList());
    }
}
