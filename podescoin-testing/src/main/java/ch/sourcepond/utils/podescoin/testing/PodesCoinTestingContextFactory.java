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
package ch.sourcepond.utils.podescoin.testing;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Named;

import ch.sourcepond.utils.podescoin.Injector;

public class PodesCoinTestingContextFactory {

	private static final Object ORIGINAL_DETECTOR;
	static final Class<?> BUNDLE_DETECTOR_INTERFACE;
	static final Method GET_BUNDLE_METHOD;
	static final String OSGI_BLUEPRINT_CONTAINER_SYMBOLICNAME_FILTER;
	static final Field DETECTOR_FIELD;

	static {
		try {
			final ClassLoader ldr = PodesCoinTestingContextFactory.class.getClassLoader();
			BUNDLE_DETECTOR_INTERFACE = ldr.loadClass("ch.sourcepond.utils.podescoin.BundleDetector");
			GET_BUNDLE_METHOD = BUNDLE_DETECTOR_INTERFACE.getMethod("getBundle", Class.class);
			GET_BUNDLE_METHOD.setAccessible(true);

			final Class<?> bundleInjectorImpl = ldr.loadClass("ch.sourcepond.utils.podescoin.BundleInjectorImpl");
			final Field osgiBlueprintContainerSymbolicnameFilter = bundleInjectorImpl
					.getDeclaredField("OSGI_BLUEPRINT_CONTAINER_SYMBOLICNAME_FILTER");
			osgiBlueprintContainerSymbolicnameFilter.setAccessible(true);
			OSGI_BLUEPRINT_CONTAINER_SYMBOLICNAME_FILTER = (String) osgiBlueprintContainerSymbolicnameFilter
					.get(bundleInjectorImpl);
			DETECTOR_FIELD = Injector.class.getDeclaredField("detector");
			DETECTOR_FIELD.setAccessible(true);
			ORIGINAL_DETECTOR = DETECTOR_FIELD.get(Injector.class);
		} catch (final ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException
				| IllegalAccessException | NoSuchMethodException e) {
			throw new AssertionError(
					String.format("Class %s could not be initialized", PodesCoinTestingContextFactory.class.getName()), e);
		}
	}

	public static PodesCoinTestingContext newContext() {
		try {
			return new PodesCoinTestingContext();
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new AssertionError(
					String.format("Instance of %s could not be created!", PodesCoinTestingContext.class.getName()), e);
		}
	}

	private static List<Field> collectFields(final Class<?> pClassOrNull, final List<Field> pFields) {
		if (pClassOrNull != null) {
			for (final Field f : pClassOrNull.getDeclaredFields()) {
				if (f.isAnnotationPresent(Named.class)) {
					f.setAccessible(true);
					pFields.add(f);
				}
			}
			return collectFields(pClassOrNull.getSuperclass(), pFields);
		}
		return pFields;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static PodesCoinTestingContext newContext(final Object pTest) {
		final PodesCoinTestingContext context = newContext();
		for (final Field f : collectFields(pTest.getClass(), new LinkedList<>())) {
			final Named component = f.getAnnotation(Named.class);
			try {
				context.addComponent(f.get(pTest), component.value(), (Class) f.getType());
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		return context;
	}

	static void setDetector(final Object detector) {
		try {
			DETECTOR_FIELD.set(Injector.class, detector);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new AssertionError(String.format("Field '%s' could not be set on %s", DETECTOR_FIELD.getName(),
					Injector.class.getName()), e);
		}
	}

	static void resetDetector() {
		setDetector(ORIGINAL_DETECTOR);
	}
}
