package ch.sourcepond.utils.podescoin.internal;

import static org.objectweb.asm.Opcodes.ASM5;

import org.objectweb.asm.AnnotationVisitor;

final class NamedAnnotationOnFieldVisitor extends AnnotationVisitor {
	private final ComponentFieldVisitor fieldVisitor;

	NamedAnnotationOnFieldVisitor(final ComponentFieldVisitor pFieldVisitor, final AnnotationVisitor pDelegate) {
		super(ASM5, pDelegate);
		fieldVisitor = pFieldVisitor;
	}

	@Override
	public void visit(final String name, final Object value) {
		fieldVisitor.setComponentId((String) value);
		super.visit(name, value);
	}
}
