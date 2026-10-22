package br.com.analizza.analizzatube.application.video.issuePlayback;

import br.com.analizza.analizzatube.application.ResultCommandHandler;
import br.com.analizza.analizzatube.domain.VideoNotFoundException;
import br.com.analizza.analizzatube.domain.repositories.VideoRepository;
import br.com.analizza.analizzatube.infrastructure.data.anticorruptionLayer.yeroon.YeroonClient;
import br.com.analizza.analizzatube.infrastructure.data.anticorruptionLayer.yeroon.YeroonPlayback;
import org.springframework.stereotype.Component;

@Component
public class IssuePlaybackHandler implements ResultCommandHandler<IssuePlaybackCommand, IssuePlaybackResult> {
    private final YeroonClient yeroon;
    private final VideoRepository videos;

    public IssuePlaybackHandler(YeroonClient yeroon, VideoRepository videos) {
        this.yeroon = yeroon;
        this.videos = videos;
    }

    @Override
    public IssuePlaybackResult handle(IssuePlaybackCommand command) {
        if (!videos.existsById(command.videoId())) throw new VideoNotFoundException();
        YeroonPlayback playback = yeroon.playbackToken(command.videoId());
        return new IssuePlaybackResult(playback.embedUrl().toString(), playback.expiresAt());
    }
}
