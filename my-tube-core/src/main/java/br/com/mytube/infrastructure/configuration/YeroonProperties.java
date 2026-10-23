package br.com.mytube.infrastructure.configuration;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("yeroon")
public record YeroonProperties(URI apiUrl, String apiKey, String webhookSecret, Duration timeout) {

    private static final URI DEFAULT_API_URL = URI.create("https://api.dev.play.yeroon.video");
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    public YeroonProperties {
        if (apiUrl == null) {
            apiUrl = DEFAULT_API_URL;
        }
        if (timeout == null) {
            timeout = DEFAULT_TIMEOUT;
        }
    }
}
