package ch.sourcepond.utils.podescoin.api;

import java.io.Serializable;

public interface TestObject<T extends Serializable> {

	T getKey() throws Exception;
}
