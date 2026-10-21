package br.com.analizza.analizzatube.application;

public interface CommandHandler<TCommand extends Command> {
    void handle(TCommand command);
}
