package br.com.analizza.analizzatube.infrastructure.data.anticorruptionLayer.yeroon;

public class YeroonUnavailableException extends RuntimeException {
    public YeroonUnavailableException(String message) {
        super(message);
    }

    public YeroonUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
