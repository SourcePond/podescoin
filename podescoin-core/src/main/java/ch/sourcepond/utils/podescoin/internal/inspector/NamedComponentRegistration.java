package ch.sourcepond.utils.podescoin.internal.inspector;

@FunctionalInterface
interface NamedComponentRegistration {

	void registerNamedComponent(String pComponentId, int pParameterIndex);
}
