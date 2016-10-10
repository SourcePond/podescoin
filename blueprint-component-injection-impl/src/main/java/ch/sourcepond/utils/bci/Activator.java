package ch.sourcepond.utils.bci;

import static org.osgi.framework.hooks.weaving.WovenClass.TRANSFORMING;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;

final class Activator implements BundleActivator, WeavingHook {

	@Override
	public void start(final BundleContext context) throws Exception {
		context.registerService(WeavingHook.class, this, null);
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		// noop; service un-registration is done automatically
	}

	@Override
	public void weave(final WovenClass wovenClass) {
		if (TRANSFORMING == wovenClass.getState()) {
			final ClassReader reader = new ClassReader(wovenClass.getBytes());
			final ClassWriter writer = new ClassWriter(reader, 0);
			final SerializableClassVisitor visitor = new SerializableClassVisitor(writer);
			reader.accept(visitor, 0);
			wovenClass.setBytes(writer.toByteArray());
		}
	}
}