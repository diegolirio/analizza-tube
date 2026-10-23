package br.com.mytube.application.video.issuePlayback;

import br.com.mytube.application.ResultCommand;
import java.util.UUID;

public record IssuePlaybackCommand(UUID videoId) implements ResultCommand<IssuePlaybackResult> {}
