package br.com.mytube.application;

public interface QueryHandler<TQuery extends Query<TResult>, TResult> {
    TResult handle(TQuery query);
}
