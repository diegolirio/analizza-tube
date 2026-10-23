package br.com.mytube.application.video.completeUpload;

import br.com.mytube.application.ResultCommandHandler;
import br.com.mytube.domain.RuleChecker;
import br.com.mytube.domain.VideoNotFoundException;
import br.com.mytube.domain.rules.RequiredFieldRule;
import br.com.mytube.domain.repositories.VideoRepository;
import br.com.mytube.infrastructure.data.anticorruptionLayer.yeroon.YeroonClient;
import br.com.mytube.infrastructure.data.anticorruptionLayer.yeroon.YeroonCompleted;
import br.com.mytube.infrastructure.data.anticorruptionLayer.yeroon.YeroonPart;
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
        new RuleChecker().checkAllRules(new RequiredFieldRule("parts", command.parts()));
        if (!videos.existsById(command.videoId())) throw new VideoNotFoundException();
        YeroonCompleted completed = yeroon.complete(
                command.videoId(),
                command.parts().stream().map(p -> new YeroonPart(p.partNumber(), p.etag())).toList());
        return new CompleteUploadResult(completed.videoId(), completed.status());
    }
}
