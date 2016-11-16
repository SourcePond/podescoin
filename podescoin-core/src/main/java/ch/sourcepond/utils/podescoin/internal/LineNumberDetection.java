package ch.sourcepond.utils.podescoin.internal;

import static org.objectweb.asm.Opcodes.ASM5;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

final class LineNumberDetection extends MethodVisitor {
	private final InspectClassVisitor inspector;
	private boolean working = true;

	public LineNumberDetection(final InspectClassVisitor pInspector, final MethodVisitor mv) {
		super(ASM5, mv);
		inspector = pInspector;
	}

	@Override
	public void visitLineNumber(final int line, final Label start) {
		if (working) {
			inspector.setReadObjectStartLine(line);
			working = false;
		}
	}
}
