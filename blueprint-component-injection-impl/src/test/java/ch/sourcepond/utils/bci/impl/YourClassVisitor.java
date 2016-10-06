package ch.sourcepond.utils.bci.impl;

import java.io.PrintWriter;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public class YourClassVisitor extends ClassVisitor {
	public YourClassVisitor(final ClassVisitor cv) {
		super(Opcodes.ASM5, cv);
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
			final String[] exceptions) {
		final MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
		final Printer p = new Textifier(Opcodes.ASM5) {
			@Override
			public void visitMethodEnd() {
				print(new PrintWriter(System.out)); // print it after it has
													// been visited
			}
		};
		return new TraceMethodVisitor(mv, p);
	}
}