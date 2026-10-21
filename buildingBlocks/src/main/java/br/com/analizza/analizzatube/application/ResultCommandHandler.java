package br.com.analizza.analizzatube.application;

public interface ResultCommandHandler<TCommand extends ResultCommand<TResult>, TResult> {
    TResult handle(TCommand command);
}
