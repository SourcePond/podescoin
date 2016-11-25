package ch.sourcepond.utils.podescoin.internal;

import static org.objectweb.asm.Type.getInternalName;

import java.io.ObjectInputStream;

import org.objectweb.asm.MethodVisitor;

public interface DefaultReadObjectGenerator {
	String OBJECT_INPUT_STREAM_INTERNAL_NAME = getInternalName(ObjectInputStream.class);

	void visitDefaultRead(MethodVisitor pVisitor);
}
