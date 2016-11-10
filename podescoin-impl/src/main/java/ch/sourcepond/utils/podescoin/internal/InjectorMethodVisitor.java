package ch.sourcepond.utils.podescoin.internal;

import static ch.sourcepond.utils.podescoin.internal.Constants.INJECT_ANNOTATION_NAME;
import static ch.sourcepond.utils.podescoin.internal.Constants.NAMED_ANNOTATION_NAME;
import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Type.getArgumentTypes;
import static org.objectweb.asm.Type.getType;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.ObjectInputStream;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.slf4j.Logger;

final class InjectorMethodVisitor extends MethodVisitor {
	private static final Logger LOG = getLogger(InjectorMethodVisitor.class);
	private final InspectForInjectorMethodClassVisitor classVisitor;
	private final String injectorMethodName;
	private final String injectorMethodDesc;

	public InjectorMethodVisitor(final InspectForInjectorMethodClassVisitor pClassVisitor, final MethodVisitor mv,
			final String pInjectorMethodName, final String pInjectorMethodDesc) {
		super(ASM5, mv);
		classVisitor = pClassVisitor;
		injectorMethodName = pInjectorMethodName;
		injectorMethodDesc = pInjectorMethodDesc;
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
		if (visible && INJECT_ANNOTATION_NAME.equals(getType(desc).getClassName())) {
			LOG.debug("{} : {} : added with descriptor {}", classVisitor.getClassName(), injectorMethodName, injectorMethodDesc);
			classVisitor.initArgumentTypes(includeObjectInputStream(), injectorMethodName, injectorMethodDesc);
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

	private boolean includeObjectInputStream() {
		boolean includeObjectInputStream = false;
		final Type[] argumentTypes = getArgumentTypes(injectorMethodDesc);
		if (argumentTypes.length > 0) {
			includeObjectInputStream = ObjectInputStream.class.getName().equals(argumentTypes[0].getClassName());
		}
		return includeObjectInputStream;
	}
	
	void setComponentId(final String pComponentId, final int pParameterIndex) {
		LOG.debug("{} : {} : use component-id {} for parameter index {}", classVisitor.getClassName(),
				injectorMethodName, pComponentId, pParameterIndex);
		classVisitor.addNamedComponent(pComponentId, pParameterIndex);
	}
}
