package br.com.analizza.analizzatube.application.video.presignParts;

import br.com.analizza.analizzatube.application.ResultCommand;
import java.util.List;
import java.util.UUID;

public record PresignPartsCommand(UUID videoId, List<Integer> partNumbers)
        implements ResultCommand<PresignPartsResult> {}
