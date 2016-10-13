package ch.sourcepond.utils.bci;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.osgi.framework.Bundle;

public class MethodInjectorTestClassLoader extends TestClassLoader {

	public MethodInjectorTestClassLoader(final ClassVisitor pVisitor, final ClassWriter pWriter,
			final Class<?> pTestSerializableClass, final Bundle pBundle) {
		super(new InspectForInjectorMethodClassVisitor(pVisitor), pWriter, pTestSerializableClass, pBundle);
	}

	@Override
	protected byte[] secondPass(final byte[] pClassData) {
		final ClassReader reader = new ClassReader(writer.toByteArray());
		writer = new ClassWriter(reader, 0);
		visitor = new MethodInjectionClassVisitor(writer, (InspectForInjectorMethodClassVisitor) visitor);
		reader.accept(visitor, 0);
		return writer.toByteArray();
	}

}
