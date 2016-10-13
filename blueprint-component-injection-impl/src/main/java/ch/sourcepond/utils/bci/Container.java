package ch.sourcepond.utils.bci;

public interface Container {

	<T> T getComponentById(String pComponentId, String pExpectedTypeName, int pParameterIndex);

	<T> T getComponentByTypeName(String pTypeName, int pParameterIndex);
}
