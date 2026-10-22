package br.com.analizza.analizzatube.domain;

public class VideoNotFoundException extends RuntimeException {
    public VideoNotFoundException() {
        super("Vídeo não encontrado.");
    }
}
