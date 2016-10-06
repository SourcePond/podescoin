package ch.sourcepond.utils.bci;

import static org.osgi.framework.FrameworkUtil.getBundle;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.osgi.framework.Bundle;

public final class Injector {
	static final ConcurrentMap<Bundle, BundleInjector> injectors = new ConcurrentHashMap<>();
	static BundleInjectorFactory factory = new BundleInjectorFactory();

	private static final BundleInjector getInjector(final Bundle pBundle) {
		BundleInjector injector = injectors.get(pBundle);
		if (injector == null) {
			injector = factory.newInjector(pBundle);
			final BundleInjector rc = injectors.putIfAbsent(pBundle, injector);
			if (rc != null) {
				injector = rc;
			} else {
				pBundle.getBundleContext().addServiceListener(injector);
			}
		}
		return injector;
	}

	public static void injectComponents(final Serializable deserializedObject, final String[][] pComponentToFields) {
		if (deserializedObject == null) {
			throw new NullPointerException("Deserialized object cannot be null!");
		}
		if (pComponentToFields == null) {
			throw new NullPointerException("Component-to-field mapping array cannot be null!");
		}

		if (pComponentToFields.length > 0) {
			final Bundle bundle = getBundle(deserializedObject.getClass());
			if (bundle == null) {
				throw new IllegalStateException("No OSGi environment detected!");
			}

			try {
				getInjector(bundle).initDeserializedObject(deserializedObject, pComponentToFields);
			} catch (final NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
				throw new IllegalArgumentException(e.getMessage(), e);
			}
		}
	}
}
