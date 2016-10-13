package ch.sourcepond.utils.bci;

import static org.osgi.framework.FrameworkUtil.getBundle;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.osgi.framework.Bundle;

public final class Injector {
	static final ConcurrentMap<Bundle, BundleInjector> injectors = new ConcurrentHashMap<>();
	static BundleInjectorFactory factory = new BundleInjectorFactory();

	private static final BundleInjector getInjector(final Serializable pDeserializedObject) {
		if (pDeserializedObject == null) {
			throw new NullPointerException("Deserialized object cannot be null!");
		}
		final Bundle bundle = getBundle(pDeserializedObject.getClass());
		if (bundle == null) {
			throw new IllegalStateException("No OSGi environment detected!");
		}

		BundleInjector injector = injectors.get(bundle);
		if (injector == null) {
			injector = factory.newInjector(bundle);
			final BundleInjector rc = injectors.putIfAbsent(bundle, injector);
			if (rc != null) {
				injector = rc;
			} else {
				bundle.getBundleContext().addServiceListener(injector);
			}
		}
		return injector;
	}

	public static Container getContainer(final Serializable pDeserializedObject) {
		return getInjector(pDeserializedObject);
	}

	public static void injectComponents(final Serializable deserializedObject, final String[][] pComponentToFields) {
		if (pComponentToFields == null) {
			throw new NullPointerException("Component-to-field mapping array cannot be null!");
		}

		if (pComponentToFields.length > 0) {
			try {
				getInjector(deserializedObject).initDeserializedObject(deserializedObject, pComponentToFields);
			} catch (final NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
				throw new IllegalArgumentException(e.getMessage(), e);
			}
		}
	}
}
