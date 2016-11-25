package ch.sourcepond.utils.podescoin.internal;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getType;

import org.objectweb.asm.MethodVisitor;

final class DefaultReadObjectVisitor implements DefaultReadObjectGenerator {
	private static final String DEFAULT_READ_OBJECT = "defaultReadObject";
	private static final String DEFAULT_READ_OBJECT_DESC = getMethodDescriptor(getType(void.class));
	private boolean insertDefaultReadObject = true;

	public void visitDefaultRead(final MethodVisitor pVisitor) {
		if (insertDefaultReadObject) {
			pVisitor.visitVarInsn(ALOAD, 1);
			pVisitor.visitMethodInsn(INVOKEVIRTUAL, OBJECT_INPUT_STREAM_INTERNAL_NAME, DEFAULT_READ_OBJECT,
					DEFAULT_READ_OBJECT_DESC, false);
			insertDefaultReadObject = false;
		}
	}
}
