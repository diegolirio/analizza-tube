package br.com.mytube.presenter.exception;

import java.util.List;

public record ListErrorMessage(String error, List<ErrorMessage> details) {
}
