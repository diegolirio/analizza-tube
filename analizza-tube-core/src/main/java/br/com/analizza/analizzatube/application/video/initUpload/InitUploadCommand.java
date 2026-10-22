package br.com.analizza.analizzatube.application.video.initUpload;

import br.com.analizza.analizzatube.application.ResultCommand;

public record InitUploadCommand(String fileName, String contentType, Long sizeBytes)
        implements ResultCommand<InitUploadResult> {}
