package br.com.mytube.domain;

import java.util.UUID;

public interface Event {
    UUID getCorrelationId();
}
