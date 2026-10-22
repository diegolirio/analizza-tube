package br.com.analizza.analizzatube.application.video.presignParts;

import br.com.analizza.analizzatube.application.ResultCommandHandler;
import br.com.analizza.analizzatube.domain.VideoNotFoundException;
import br.com.analizza.analizzatube.domain.repositories.VideoRepository;
import br.com.analizza.analizzatube.infrastructure.data.anticorruptionLayer.yeroon.YeroonClient;
import org.springframework.stereotype.Component;

@Component
public class PresignPartsHandler implements ResultCommandHandler<PresignPartsCommand, PresignPartsResult> {
    private final YeroonClient yeroon;
    private final VideoRepository videos;

    public PresignPartsHandler(YeroonClient yeroon, VideoRepository videos) {
        this.yeroon = yeroon;
        this.videos = videos;
    }

    @Override
    public PresignPartsResult handle(PresignPartsCommand command) {
        if (!videos.existsById(command.videoId())) throw new VideoNotFoundException();
        return new PresignPartsResult(yeroon.partUrls(command.videoId(), command.partNumbers()).stream()
                .map(p -> new PresignPartsResult.PartUrl(p.partNumber(), p.url().toString()))
                .toList());
    }
}
