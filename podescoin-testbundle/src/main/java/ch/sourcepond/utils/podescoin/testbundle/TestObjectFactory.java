package ch.sourcepond.utils.podescoin.testbundle;

public interface TestObjectFactory {

	FieldInjectionObject getFieldInjectionObject() throws Exception;

	FieldInjectionObjectWithComponentId getFieldInjectionObjectWithComponentId() throws Exception;

	InjectorMethodObject getInjectorMethodObject() throws Exception;
}