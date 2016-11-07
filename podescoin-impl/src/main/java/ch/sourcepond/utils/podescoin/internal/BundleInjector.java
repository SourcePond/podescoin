package ch.sourcepond.utils.podescoin.internal;

import java.io.Serializable;

import org.osgi.framework.ServiceListener;

import ch.sourcepond.utils.podescoin.Container;

public interface BundleInjector extends Container, ServiceListener {

	void initDeserializedObject(Serializable pObj, String[][] pComponentToFields)
			throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException;

}