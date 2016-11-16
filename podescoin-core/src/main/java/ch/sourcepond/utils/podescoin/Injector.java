/*Copyright (C) 2016 Roland Hauser, <sourcepond@gmail.com>
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package ch.sourcepond.utils.podescoin;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.osgi.framework.Bundle;

import ch.sourcepond.utils.podescoin.internal.BundleInjector;

public final class Injector {
	static final ConcurrentMap<Bundle, BundleInjector> injectors = new ConcurrentHashMap<>();
	static BundleDetector detector = new BundleDetectorImpl();
	static BundleInjectorFactory factory = new BundleInjectorFactory();

	private static final BundleInjector getInjector(final Serializable pDeserializedObject) {
		if (pDeserializedObject == null) {
			throw new NullPointerException("Deserialized object cannot be null!");
		}
		final Bundle bundle = detector.getBundle(pDeserializedObject.getClass());
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
