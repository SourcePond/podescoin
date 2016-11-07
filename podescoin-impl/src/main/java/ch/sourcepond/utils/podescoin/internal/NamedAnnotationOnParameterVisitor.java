package ch.sourcepond.utils.podescoin.internal;

import static org.objectweb.asm.Opcodes.ASM5;

import org.objectweb.asm.AnnotationVisitor;

public class NamedAnnotationOnParameterVisitor extends AnnotationVisitor {
	private final InjectorMethodVisitor methodVisitor;
	private final int parameterIndex;

	NamedAnnotationOnParameterVisitor(final InjectorMethodVisitor pMethodVisitor,
			final AnnotationVisitor pDelegate, final int pParameterIndex) {
		super(ASM5, pDelegate);
		methodVisitor = pMethodVisitor;
		parameterIndex = pParameterIndex;
	}

	@Override
	public void visit(final String name, final Object value) {
		methodVisitor.setComponentId((String) value, parameterIndex);
		super.visit(name, value);
	}
}
