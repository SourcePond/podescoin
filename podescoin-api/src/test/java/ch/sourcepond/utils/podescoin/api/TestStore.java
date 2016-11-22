package ch.sourcepond.utils.podescoin.api;

import java.io.Serializable;

public interface TestStore {

	<T extends Serializable> TestObject<T> load(T pKey) throws Exception;
}
