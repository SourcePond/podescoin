package ch.sourcepond.utils.bci;

public interface Container {

	<T> T getComponentById(String pComponentId, String pExpectedTypeName);

	<T> T getComponentByTypeName(String pTypeName);
}
