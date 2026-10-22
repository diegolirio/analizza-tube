package br.com.analizza.analizzatube.application.video.initUpload;

import br.com.analizza.analizzatube.application.ResultCommandHandler;
import br.com.analizza.analizzatube.domain.Video;
import br.com.analizza.analizzatube.domain.repositories.VideoRepository;
import br.com.analizza.analizzatube.infrastructure.data.anticorruptionLayer.yeroon.YeroonClient;
import br.com.analizza.analizzatube.infrastructure.data.anticorruptionLayer.yeroon.YeroonUpload;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class InitUploadHandler implements ResultCommandHandler<InitUploadCommand, InitUploadResult> {
    private final YeroonClient yeroon;
    private final VideoRepository videos;

    public InitUploadHandler(YeroonClient yeroon, VideoRepository videos) {
        this.yeroon = yeroon;
        this.videos = videos;
    }

    @Override
    public InitUploadResult handle(InitUploadCommand command) {
        YeroonUpload upload = yeroon.initUpload(command.fileName(), command.contentType(), command.sizeBytes());
        videos.save(Video.register(upload.videoId(), Instant.now()));
        return new InitUploadResult(upload.videoId(), upload.partSizeBytes(), upload.partCount());
    }
}
