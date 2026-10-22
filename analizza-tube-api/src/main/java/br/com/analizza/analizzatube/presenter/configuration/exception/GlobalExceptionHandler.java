package br.com.analizza.analizzatube.presenter.configuration.exception;

import br.com.analizza.analizzatube.domain.BusinessRulesBrokenException;
import br.com.analizza.analizzatube.domain.VideoNotFoundException;
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
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Vídeo não encontrado.");
    }
}
