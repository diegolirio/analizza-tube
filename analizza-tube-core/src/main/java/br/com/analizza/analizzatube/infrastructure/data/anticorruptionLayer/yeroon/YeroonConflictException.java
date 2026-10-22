package br.com.analizza.analizzatube.infrastructure.data.anticorruptionLayer.yeroon;

public class YeroonConflictException extends RuntimeException {
    public YeroonConflictException() {
        super("Estado do vídeo no yeroon não permite a operação.");
    }
}
