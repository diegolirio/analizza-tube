package br.com.analizza.analizzatube.application.video.issuePlayback;

import br.com.analizza.analizzatube.application.ResultCommand;
import java.util.UUID;

public record IssuePlaybackCommand(UUID videoId) implements ResultCommand<IssuePlaybackResult> {}
