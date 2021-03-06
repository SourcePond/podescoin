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

import static ch.sourcepond.utils.podescoin.internal.Transformer.isNonJDKClass;
import static ch.sourcepond.utils.podescoin.internal.Transformer.hasEnhancerAnnotation;
import static ch.sourcepond.utils.podescoin.internal.Transformer.transform;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

public class PodesCoinClassLoader extends ClassLoader {
	private final Map<String, Class<?>> enhancedClasses = new HashMap<>();

	public PodesCoinClassLoader() {
		super(ClassLoader.getSystemClassLoader());
	}

	public PodesCoinClassLoader(final ClassLoader pParent) {
		super(pParent);
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
				if (hasEnhancerAnnotation(originalClass)) {
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

	private void enhanceClassHierarchy(final Class<?> pOriginalClass) throws ClassNotFoundException {
		if (pOriginalClass != null && isNonJDKClass(pOriginalClass)
				&& !enhancedClasses.containsKey(pOriginalClass.getName())) {
			if (pOriginalClass.isArray()) {
				enhanceClassHierarchy(pOriginalClass.getComponentType());
			} else {
				for (final Class<?> ifs : pOriginalClass.getInterfaces()) {
					enhanceClassHierarchy(ifs);
				}

				if (!pOriginalClass.isInterface()) {
					enhanceClass(pOriginalClass);
				} else {
					final byte[] classData = toByteArray(pOriginalClass);
					defineClass(pOriginalClass, classData);
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

	private void enhanceClass(final Class<?> pOriginalClass) throws ClassNotFoundException {
		final byte[] enhancedClassData = transform(toByteArray(pOriginalClass));
		try {
			defineClass(pOriginalClass, enhancedClassData);
		} catch (final IllegalAccessError e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void defineClass(final Class<?> pOriginalClass, final byte[] classData) {
		Class<?> enhancedClass = enhancedClasses.get(pOriginalClass.getName());
		if (enhancedClass == null) {
			final ProtectionDomain originalProtectionDomain = pOriginalClass.getProtectionDomain();
			enhancedClass = defineClass(pOriginalClass.getName(), classData, 0, classData.length,
					new ProtectionDomain(originalProtectionDomain.getCodeSource(),
							originalProtectionDomain.getPermissions(), this, originalProtectionDomain.getPrincipals()));
			enhancedClasses.put(pOriginalClass.getName(), enhancedClass);
		}
	}
}
