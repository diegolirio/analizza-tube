package br.com.analizza.analizzatube.application.video.cancelUpload;

import br.com.analizza.analizzatube.application.CommandHandler;
import br.com.analizza.analizzatube.domain.VideoNotFoundException;
import br.com.analizza.analizzatube.domain.repositories.VideoRepository;
import br.com.analizza.analizzatube.infrastructure.data.anticorruptionLayer.yeroon.YeroonClient;
import org.springframework.stereotype.Component;

@Component
public class CancelUploadHandler implements CommandHandler<CancelUploadCommand> {
    private final YeroonClient yeroon;
    private final VideoRepository videos;

    public CancelUploadHandler(YeroonClient yeroon, VideoRepository videos) {
        this.yeroon = yeroon;
        this.videos = videos;
    }

    @Override
    public void handle(CancelUploadCommand command) {
        if (!videos.existsById(command.videoId())) throw new VideoNotFoundException();
        yeroon.cancel(command.videoId());
    }
}
