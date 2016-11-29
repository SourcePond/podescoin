package ch.sourcepond.utils.podescoin.internal;

import org.objectweb.asm.MethodVisitor;

final class NoopDefaultReadObjectGenerator implements DefaultReadObjectGenerator {

	@Override
	public void visitDefaultRead(MethodVisitor pVisitor) {
		// noop
	}

}
