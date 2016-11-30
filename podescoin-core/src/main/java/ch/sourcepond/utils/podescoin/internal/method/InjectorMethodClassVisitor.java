package ch.sourcepond.utils.podescoin.internal.method;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import ch.sourcepond.utils.podescoin.internal.Enhancer;
import ch.sourcepond.utils.podescoin.internal.SerializableClassVisitor;
import ch.sourcepond.utils.podescoin.internal.inspector.DefaultStreamCallGenerator;
import ch.sourcepond.utils.podescoin.internal.inspector.Inspector;

public abstract class InjectorMethodClassVisitor extends SerializableClassVisitor {

	protected InjectorMethodClassVisitor(Inspector pInspector, ClassVisitor pWriter) {
		super(pInspector, pWriter);
	}

	@Override
	protected final boolean isEnhancementNecessary() {
		final String[][] components = inspector.getComponents();
		return components != null && components.length > 0;
	}

}
