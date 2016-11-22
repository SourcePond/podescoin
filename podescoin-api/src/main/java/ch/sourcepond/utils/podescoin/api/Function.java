package ch.sourcepond.utils.podescoin.api;

@FunctionalInterface
public interface Function<T, R> {

	R apply(T pSource) throws Exception;
}
