package br.com.mytube.application.video.listUploadedParts;

import br.com.mytube.application.QueryBase;
import java.util.UUID;

public class ListUploadedPartsQuery extends QueryBase<ListUploadedPartsResult> {
    private final UUID videoId;

    public ListUploadedPartsQuery(UUID videoId) {
        this.videoId = videoId;
    }

    public UUID videoId() {
        return videoId;
    }
}
