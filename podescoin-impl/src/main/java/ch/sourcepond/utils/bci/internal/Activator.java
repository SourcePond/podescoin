package ch.sourcepond.utils.bci.internal;

import static org.osgi.framework.hooks.weaving.WovenClass.TRANSFORMING;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.hooks.weaving.WeavingException;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;

import ch.sourcepond.utils.bci.Injector;

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
		if (!context.getBundle().equals(wovenClass.getBundleWiring().getBundle()) && TRANSFORMING == wovenClass.getState()) {
			try {
				ClassReader reader = new ClassReader(wovenClass.getBytes());
				ClassWriter writer = new ClassWriter(reader, 0);

				final InspectForInjectorMethodClassVisitor inspector = new InspectForInjectorMethodClassVisitor(writer);
				ClassVisitor visitor = new FieldInjectionClassVisitor(inspector);
				try {
					reader.accept(visitor, 0);
				} catch (final AmbiguousInjectorMethodsException e) {
					throw new WeavingException(e.getMessage(), e);
				}

				reader = new ClassReader(writer.toByteArray());
				writer = new ClassWriter(reader, 0);
				visitor = new MethodInjectionClassVisitor(writer, inspector);
				reader.accept(visitor, 0);

				wovenClass.setBytes(writer.toByteArray());
				wovenClass.getDynamicImports().add(Injector.class.getPackage().getName());
			} catch (final Throwable e) {
				e.printStackTrace();
			}
		}
	}
}