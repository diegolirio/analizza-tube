package br.com.mytube.application.video.initUpload;

import br.com.mytube.application.ResultCommand;

public record InitUploadCommand(String fileName, String contentType, Long sizeBytes)
        implements ResultCommand<InitUploadResult> {}
