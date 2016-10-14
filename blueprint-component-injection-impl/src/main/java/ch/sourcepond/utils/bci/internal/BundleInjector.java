package ch.sourcepond.utils.bci.internal;

import java.io.Serializable;

import org.osgi.framework.ServiceListener;

import ch.sourcepond.utils.bci.Container;

public interface BundleInjector extends Container, ServiceListener {

	void initDeserializedObject(Serializable pObj, String[][] pComponentToFields)
			throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException;

}