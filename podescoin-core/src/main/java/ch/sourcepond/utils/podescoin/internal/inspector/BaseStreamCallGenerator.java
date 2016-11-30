package ch.sourcepond.utils.podescoin.internal.inspector;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import org.objectweb.asm.MethodVisitor;

abstract class BaseStreamCallGenerator implements DefaultStreamCallGenerator {
	private boolean insertDefaultStreamCall = true;

	protected abstract String getStreamInternalName();

	protected abstract String getMethodName();

	protected abstract String getMethodDesc();

	@Override
	public final void visitDefaultStreamCall(final MethodVisitor pVisitor) {
		if (insertDefaultStreamCall) {
			pVisitor.visitVarInsn(ALOAD, 1);
			pVisitor.visitMethodInsn(INVOKEVIRTUAL, getStreamInternalName(), getMethodName(), getMethodDesc(), false);
			insertDefaultStreamCall = false;
		}
	}
}
