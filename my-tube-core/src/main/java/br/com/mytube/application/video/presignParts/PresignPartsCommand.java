package br.com.mytube.application.video.presignParts;

import br.com.mytube.application.ResultCommand;
import java.util.List;
import java.util.UUID;

public record PresignPartsCommand(UUID videoId, List<Integer> partNumbers)
        implements ResultCommand<PresignPartsResult> {}
