package ch.sourcepond.utils.bci;

import static ch.sourcepond.utils.bci.Constants.INJECT_ANNOTATION_NAME;
import static java.lang.String.format;
import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Type.getArgumentTypes;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

final class InspectForInjectorMethodClassVisitor extends ClassVisitor {
	private static final String[][] EMPTY = new String[0][0];
	private String[][] namedComponents;
	private String injectorMethodName;
	private String injectorMethodDesc;

	InspectForInjectorMethodClassVisitor(final ClassVisitor pWriter) {
		super(ASM5, pWriter);
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
			final String[] exceptions) {
		return new InjectorMethodVisitor(this, super.visitMethod(access, name, desc, signature, exceptions), name,
				desc);
	}

	void addNamedComponent(final String pComponentId, final int pParameterIndex) {
		namedComponents[pParameterIndex][0] = pComponentId;
	}

	boolean isArgumentTypesInitialized() {
		return namedComponents != null;
	}

	String[][] getComponents() {
		return namedComponents;
	}

	void initArgumentTypes(final String pInjectorMethodName, final String pInjectorMethodDesc) {
		if (isArgumentTypesInitialized()) {
			throw new AmbiguousInjectorMethodsException(
					format("More than one method detected which is annotated with %s", INJECT_ANNOTATION_NAME));
		}

		injectorMethodName = pInjectorMethodName;
		injectorMethodDesc = pInjectorMethodDesc;

		final Type[] argumentTypes = getArgumentTypes(pInjectorMethodDesc);
		if (argumentTypes.length > 0) {
			namedComponents = new String[argumentTypes.length][2];
			for (int i = 0; i < argumentTypes.length; i++) {
				namedComponents[i][1] = argumentTypes[i].getClassName();
			}
		} else {
			namedComponents = EMPTY;
		}
	}

	public String getInjectorMethodName() {
		return injectorMethodName;
	}

	public String getInjectorMethodDesc() {
		return injectorMethodDesc;
	}
}
