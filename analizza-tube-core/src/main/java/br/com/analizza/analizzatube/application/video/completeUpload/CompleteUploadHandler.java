package br.com.analizza.analizzatube.application.video.completeUpload;

import br.com.analizza.analizzatube.application.ResultCommandHandler;
import br.com.analizza.analizzatube.domain.VideoNotFoundException;
import br.com.analizza.analizzatube.domain.repositories.VideoRepository;
import br.com.analizza.analizzatube.infrastructure.data.anticorruptionLayer.yeroon.YeroonClient;
import br.com.analizza.analizzatube.infrastructure.data.anticorruptionLayer.yeroon.YeroonCompleted;
import br.com.analizza.analizzatube.infrastructure.data.anticorruptionLayer.yeroon.YeroonPart;
import org.springframework.stereotype.Component;

@Component
public class CompleteUploadHandler implements ResultCommandHandler<CompleteUploadCommand, CompleteUploadResult> {
    private final YeroonClient yeroon;
    private final VideoRepository videos;

    public CompleteUploadHandler(YeroonClient yeroon, VideoRepository videos) {
        this.yeroon = yeroon;
        this.videos = videos;
    }

    @Override
    public CompleteUploadResult handle(CompleteUploadCommand command) {
        if (!videos.existsById(command.videoId())) throw new VideoNotFoundException();
        YeroonCompleted completed = yeroon.complete(
                command.videoId(),
                command.parts().stream().map(p -> new YeroonPart(p.partNumber(), p.etag())).toList());
        return new CompleteUploadResult(completed.videoId(), completed.status());
    }
}
