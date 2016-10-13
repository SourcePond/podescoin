package ch.sourcepond.utils.bci;

public interface Container {

	<T> T getComponentById(String pComponentId);

	<T> T getComponentByTypeName(String pTypeName);
}
