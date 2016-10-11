package ch.sourcepond.utils.bci;

import static ch.sourcepond.utils.bci.Constants.INJECT_ANNOTATION_NAME;
import static ch.sourcepond.utils.bci.Constants.NAMED_ANNOTATION_NAME;
import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Type.getArgumentTypes;
import static org.objectweb.asm.Type.getType;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;

class ComponentInjectorMethodVisitor extends MethodVisitor {
	private final InjectorMethodClassVisitor classVisitor;
	private final String desc;

	public ComponentInjectorMethodVisitor(final InjectorMethodClassVisitor pClassVisitor, final MethodVisitor mv,
			final String pDesc) {
		super(ASM5, mv);
		classVisitor = pClassVisitor;
		desc = pDesc;
	}

	@Override
	public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
		if (visible && INJECT_ANNOTATION_NAME.equals(getType(desc).getClassName())) {
			classVisitor.initArgumentTypes(getArgumentTypes(this.desc));
		}
		return super.visitAnnotation(desc, visible);
	}

	@Override
	public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
		if (classVisitor.isArgumentTypesInitialized()) {
			if (NAMED_ANNOTATION_NAME.equals(getType(desc).getClassName())) {
				return new NamedAnnotationOnParameterVisitor(this,
						super.visitParameterAnnotation(parameter, desc, visible), parameter);
			}
		}
		return super.visitParameterAnnotation(parameter, desc, visible);
	}

	void setComponentId(final String pComponentId, final int pParameterIndex) {
		classVisitor.addNamedComponent(pComponentId, pParameterIndex);
	}
}
