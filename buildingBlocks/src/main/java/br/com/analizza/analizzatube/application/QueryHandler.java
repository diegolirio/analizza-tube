package br.com.analizza.analizzatube.application;

public interface QueryHandler<TQuery extends Query<TResult>, TResult> {
    TResult handle(TQuery query);
}
