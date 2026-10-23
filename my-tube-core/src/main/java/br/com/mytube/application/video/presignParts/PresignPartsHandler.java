package br.com.mytube.application.video.presignParts;

import br.com.mytube.application.ResultCommandHandler;
import br.com.mytube.domain.RuleChecker;
import br.com.mytube.domain.VideoNotFoundException;
import br.com.mytube.domain.rules.RequiredFieldRule;
import br.com.mytube.domain.repositories.VideoRepository;
import br.com.mytube.infrastructure.data.anticorruptionLayer.yeroon.YeroonClient;
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
        new RuleChecker().checkAllRules(new RequiredFieldRule("partNumbers", command.partNumbers()));
        if (!videos.existsById(command.videoId())) throw new VideoNotFoundException();
        return new PresignPartsResult(yeroon.partUrls(command.videoId(), command.partNumbers()).stream()
                .map(p -> new PresignPartsResult.PartUrl(p.partNumber(), p.url().toString()))
                .toList());
    }
}
