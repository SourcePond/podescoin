package ch.sourcepond.utils.bci;

import static ch.sourcepond.utils.bci.Constants.INJECT_ANNOTATION_NAME;
import static java.lang.String.format;
import static org.objectweb.asm.Opcodes.ASM5;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class InjectorMethodClassVisitor extends ClassVisitor {
	private String[][] namedComponents;

	public InjectorMethodClassVisitor(final ClassVisitor cv) {
		super(ASM5, cv);
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
			final String[] exceptions) {
		return new ComponentInjectorMethodVisitor(this, super.visitMethod(access, name, desc, signature, exceptions),
				desc);
	}

	public void addNamedComponent(final String pComponentId, final int pParameterIndex) {
		namedComponents[pParameterIndex][0] = pComponentId;
	}

	boolean isArgumentTypesInitialized() {
		return namedComponents != null;
	}

	void initArgumentTypes(final Type[] pArgumentTypes) {
		if (isArgumentTypesInitialized()) {
			throw new AmbiguousInjectorMethodsException(
					format("More than one method detected which is annotated with %s", INJECT_ANNOTATION_NAME));
		}

		namedComponents = new String[pArgumentTypes.length][2];
		for (int i = 0; i < pArgumentTypes.length; i++) {
			namedComponents[i][1] = pArgumentTypes[i].getClassName();
		}
	}

	@Override
	public void visitEnd() {
		super.visitEnd();
	}
}
