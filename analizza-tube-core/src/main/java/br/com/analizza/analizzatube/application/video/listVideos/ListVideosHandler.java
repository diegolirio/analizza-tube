package br.com.analizza.analizzatube.application.video.listVideos;

import br.com.analizza.analizzatube.application.QueryHandler;
import br.com.analizza.analizzatube.domain.repositories.VideoRepository;
import br.com.analizza.analizzatube.infrastructure.data.anticorruptionLayer.yeroon.YeroonClient;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ListVideosHandler implements QueryHandler<ListVideosQuery, ListVideosResult> {
    private final YeroonClient yeroon;
    private final VideoRepository videos;

    public ListVideosHandler(YeroonClient yeroon, VideoRepository videos) {
        this.yeroon = yeroon;
        this.videos = videos;
    }

    @Override
    public ListVideosResult handle(ListVideosQuery query) {
        Set<UUID> known = videos.findAllIds();
        return new ListVideosResult(yeroon.list().stream()
                .filter(v -> known.contains(v.videoId()))
                .map(v -> new ListVideosResult.Item(
                        v.videoId(),
                        v.fileName(),
                        v.status(),
                        v.createdAt(),
                        v.thumbnailUrl() == null ? null : v.thumbnailUrl().toString()))
                .toList());
    }
}
