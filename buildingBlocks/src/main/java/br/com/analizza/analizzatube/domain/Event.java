package br.com.analizza.analizzatube.domain;

import java.util.UUID;

public interface Event {
    UUID getCorrelationId();
}
