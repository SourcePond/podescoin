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

public final class Activator implements BundleActivator, WeavingHook {
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

	@Override
	public void weave(final WovenClass wovenClass) {
		if (!context.getBundle().equals(wovenClass.getBundleWiring().getBundle())
				&& TRANSFORMING == wovenClass.getState()) {
			try {
				wovenClass.setBytes(transform(wovenClass.getBytes()));
				wovenClass.getDynamicImports().add(Injector.class.getPackage().getName());
			} catch (final Throwable e) {
				throw new WeavingException(e.getMessage(), e);
			}
		}
	}

	public static byte[] transform(final byte[] pOriginalClassBytes) {
		ClassReader reader = new ClassReader(pOriginalClassBytes);

		// First step: determine injector methods; this needs a full visit of
		// the class in order to find all possibilities. If more than one
		// injector method has been detected, an
		// AmbiguousInjectorMethodsException will be caused to be thrown.
		final InspectForInjectorMethodClassVisitor inspector = new InspectForInjectorMethodClassVisitor();
		try {
			reader.accept(inspector, 0);
		} catch (final AmbiguousInjectorMethodsException e) {
			throw new WeavingException(e.getMessage(), e);
		}

		// Second step: create or enhance readObject which calls the injector
		// method
		ClassWriter writer = new ClassWriter(reader, 0);
		ClassVisitor visitor = new MethodInjectionClassVisitor(writer, inspector);
		reader.accept(visitor, 0);

		// Third step: create or enhance readObject which injects fields. This
		// is done at the end because fields should have been injected before an
		// injector method is called (LIFO order)
		reader = new ClassReader(writer.toByteArray());
		writer = new ClassWriter(reader, 0);
		visitor = new FieldInjectionClassVisitor(writer);
		reader.accept(visitor, 0);

		return writer.toByteArray();
	}
}
