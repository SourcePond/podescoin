package ch.sourcepond.utils.bci;

import static org.objectweb.asm.ClassReader.SKIP_DEBUG;

import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;

public class TestClassLoader extends ClassLoader implements BundleReference {
	private final Class<?> testSerializableClass;
	private final Bundle bundle;
	protected ClassVisitor visitor;
	protected ClassWriter writer;
	private Class<?> cl;

	public TestClassLoader(final ClassVisitor pVisitor, final ClassWriter pWriter,
			final Class<?> pTestSerializableClass, final Bundle pBundle) {
		super(null);
		visitor = pVisitor;
		writer = pWriter;
		testSerializableClass = pTestSerializableClass;
		bundle = pBundle;
	}

	protected byte[] secondPass(final byte[] pClassData) {
		// noop by default
		return pClassData;
	}

	@Override
	protected Class<?> findClass(final String name) throws ClassNotFoundException {
		if (name.equals(testSerializableClass.getName())) {
			if (cl == null) {
				try (final InputStream in = getClass().getResourceAsStream("/" + name.replace('.', '/') + ".class")) {
					final ClassReader reader = new ClassReader(in);
					reader.accept(visitor, SKIP_DEBUG);
					byte[] classData = writer.toByteArray();
					classData = secondPass(classData);
					cl = defineClass(name, classData, 0, classData.length);
				} catch (final IOException e) {
					throw new ClassNotFoundException(e.getMessage(), e);
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