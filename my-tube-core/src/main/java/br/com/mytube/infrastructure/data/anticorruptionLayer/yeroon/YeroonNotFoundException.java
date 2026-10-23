package br.com.mytube.infrastructure.data.anticorruptionLayer.yeroon;

public class YeroonNotFoundException extends RuntimeException {
    public YeroonNotFoundException() {
        super("Vídeo não encontrado no yeroon.");
    }
}
