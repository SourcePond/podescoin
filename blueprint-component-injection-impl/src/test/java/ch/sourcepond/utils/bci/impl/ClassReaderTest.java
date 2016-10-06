package ch.sourcepond.utils.bci.impl;

import java.io.PrintWriter;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.TraceClassVisitor;

public class ClassReaderTest {

	@Test
	public void testIt() throws Exception {
		final ClassReader reader = new ClassReader(
				getClass().getResourceAsStream("/" + TestClass.class.getName().replace('.', '/') + ".class"));
		final ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
		final ClassVisitor visitor = new YourClassVisitor(
				new TraceClassVisitor(null, new ASMifier(), new PrintWriter(System.out)));

		reader.accept(visitor, ClassReader.SKIP_DEBUG);
	}
}
