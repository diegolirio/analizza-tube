package br.com.analizza.analizzatube.infrastructure.data.anticorruptionLayer.yeroon;

import java.util.List;
import java.util.UUID;

public interface YeroonClient {
    YeroonUpload initUpload(String fileName, String contentType, long sizeBytes);

    List<YeroonPartUrl> partUrls(UUID videoId, List<Integer> partNumbers);

    List<YeroonUploadedPart> uploadedParts(UUID videoId);

    YeroonCompleted complete(UUID videoId, List<YeroonPart> parts);

    void cancel(UUID videoId);

    YeroonVideo get(UUID videoId);

    List<YeroonVideoSummary> list();

    YeroonPlayback playbackToken(UUID videoId);
}
