package br.com.analizza.analizzatube.presenter.exception;

import java.util.List;

public record ListErrorMessage(String error, List<ErrorMessage> details) {
}
