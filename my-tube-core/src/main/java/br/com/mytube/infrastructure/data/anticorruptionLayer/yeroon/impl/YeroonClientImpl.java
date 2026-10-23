package br.com.mytube.infrastructure.data.anticorruptionLayer.yeroon.impl;

import br.com.mytube.infrastructure.configuration.YeroonProperties;
import br.com.mytube.infrastructure.data.anticorruptionLayer.yeroon.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.IOException;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.json.JsonMapper;

@Component
public class YeroonClientImpl implements YeroonClient {

    private static final Logger log = LoggerFactory.getLogger(YeroonClientImpl.class);
    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private final RestClient http;

    public YeroonClientImpl(YeroonProperties properties) {
        HttpClient jdk = HttpClient.newBuilder().connectTimeout(properties.timeout()).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(jdk);
        factory.setReadTimeout(properties.timeout());
        String key = properties.apiKey() == null ? "" : properties.apiKey();
        if (key.isBlank()) {
            log.warn("YEROON_API_KEY (yeroon.api-key) esta vazia: toda chamada ao yeroon play sera recusada.");
        }
        this.http = RestClient.builder()
                .baseUrl(properties.apiUrl().toString())
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + key)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    throw map(response);
                })
                .build();
    }

    @Override
    public YeroonUpload initUpload(String fileName, String contentType, long sizeBytes) {
        Map<String, Object> body = Map.of("fileName", fileName, "contentType", contentType, "sizeBytes", sizeBytes);
        return call(() -> http.post()
                .uri("/v1/videos/uploads")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(YeroonUpload.class));
    }

    @Override
    public List<YeroonPartUrl> partUrls(UUID videoId, List<Integer> partNumbers) {
        Urls urls = call(() -> http.post()
                .uri("/v1/videos/{id}/upload/part-urls", videoId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("partNumbers", partNumbers))
                .retrieve()
                .body(Urls.class));
        return urls.urls();
    }

    @Override
    public List<YeroonUploadedPart> uploadedParts(UUID videoId) {
        Parts parts = call(() -> http.get()
                .uri("/v1/videos/{id}/upload/parts", videoId)
                .retrieve()
                .body(Parts.class));
        return parts.parts();
    }

    @Override
    public YeroonCompleted complete(UUID videoId, List<YeroonPart> parts) {
        return call(() -> http.post()
                .uri("/v1/videos/{id}/upload/complete", videoId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("parts", parts))
                .retrieve()
                .body(YeroonCompleted.class));
    }

    @Override
    public void cancel(UUID videoId) {
        call(() -> http.delete().uri("/v1/videos/{id}/upload", videoId).retrieve().toBodilessEntity());
    }

    @Override
    public YeroonVideo get(UUID videoId) {
        return call(() -> http.get().uri("/v1/videos/{id}", videoId).retrieve().body(YeroonVideo.class));
    }

    @Override
    public List<YeroonVideoSummary> list() {
        Videos videos = call(() -> http.get().uri("/v1/videos").retrieve().body(Videos.class));
        return videos.videos();
    }

    @Override
    public YeroonPlayback playbackToken(UUID videoId) {
        return call(() -> http.post()
                .uri("/v1/videos/{id}/playback-token", videoId)
                .retrieve()
                .body(YeroonPlayback.class));
    }

    private <T> T call(Supplier<T> request) {
        try {
            return request.get();
        } catch (YeroonRejectedException | YeroonNotFoundException | YeroonConflictException
                | YeroonUnavailableException e) {
            throw e;
        } catch (RestClientException e) {
            // A mensagem da causa pode citar a URL, nunca o cabeçalho Authorization.
            // Registra so a classe da excecao: sem mensagem, cabecalhos nem chave.
            log.warn("yeroon indisponivel ({})", e.getClass().getSimpleName());
            throw new YeroonUnavailableException("Serviço de vídeo indisponível.", e);
        }
    }

    private static RuntimeException map(ClientHttpResponse response) throws IOException {
        HttpStatusCode status = response.getStatusCode();
        if (status.value() == HttpStatus.UNPROCESSABLE_CONTENT.value()) {
            return new YeroonRejectedException(readErrors(response));
        }
        if (status.value() == HttpStatus.NOT_FOUND.value()) {
            return new YeroonNotFoundException();
        }
        if (status.value() == HttpStatus.CONFLICT.value()) {
            return new YeroonConflictException();
        }
        if (status.value() == HttpStatus.UNAUTHORIZED.value() || status.value() == HttpStatus.FORBIDDEN.value()) {
            // Chave recusada: configuração, não do usuário. Nunca registrar a chave inteira.
            log.error("yeroon recusou a chave de API (status {})", status.value());
        }
        log.warn("yeroon indisponivel (status {})", status.value());
        return new YeroonUnavailableException("Serviço de vídeo indisponível (status " + status.value() + ").");
    }

    private static List<YeroonError> readErrors(ClientHttpResponse response) {
        try {
            Errors errors = MAPPER.readValue(response.getBody(), Errors.class);
            return errors.errors() == null ? List.of() : errors.errors();
        } catch (RuntimeException | IOException e) {
            return List.of();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Urls(List<YeroonPartUrl> urls) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Parts(List<YeroonUploadedPart> parts) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Videos(List<YeroonVideoSummary> videos) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Errors(List<YeroonError> errors) {}
}
