package ch.sourcepond.utils.podescoin.internal.inspector;

import org.objectweb.asm.MethodVisitor;

final class NoopDefaultReadObjectGenerator implements DefaultReadObjectGenerator {

	@Override
	public void visitDefaultRead(MethodVisitor pVisitor) {
		// noop
	}

}
