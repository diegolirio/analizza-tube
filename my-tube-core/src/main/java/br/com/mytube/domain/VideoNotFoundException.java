package br.com.mytube.domain;

public class VideoNotFoundException extends RuntimeException {
    public VideoNotFoundException() {
        super("Vídeo não encontrado.");
    }
}
