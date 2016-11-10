package ch.sourcepond.utils.podescoin.devel;

import java.io.PrintWriter;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.TraceClassVisitor;

public class PrintASMInstructions {
	
	@Test
	public void printInstructions() throws Exception {
		final ClassReader reader = new ClassReader(InjectorMethodClass.class.getName());
		final ClassVisitor visitor = new PrintClassVisitor(
				new TraceClassVisitor(null, new ASMifier(), new PrintWriter(System.out)));
		reader.accept(visitor, ClassReader.SKIP_DEBUG);
	}
}
