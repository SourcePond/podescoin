package ch.sourcepond.utils.podescoin.internal;

import static ch.sourcepond.utils.podescoin.internal.Constants.INJECT_ANNOTATION_NAME;
import static ch.sourcepond.utils.podescoin.internal.Constants.NAMED_ANNOTATION_NAME;
import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Type.getType;
import static org.slf4j.LoggerFactory.getLogger;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.slf4j.Logger;

final class ComponentFieldVisitor extends FieldVisitor {
	private static final Logger LOG = getLogger(ComponentFieldVisitor.class);
	private final FieldInjectionClassVisitor classVisitor;
	private final String fieldName;
	private final String fieldType;
	private String componentIdOrNull;
	private boolean inject;

	ComponentFieldVisitor(final FieldInjectionClassVisitor pClassVisitor, final FieldVisitor pDelegate,
			final String pFieldName, final String pFieldType) {
		super(ASM5, pDelegate);
		classVisitor = pClassVisitor;
		fieldName = pFieldName;
		fieldType = pFieldType;
	}

	void setComponentId(final String pComponentId) {
		componentIdOrNull = pComponentId;
	}

	@Override
	public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
		if (visible) {
			if (INJECT_ANNOTATION_NAME.equals(getType(desc).getClassName())) {
				inject = true;
			}
			if (NAMED_ANNOTATION_NAME.equals(getType(desc).getClassName())) {
				return new NamedAnnotationOnFieldVisitor(this, fv.visitAnnotation(desc, visible));
			}
		}
		return super.visitAnnotation(desc, visible);
	}

	@Override
	public void visitEnd() {
		if (inject) {
			LOG.debug("{} : registering injection field {} with id {} and type {}", classVisitor.getClassName(), fieldName, componentIdOrNull, fieldType);
			classVisitor.addNamedComponent(fieldName, componentIdOrNull, fieldType);
		}
	}
}