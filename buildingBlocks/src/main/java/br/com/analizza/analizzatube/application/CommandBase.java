package br.com.analizza.analizzatube.application;

import java.util.UUID;

public abstract class CommandBase implements Command {
    private final UUID id = UUID.randomUUID();

    @Override
    public UUID getId() {
        return id;
    }
}
