package br.com.mytube.application;

public interface ResultCommandHandler<TCommand extends ResultCommand<TResult>, TResult> {
    TResult handle(TCommand command);
}
