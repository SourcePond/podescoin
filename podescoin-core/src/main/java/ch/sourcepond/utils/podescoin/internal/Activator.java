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

import static org.osgi.framework.hooks.weaving.WovenClass.TRANSFORMING;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.hooks.weaving.WeavingException;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;

import ch.sourcepond.utils.podescoin.Injector;
import ch.sourcepond.utils.podescoin.api.Recipient;
import ch.sourcepond.utils.podescoin.internal.field.ReadObjectFieldInjectionClassVisitor;
import ch.sourcepond.utils.podescoin.internal.inspector.Inspector;
import ch.sourcepond.utils.podescoin.internal.inspector.ReadObjectInspector;
import ch.sourcepond.utils.podescoin.internal.inspector.WriteObjectInspector;
import ch.sourcepond.utils.podescoin.internal.method.ReadObjectMethodClassVisitor;
import ch.sourcepond.utils.podescoin.internal.method.WriteObjectMethodClassVisitor;

public final class Activator implements BundleActivator, WeavingHook {
	private static final String RECIPIENT_CLASS_NAME = Recipient.class.getName();
	private BundleContext context;

	@Override
	public void start(final BundleContext context) throws Exception {
		this.context = context;
		context.registerService(WeavingHook.class, this, null);
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		// noop; service un-registration is done automatically
	}

	private boolean isAllowed(final WovenClass wovenClass) {
		return !RECIPIENT_CLASS_NAME.equals(wovenClass.getClassName())
				&& !context.getBundle().equals(wovenClass.getBundleWiring().getBundle());
	}

	@Override
	public void weave(final WovenClass wovenClass) {
		if (TRANSFORMING == wovenClass.getState() && isAllowed(wovenClass)) {
			try {
				wovenClass.setBytes(transform(wovenClass.getBytes()));
				wovenClass.getDynamicImports().add(Injector.class.getPackage().getName());
			} catch (final Throwable e) {
				throw new WeavingException(String.format("Enhancement of %s failed!", wovenClass.getClassName()), e);
			}
		}
	}

	public static byte[] transform(final byte[] pOriginalClassBytes) {
		ClassReader reader = new ClassReader(pOriginalClassBytes);

		// First step: determine readObject injector methods; this needs a full
		// visit of
		// the class in order to find all possibilities. If more than one
		// injector method for readObject has been detected, an
		// AmbiguousInjectorMethodsException will be caused to be thrown.
		Inspector inspector = new ReadObjectInspector();
		reader.accept(inspector, 0);
		byte[] classData = pOriginalClassBytes;

		if (inspector.isInjectionAware()) {
			// Second step: create or enhance readObject which calls the
			// injector
			// method
			ClassWriter writer = new ClassWriter(reader, 0);
			ClassVisitor visitor = new ReadObjectMethodClassVisitor(writer, inspector);
			reader.accept(visitor, 0);

			// Third step: create or enhance readObject which injects fields.
			// This
			// is done at the end because fields should have been injected
			// before an
			// injector method is called (LIFO order)
			reader = new ClassReader(writer.toByteArray());
			writer = new ClassWriter(reader, 0);
			visitor = new ReadObjectFieldInjectionClassVisitor(inspector, writer);
			reader.accept(visitor, 0);

			// Forth step: determine writeObject injector methods; this needs a
			// full visit of
			// the class in order to find all possibilities. If more than one
			// injector method for writeObject has been detected, an
			// AmbiguousInjectorMethodsException will be caused to be thrown.
			inspector = new WriteObjectInspector();
			reader = new ClassReader(writer.toByteArray());
			reader.accept(inspector, 0);
			writer = new ClassWriter(reader, 0);
			visitor = new WriteObjectMethodClassVisitor(writer, inspector);
			reader.accept(visitor, 0);

			classData = writer.toByteArray();
		}
		return classData;
	}
}
