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
package ch.sourcepond.utils.podescoin.internal;

import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Function;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import ch.sourcepond.utils.podescoin.api.Component;
import ch.sourcepond.utils.podescoin.api.ReadObject;
import ch.sourcepond.utils.podescoin.api.WriteObject;
import ch.sourcepond.utils.podescoin.internal.field.ReadObjectFieldInjectionClassVisitor;
import ch.sourcepond.utils.podescoin.internal.field.WriteObjectFieldInjectionClassVisitor;
import ch.sourcepond.utils.podescoin.internal.inspector.ReadObjectInspector;
import ch.sourcepond.utils.podescoin.internal.inspector.WriteObjectInspector;
import ch.sourcepond.utils.podescoin.internal.method.ReadObjectMethodClassVisitor;
import ch.sourcepond.utils.podescoin.internal.method.WriteObjectMethodClassVisitor;

public final class Transformer {
	private static final String[] IGNORED_PACKAGE_PREFIXES = new String[] { "java.", "javax.", "sun." };

	public static byte[] transform(final byte[] pOriginalClassBytes) {
		ClassReader reader = new ClassReader(pOriginalClassBytes);

		// First step: determine readObject injector methods; this needs a full
		// visit of
		// the class in order to find all possibilities. If more than one
		// injector method for readObject has been detected, an
		// AmbiguousInjectorMethodsException will be caused to be thrown.
		final ReadObjectInspector readObjectInspector = new ReadObjectInspector();
		final WriteObjectInspector writeObjectInspector = new WriteObjectInspector();
		reader.accept(readObjectInspector, 0);
		reader.accept(writeObjectInspector, 0);
		byte[] classData = pOriginalClassBytes;

		if (readObjectInspector.isInjectionAware() || writeObjectInspector.isInjectionAware()) {
			// Second step: create or enhance readObject which calls the
			// injector
			// method
			ClassWriter writer = new ClassWriter(reader, 0);
			ClassVisitor visitor = new ReadObjectMethodClassVisitor(writer, readObjectInspector);
			reader.accept(visitor, 0);

			// Third step: create or enhance readObject which injects fields.
			// This
			// is done at the end because fields should have been injected
			// before an
			// injector method is called (LIFO order)
			reader = new ClassReader(writer.toByteArray());
			writer = new ClassWriter(reader, 0);
			visitor = new ReadObjectFieldInjectionClassVisitor(readObjectInspector, writer);
			reader.accept(visitor, 0);

			// Forth step: determine writeObject injector methods; this needs a
			// full visit of
			// the class in order to find all possibilities. If more than one
			// injector method for writeObject has been detected, an
			// AmbiguousInjectorMethodsException will be caused to be thrown.
			reader = new ClassReader(writer.toByteArray());
			writer = new ClassWriter(reader, 0);
			visitor = new WriteObjectMethodClassVisitor(writer, writeObjectInspector);
			reader.accept(visitor, 0);

			// Fifth step: create or enhance writeObject which injects fields.
			// This
			// is done at the end because fields should have been injected
			// before an
			// injector method is called (LIFO order)
			reader = new ClassReader(writer.toByteArray());
			writer = new ClassWriter(reader, 0);
			visitor = new WriteObjectFieldInjectionClassVisitor(writeObjectInspector, writer);
			reader.accept(visitor, 0);

			// Transformation is done
			classData = writer.toByteArray();
		}
		return classData;
	}

	public static boolean isNonJDKClass(final Class<?> pType) {
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

	private static void collectAccessibleObjects(final Class<?> pClass,
			final Collection<AccessibleObject> pAccessibleObjects,
			final Function<Class<?>, AccessibleObject[]> pGetAccessibleObjects) {
		if (pClass != null) {
			for (final AccessibleObject o : pGetAccessibleObjects.apply(pClass)) {
				pAccessibleObjects.add(o);
			}

			collectAccessibleObjects(pClass.getSuperclass(), pAccessibleObjects, pGetAccessibleObjects);
		}
	}

	public static boolean shouldBeEnhanced(final Class<?> pClass) {
		if (Serializable.class.isAssignableFrom(pClass) && isNonJDKClass(pClass)) {
			final Collection<AccessibleObject> accessibleObjects = new LinkedList<>();
			collectAccessibleObjects(pClass, accessibleObjects, c -> c.getDeclaredFields());
			collectAccessibleObjects(pClass, accessibleObjects, c -> c.getDeclaredMethods());

			for (final AccessibleObject obj : accessibleObjects) {
				obj.setAccessible(true);
				if (obj.isAnnotationPresent(Component.class) || obj.isAnnotationPresent(ReadObject.class)
						|| obj.isAnnotationPresent(WriteObject.class)) {
					return true;
				}
			}
		}
		return false;
	}
}
