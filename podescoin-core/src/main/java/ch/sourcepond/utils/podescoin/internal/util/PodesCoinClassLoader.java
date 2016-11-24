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
package ch.sourcepond.utils.podescoin.internal.util;

import static java.lang.reflect.Modifier.isPublic;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.inject.Inject;

import ch.sourcepond.utils.podescoin.internal.Activator;

public class PodesCoinClassLoader extends ClassLoader {
	private static final String[] IGNORED_PACKAGE_PREFIXES = new String[] { "java.", "javax.", "sun." };
	private final Map<String, Class<?>> enhancedClasses = new HashMap<>();

	public PodesCoinClassLoader() {
		super();
	}

	public PodesCoinClassLoader(final ClassLoader pParent) {
		super(pParent);
	}

	private static boolean isNonJDKClass(final Class<?> pType) {
		boolean allowed = pType != null;
		if (allowed) {
			for (int i = 0; i < IGNORED_PACKAGE_PREFIXES.length; i++) {
				if (pType.getName().startsWith(IGNORED_PACKAGE_PREFIXES[i])) {
					allowed = false;
					break;
				}
			}
		}
		return allowed;
	}

	protected ClassLoader getParentLoader() {
		ClassLoader parent = super.getParent();
		if (parent == null) {
			parent = ClassLoader.getSystemClassLoader();
		}
		return parent;
	}

	@Override
	public Class<?> loadClass(final String name) throws ClassNotFoundException {
		synchronized (enhancedClasses) {
			Class<?> enhancedClass = enhancedClasses.get(name);
			if (enhancedClass == null) {
				final Class<?> originalClass = getParentLoader().loadClass(name);
				if (Serializable.class.isAssignableFrom(originalClass) && isInjectionCapable(originalClass)) {
					enhanceClassHierarchy(originalClass);
					enhancedClass = enhancedClasses.get(name);
				} else {
					enhancedClass = originalClass;
					enhancedClasses.put(name, originalClass);
				}
			}
			return enhancedClass;
		}
	}

	private static <T extends AccessibleObject> boolean hasAnnotation(final Class<?> pClass,
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

	private static boolean hasAnnotation(final Class<?> pClass) {
		final Predicate<AnnotatedElement> tester = e -> e.isAnnotationPresent(Inject.class);
		return hasAnnotation(pClass, cl -> cl.getDeclaredFields(), tester)
				|| hasAnnotation(pClass, cl -> cl.getDeclaredMethods(), tester);
	}

	private boolean isInjectionCapable(final Class<?> pOriginalClass) {
		final Predicate<AnnotatedElement> predicate = f -> f.isAnnotationPresent(Inject.class);
		return hasAnnotation(pOriginalClass, cl -> cl.getDeclaredFields(), predicate)
				|| hasAnnotation(pOriginalClass, cl -> cl.getDeclaredMethods(), predicate);
	}

	private void enhanceClassHierarchy(final Class<?> pOriginalClass) throws ClassNotFoundException {
		if (pOriginalClass != null && isNonJDKClass(pOriginalClass)
				&& !enhancedClasses.containsKey(pOriginalClass.getName())) {
			if (pOriginalClass.isArray()) {
				enhanceClassHierarchy(pOriginalClass.getComponentType());
			} else {
				for (final Class<?> ifs : pOriginalClass.getInterfaces()) {
					enhanceClassHierarchy(ifs);
				}

				if (!pOriginalClass.isInterface() && hasAnnotation(pOriginalClass)) {
					enhancedClasses.put(pOriginalClass.getName(), enhanceClass(pOriginalClass));
				} else if (!isPublic(pOriginalClass.getModifiers())) {
					final byte[] classData = toByteArray(pOriginalClass);
					enhancedClasses.put(pOriginalClass.getName(), defineClass(pOriginalClass.getName(), classData, 0,
							classData.length, pOriginalClass.getProtectionDomain()));
				}
			}

			enhanceClassHierarchy(pOriginalClass.getSuperclass());
		}
	}

	private static byte[] toByteArray(final Class<?> pOriginalClass) throws ClassNotFoundException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (final InputStream in = new BufferedInputStream(
				pOriginalClass.getResourceAsStream("/" + pOriginalClass.getName().replace('.', '/') + ".class"))) {
			final byte[] buffer = new byte[1024];
			int read = in.read(buffer);
			while (read != -1) {
				out.write(buffer, 0, read);
				read = in.read(buffer);
			}
		} catch (final IOException e) {
			throw new ClassNotFoundException(e.getMessage(), e);
		}
		return out.toByteArray();
	}

	private Class<?> enhanceClass(final Class<?> pOriginalClass) throws ClassNotFoundException {
		final byte[] enhancedClassData = Activator.transform(toByteArray(pOriginalClass));
		return defineClass(pOriginalClass.getName(), enhancedClassData, 0, enhancedClassData.length,
				pOriginalClass.getProtectionDomain());
	}
}
