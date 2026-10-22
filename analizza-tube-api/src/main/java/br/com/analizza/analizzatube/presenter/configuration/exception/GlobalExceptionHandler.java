package br.com.analizza.analizzatube.presenter.configuration.exception;

import br.com.analizza.analizzatube.domain.BusinessRulesBrokenException;
import br.com.analizza.analizzatube.domain.VideoNotFoundException;
import br.com.analizza.analizzatube.infrastructure.data.anticorruptionLayer.yeroon.*;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessRulesBrokenException.class)
    ProblemDetail businessRulesBroken(BusinessRulesBrokenException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY, "Uma ou mais regras de negócio foram quebradas.");
        List<Map<String, String>> errors = ex.getBrokenRules().stream()
                .map(rule -> Map.of("code", rule.getId(), "message", rule.getMessage()))
                .toList();
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(VideoNotFoundException.class)
    ProblemDetail videoNotFound(VideoNotFoundException ex) {
        return videoNotFoundProblem();
    }

    // O Spring preenche "instance" com o path da requisicao quando ela e nula, e
    // o path carrega o id; um valor explicito mantem o corpo do 404 generico.
    private static ProblemDetail videoNotFoundProblem() {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Vídeo não encontrado.");
        problem.setInstance(URI.create("about:blank"));
        return problem;
    }

    @ExceptionHandler(YeroonRejectedException.class)
    ProblemDetail yeroonRejected(YeroonRejectedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY, "Uma ou mais regras de negócio foram quebradas.");
        problem.setProperty("errors", ex.errors().stream()
                .map(error -> Map.of("code", error.code(), "message", error.message()))
                .toList());
        return problem;
    }

    @ExceptionHandler(YeroonNotFoundException.class)
    ProblemDetail yeroonNotFound(YeroonNotFoundException ex) {
        return videoNotFoundProblem();
    }

    @ExceptionHandler(YeroonConflictException.class)
    ProblemDetail yeroonConflict(YeroonConflictException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "O vídeo não está em estado de receber essa operação.");
    }

    @ExceptionHandler(YeroonUnavailableException.class)
    ProblemDetail yeroonUnavailable(YeroonUnavailableException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, "Serviço de vídeo indisponível.");
    }
}
