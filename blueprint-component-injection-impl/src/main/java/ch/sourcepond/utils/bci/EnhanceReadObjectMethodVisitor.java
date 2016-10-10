package ch.sourcepond.utils.bci;

import static ch.sourcepond.utils.bci.SerializableClassVisitor.INJECT_BLUEPRINT_COMPONENTS_METHOD_NAME;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getType;

import org.objectweb.asm.MethodVisitor;

final class EnhanceReadObjectMethodVisitor extends MethodVisitor {
	private static final String INJECT_BLUEPRINT_COMPONENTS_METHOD_DESC = getMethodDescriptor(getType(void.class));
	private final String thisClassInternalName;

	EnhanceReadObjectMethodVisitor(final String pThisClassInternalName, final MethodVisitor pDelegate) {
		super(ASM5, pDelegate);
		thisClassInternalName = pThisClassInternalName;
	}

	@Override
	public void visitCode() {
		super.visitCode();
		visitVarInsn(ALOAD, 0);
		visitMethodInsn(INVOKEVIRTUAL, thisClassInternalName, INJECT_BLUEPRINT_COMPONENTS_METHOD_NAME,
				INJECT_BLUEPRINT_COMPONENTS_METHOD_DESC, false);
	}
}
