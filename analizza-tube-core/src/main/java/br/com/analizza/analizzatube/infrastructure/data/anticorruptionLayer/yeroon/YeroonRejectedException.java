package br.com.analizza.analizzatube.infrastructure.data.anticorruptionLayer.yeroon;

import java.util.List;

public class YeroonRejectedException extends RuntimeException {
    private final List<YeroonError> errors;

    public YeroonRejectedException(List<YeroonError> errors) {
        super("Yeroon rejeitou a requisição.");
        this.errors = List.copyOf(errors);
    }

    public List<YeroonError> errors() {
        return errors;
    }
}
