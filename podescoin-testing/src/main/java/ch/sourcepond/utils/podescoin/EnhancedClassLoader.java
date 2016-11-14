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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.inject.Inject;

import ch.sourcepond.utils.podescoin.internal.Activator;

final class EnhancedClassLoader extends ClassLoader {
	private final Map<String, Class<?>> enhancedClasses = new HashMap<>();
	private final Map<String, Class<?>> originalClasses = new HashMap<>();
	private final Set<String> registeredInterfaces = new HashSet<>();
	private final Set<String> detectedInterfaces = new HashSet<>();

	EnhancedClassLoader(final ClassLoader pParent) {
		super(pParent);
	}

	@Override
	protected Class<?> findClass(final String name) throws ClassNotFoundException {
		Class<?> cl = enhancedClasses.get(name);
		if (cl == null) {
			cl = getParent().loadClass(name);
		} else {
			System.out.println(cl);
		}
		return cl;
	}

	private <T extends AccessibleObject> boolean hasAnnotation(final Class<?> pClass,
			final Function<Class<?>, T[]> pFunc, final Predicate<AnnotatedElement> pTester) {
		if (pClass != null) {
			for (final T member : pFunc.apply(pClass)) {
				if (pTester.test(member)) {
					return true;
				}
			}
			return hasAnnotation(pClass.getSuperclass(), pFunc, pTester);
		}
		return false;
	}

	private Class<?>[] determineTypeParameterClass(final Type pGenericType) throws ClassNotFoundException {
		Class<?>[] result = null;
		if (pGenericType != null && pGenericType instanceof ParameterizedType) {
			final Type[] actualTypeArguments = ((ParameterizedType) pGenericType).getActualTypeArguments();
			if (actualTypeArguments.length > 0) {
				result = new Class<?>[actualTypeArguments.length];
				for (int i = 0; i < actualTypeArguments.length; i++) {
					try {
						result[i] = getClass().getClassLoader().loadClass(actualTypeArguments[i].getTypeName());
					} catch (final ClassNotFoundException e) {
						result[i] = Object.class;
					}

					if (result[i].isInterface()) {
						detectedInterfaces.add(result[i].getName());
					}
				}
			}
		}

		if (result == null) {
			result = new Class<?>[0];
		}

		return result;
	}

	private Class<?> enhanceClass(final Class<?> pClass) throws ClassNotFoundException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (final InputStream in = new BufferedInputStream(
				getClass().getResourceAsStream("/" + pClass.getName().replace('.', '/') + ".class"))) {
			final byte[] buffer = new byte[1024];
			int read = in.read(buffer);
			while (read != -1) {
				out.write(buffer, 0, read);
				read = in.read(buffer);
			}
		} catch (final IOException e) {
			throw new ClassNotFoundException(e.getMessage(), e);
		}
		final byte[] enhancedClassData = Activator.transform(out.toByteArray());
		return defineClass(pClass.getName(), enhancedClassData, 0, enhancedClassData.length,
				getClass().getProtectionDomain());
	}

	private void register(final Class<?> pClass, final Field pField, final Set<Class<?>> pVisitedClasses)
			throws ClassNotFoundException {
		if (pClass != null && !pVisitedClasses.contains(pClass) && !enhancedClasses.containsKey(pClass.getName())) {
			pVisitedClasses.add(pClass);
			if (pClass.isArray()) {
				register(pClass.getComponentType(), null, pVisitedClasses);
			} else if (Collection.class.isAssignableFrom(pClass) || Map.class.isAssignableFrom(pClass)) {
				for (final Class<?> actualType : determineTypeParameterClass(pField.getGenericType())) {
					register(actualType, null, pVisitedClasses);
				}
			} else {
				final Predicate<AnnotatedElement> tester = e -> e.isAnnotationPresent(Inject.class);
				if (hasAnnotation(pClass, cl -> cl.getDeclaredFields(), tester)
						|| hasAnnotation(pClass, cl -> cl.getDeclaredMethods(), tester)) {
					final Class<?> enhancedClass = enhanceClass(pClass);
					enhancedClasses.put(pClass.getName(), enhancedClass);
					originalClasses.put(enhancedClass.getName(), pClass);
				}
			}

			for (final Field f : pClass.getDeclaredFields()) {
				if (!f.getType().getName().startsWith("java.lang.")) {
					register(f.getType(), f, pVisitedClasses);
				}
			}
			register(pClass.getSuperclass(), pField, pVisitedClasses);
		}
	}

	public Class<?> swap(Class<?> pClass) {
		Class<?> result = null;
		if (equals(pClass.getClassLoader())) {
			result = originalClasses.get(pClass.getName());
		} else {
			result = enhancedClasses.get(pClass.getName());
		}

		if (result == null) {
			result = pClass;
		}
		return result;
	}

	public Class<?> getOriginalClass(final Class<?> pClass) {
		return originalClasses.get(pClass.getName());
	}

	public Class<?> getEnhancedClass(final Class<?> pClass) {
		return enhancedClasses.get(pClass.getName());
	}

	public boolean isRegistered(final String pName) {
		return enhancedClasses.containsKey(pName);
	}

	public void register(final Class<?> pClass) throws ClassNotFoundException {
		register(pClass, null, new HashSet<>());

		detectedInterfaces.removeAll(registeredInterfaces);		
		if (!detectedInterfaces.isEmpty()) {
			final StringBuilder builder = new StringBuilder(
					"Following interfaces are NOT mapped to an implementation class! Use CloneContext.registerImplementation to do so.\n");
			for (final String line : detectedInterfaces) {
				builder.append(String.format("\t%s\n", line));
			}
			throw new IllegalStateException(builder.toString());
		}
	}

	public void registerImplementation(Class<?> pInterface,
			Class<? extends Serializable> pImplementation) throws ClassNotFoundException {
		final Class<?> enhancedClass = enhanceClass(pImplementation);
		originalClasses.put(pImplementation.getName(), pImplementation);
		enhancedClasses.put(pImplementation.getName(), enhancedClass);
		registeredInterfaces.add(pInterface.getName());
	}
}
