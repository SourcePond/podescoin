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

import static org.objectweb.asm.ClassReader.SKIP_DEBUG;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;

import ch.sourcepond.utils.podescoin.internal.FieldInjectionClassVisitorTest;

public class TestClassLoader extends ClassLoader implements BundleReference {
	private final Class<?> testSerializableClass;
	private final Bundle bundle;
	protected ClassVisitor visitor;
	protected ClassWriter writer;
	private Map<String, Class<?>> enhanced = new HashMap<>();

	public TestClassLoader(final ClassVisitor pVisitor, final ClassWriter pWriter,
			final Class<?> pTestSerializableClass, final Bundle pBundle) {
		super(null);
		visitor = pVisitor;
		writer = pWriter;
		bundle = pBundle;
		testSerializableClass = pTestSerializableClass;
		enhanced.put(pTestSerializableClass.getName(), null);
	}

	protected byte[] secondPass(final byte[] pClassData) {
		// noop by default
		return pClassData;
	}

	public boolean isEnhanced(final String pClassName) {
		return enhanced.containsKey(pClassName);
	}

	private Class<?> enhance(final Class<?> pOriginal) throws ClassNotFoundException {
		Class<?> enhanced = null;
		if (pOriginal != null) {
			try (final InputStream in = getClass()
					.getResourceAsStream("/" + pOriginal.getName().replace('.', '/') + ".class")) {
				final ClassReader reader = new ClassReader(in);
				reader.accept(visitor, SKIP_DEBUG);
				byte[] classData = writer.toByteArray();
				classData = secondPass(classData);
				enhanced = defineClass(pOriginal.getName(), classData, 0, classData.length);
			} catch (final IOException e) {
				throw new ClassNotFoundException(e.getMessage(), e);
			}
		}
		return enhanced;
	}

	@Override
	protected Class<?> findClass(final String name) throws ClassNotFoundException {
		if (enhanced.containsKey(name)) {
			Class<?> cl = enhanced.get(name);
			if (cl == null) {
				Class<?> current = testSerializableClass;
				while (current != null) {
					Class<?> enhancedClass = enhance(current);

					if (cl == null) {
						cl = enhancedClass;
					}

					enhanced.put(enhancedClass.getName(), enhancedClass);
					current = Object.class.equals(current.getSuperclass()) ? null : current.getSuperclass();
				}
			}
			return cl;
		}
		return FieldInjectionClassVisitorTest.class.getClassLoader().loadClass(name);
	}

	@Override
	public Bundle getBundle() {
		return bundle;
	}
}