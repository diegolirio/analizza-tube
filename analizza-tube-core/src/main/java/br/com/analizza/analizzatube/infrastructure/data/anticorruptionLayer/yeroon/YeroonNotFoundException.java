package br.com.analizza.analizzatube.infrastructure.data.anticorruptionLayer.yeroon;

public class YeroonNotFoundException extends RuntimeException {
    public YeroonNotFoundException() {
        super("Vídeo não encontrado no yeroon.");
    }
}
