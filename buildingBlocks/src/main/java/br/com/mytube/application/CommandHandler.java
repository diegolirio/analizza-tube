package br.com.mytube.application;

public interface CommandHandler<TCommand extends Command> {
    void handle(TCommand command);
}
