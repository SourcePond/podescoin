package ch.sourcepond.utils.bci;

import static ch.sourcepond.utils.bci.Constants.CONSTRUCTOR_NAME;
import static ch.sourcepond.utils.bci.Constants.INJECTOR_INTERNAL_NAME;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Type.getInternalName;
import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getType;

import java.io.ObjectInputStream;
import java.io.Serializable;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class MethodInjectionClassVisitor extends SerializableClassVisitor {
	private static final String CONTAINER_INTERNAL_NAME = getInternalName(Container.class);
	private static final String GET_CONTAINER_METHOD_NAME = "getContainer";
	private static final String GET_CONTAINER_METHOD_DESC = getMethodDescriptor(getType(Container.class),
			getType(Serializable.class));
	private static final String GET_COMPONENT_BY_ID_NAME = "getComponentById";
	private static final String GET_COMPONENT_BY_ID_DESC = getMethodDescriptor(getType(Object.class),
			getType(String.class), getType(String.class));
	private static final String GET_COMPONENT_BY_TYPE_NAME_NAME = "getComponentByTypeName";
	private static final String GET_COMPONENT_BY_TYPE_DESC = getMethodDescriptor(getType(Object.class),
			getType(String.class));
	private static final String OBJECT_INPUT_STREAM_INTERNAL_NAME = getInternalName(ObjectInputStream.class);
	private static final String EXCEPTION_INTERNAL_NAME = getInternalName(Exception.class);
	private static final String ILLEGAL_EXCEPTION_INTERNAL_NAME = getInternalName(IllegalStateException.class);
	private static final String GET_MESSAGE_NAME = "getMessage";
	private static final String GET_MESSAGE_DESC = getMethodDescriptor(getType(String.class));
	private static final String CONSTRUCTOR_DESC = getMethodDescriptor(getType(void.class), getType(String.class),
			getType(Throwable.class));
	private static final int MIN_STACK_SIZE = 3;
	private final InspectForInjectorMethodClassVisitor inspector;

	public MethodInjectionClassVisitor(final ClassVisitor pVisitor,
			final InspectForInjectorMethodClassVisitor pInspector) {
		super(pVisitor);
		inspector = pInspector;
	}

	@Override
	protected boolean isEnhancementNecessary() {
		final String[][] components = inspector.getComponents();
		return components != null && components.length > 0;
	}

	@Override
	protected void enhanceReadObject(final MethodVisitor mv) {
		mv.visitCode();
		final Label l0 = new Label();
		final Label l1 = new Label();
		final Label l2 = new Label();
		mv.visitTryCatchBlock(l0, l1, l2, EXCEPTION_INTERNAL_NAME);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESTATIC, INJECTOR_INTERNAL_NAME, GET_CONTAINER_METHOD_NAME, GET_CONTAINER_METHOD_DESC,
				false);
		mv.visitVarInsn(ASTORE, 2);
		mv.visitLabel(l0);
		mv.visitVarInsn(ALOAD, 0);

		final String[][] components = inspector.getComponents();

		boolean increaseStackSizeByOne = false;
		int stackSize = MIN_STACK_SIZE;
		for (int i = 0; i < components.length; i++, stackSize++) {
			mv.visitVarInsn(ALOAD, 2);
			if (components[i][0] != null) {
				mv.visitLdcInsn(components[i][0]);
				mv.visitLdcInsn(components[i][1]);
				mv.visitMethodInsn(INVOKEINTERFACE, CONTAINER_INTERNAL_NAME, GET_COMPONENT_BY_ID_NAME,
						GET_COMPONENT_BY_ID_DESC, true);

				if (!increaseStackSizeByOne) {
					increaseStackSizeByOne = true;
				}
			} else {
				mv.visitLdcInsn(components[i][1]);
				mv.visitMethodInsn(INVOKEINTERFACE, CONTAINER_INTERNAL_NAME, GET_COMPONENT_BY_TYPE_NAME_NAME,
						GET_COMPONENT_BY_TYPE_DESC, true);
			}
			mv.visitTypeInsn(CHECKCAST, components[i][1].replace('.', '/'));
		}

		mv.visitMethodInsn(INVOKEVIRTUAL, thisClassInternalName, inspector.getInjectorMethodName(),
				inspector.getInjectorMethodDesc(), false);

		mv.visitLabel(l1);
		final Label l3 = new Label();
		mv.visitJumpInsn(GOTO, l3);
		mv.visitLabel(l2);
		mv.visitFrame(Opcodes.F_FULL, 3,
				new Object[] { thisClassInternalName, OBJECT_INPUT_STREAM_INTERNAL_NAME, CONTAINER_INTERNAL_NAME }, 1,
				new Object[] { EXCEPTION_INTERNAL_NAME });
		mv.visitVarInsn(ASTORE, 3);
		mv.visitTypeInsn(NEW, ILLEGAL_EXCEPTION_INTERNAL_NAME);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 3);
		mv.visitMethodInsn(INVOKEVIRTUAL, EXCEPTION_INTERNAL_NAME, GET_MESSAGE_NAME, GET_MESSAGE_DESC, false);
		mv.visitVarInsn(ALOAD, 3);
		mv.visitMethodInsn(INVOKESPECIAL, ILLEGAL_EXCEPTION_INTERNAL_NAME, CONSTRUCTOR_NAME, CONSTRUCTOR_DESC, false);
		mv.visitInsn(ATHROW);
		mv.visitLabel(l3);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitInsn(RETURN);
		mv.visitMaxs(increaseStackSizeByOne ? stackSize + 1 : stackSize, 4);
		mv.visitEnd();
	}
}
