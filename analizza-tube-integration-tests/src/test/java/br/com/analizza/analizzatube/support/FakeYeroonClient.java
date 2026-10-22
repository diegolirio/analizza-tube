package br.com.analizza.analizzatube.support;

import br.com.analizza.analizzatube.infrastructure.data.anticorruptionLayer.yeroon.*;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Duble em memoria do yeroon v1: nenhuma chamada de rede sai da suite. */
public class FakeYeroonClient implements YeroonClient {

    public static final String ETAG = "\"etag-1\"";

    public final Map<UUID, YeroonVideo> videos = new ConcurrentHashMap<>();
    private final AtomicInteger calls = new AtomicInteger();
    private volatile RuntimeException nextFailure;

    public int calls() {
        return calls.get();
    }

    public void failNext(RuntimeException failure) {
        this.nextFailure = failure;
    }

    public void reset() {
        videos.clear();
        calls.set(0);
        nextFailure = null;
    }

    private void enter() {
        calls.incrementAndGet();
        RuntimeException failure = nextFailure;
        if (failure != null) {
            nextFailure = null;
            throw failure;
        }
    }

    @Override
    public YeroonUpload initUpload(String fileName, String contentType, long sizeBytes) {
        enter();
        UUID id = UUID.randomUUID();
        videos.put(id, new YeroonVideo(id, fileName, "UPLOADING", null, List.of(), null));
        return new YeroonUpload(id, 8_388_608L, (int) Math.max(1, (sizeBytes + 8_388_607L) / 8_388_608L));
    }

    @Override
    public List<YeroonPartUrl> partUrls(UUID videoId, List<Integer> partNumbers) {
        enter();
        return partNumbers.stream()
                .map(n -> new YeroonPartUrl(n, URI.create("https://r2.example/" + videoId + "/" + n)))
                .toList();
    }

    @Override
    public List<YeroonUploadedPart> uploadedParts(UUID videoId) {
        enter();
        return List.of(new YeroonUploadedPart(1, ETAG, 1024));
    }

    @Override
    public YeroonCompleted complete(UUID videoId, List<YeroonPart> parts) {
        enter();
        return new YeroonCompleted(videoId, "PROCESSING");
    }

    @Override
    public void cancel(UUID videoId) {
        enter();
        videos.remove(videoId);
    }

    @Override
    public YeroonVideo get(UUID videoId) {
        enter();
        YeroonVideo video = videos.get(videoId);
        if (video == null) {
            throw new YeroonNotFoundException();
        }
        return video;
    }

    @Override
    public List<YeroonVideoSummary> list() {
        enter();
        return videos.values().stream()
                .map(v -> new YeroonVideoSummary(v.videoId(), v.fileName(), v.status(), Instant.now(), v.thumbnailUrl()))
                .toList();
    }

    @Override
    public YeroonPlayback playbackToken(UUID videoId) {
        enter();
        return new YeroonPlayback(
                URI.create("https://play.example/embed/" + videoId), Instant.now().plusSeconds(300));
    }
}
