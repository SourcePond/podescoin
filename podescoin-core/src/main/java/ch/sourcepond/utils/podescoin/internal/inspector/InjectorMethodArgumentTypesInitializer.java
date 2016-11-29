package ch.sourcepond.utils.podescoin.internal.inspector;

@FunctionalInterface
interface InjectorMethodArgumentTypesInitializer {

	void initArgumentTypes(boolean pIncludeStream, String pMethodName, String pMethodDesc);
}
