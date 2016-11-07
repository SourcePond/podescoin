package ch.sourcepond.utils.podescoin;

public interface Container {

	<T> T getComponentById(String pComponentId, String pExpectedTypeName, int pParameterIndex);

	<T> T getComponentByTypeName(String pTypeName, int pParameterIndex);
}
