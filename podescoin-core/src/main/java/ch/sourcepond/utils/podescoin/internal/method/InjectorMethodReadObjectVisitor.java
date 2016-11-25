/*Copyright (C) 2016 Roland Hauser, <sourcepond@gmail.com>
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package ch.sourcepond.utils.podescoin.internal.method;

import static ch.sourcepond.utils.podescoin.internal.Constants.CONSTRUCTOR_NAME;
import static ch.sourcepond.utils.podescoin.internal.Constants.INJECTOR_INTERNAL_NAME;
import static ch.sourcepond.utils.podescoin.internal.DefaultReadObjectGenerator.OBJECT_INPUT_STREAM_INTERNAL_NAME;
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

import java.io.IOException;
import java.io.Serializable;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import ch.sourcepond.utils.podescoin.Container;
import ch.sourcepond.utils.podescoin.internal.DefaultReadObjectGenerator;
import ch.sourcepond.utils.podescoin.internal.Inspector;
import ch.sourcepond.utils.podescoin.internal.ReadObjectVisitor;

final class InjectorMethodReadObjectVisitor extends ReadObjectVisitor {
	private static final String CONTAINER_INTERNAL_NAME = getInternalName(Container.class);
	private static final String GET_CONTAINER_METHOD_NAME = "getContainer";
	private static final String GET_CONTAINER_METHOD_DESC = getMethodDescriptor(getType(Container.class),
			getType(Serializable.class));
	private static final String GET_COMPONENT_BY_ID_NAME = "getComponentById";
	private static final String GET_COMPONENT_BY_ID_DESC = getMethodDescriptor(getType(Object.class),
			getType(String.class), getType(String.class), getType(int.class));
	private static final String GET_COMPONENT_BY_TYPE_NAME_NAME = "getComponentByTypeName";
	private static final String GET_COMPONENT_BY_TYPE_DESC = getMethodDescriptor(getType(Object.class),
			getType(String.class), getType(int.class));
	private static final String CLASS_NOT_FOUND_EXCEPTION_INTERNAL_NAME = getInternalName(ClassNotFoundException.class);
	private static final String EXCEPTION_INTERNAL_NAME = getInternalName(Exception.class);
	private static final String IO_EXCEPTION_INTERNAL_NAME = getInternalName(IOException.class);
	private static final String GET_MESSAGE_NAME = "getMessage";
	private static final String GET_MESSAGE_DESC = getMethodDescriptor(getType(String.class));
	private static final String CONSTRUCTOR_DESC = getMethodDescriptor(getType(void.class), getType(String.class),
			getType(Throwable.class));
	private static final int MIN_STACK_SIZE = 4;
	private final Inspector inspector;

	InjectorMethodReadObjectVisitor(final Inspector pInspector, final MethodVisitor pDelegate,
			final boolean pEnhanceMode, final DefaultReadObjectGenerator pDefaultReadGenerator) {
		super(pEnhanceMode, pDefaultReadGenerator, pDelegate);
		inspector = pInspector;
	}

	@Override
	public void visitEnhance() {
		visitCode();

		final Label l0 = new Label();
		final Label l1 = new Label();
		final Label l2 = new Label();
		visitTryCatchBlock(l0, l1, l2, CLASS_NOT_FOUND_EXCEPTION_INTERNAL_NAME);
		final Label l3 = new Label();
		visitTryCatchBlock(l0, l1, l3, IO_EXCEPTION_INTERNAL_NAME);
		final Label l4 = new Label();
		visitTryCatchBlock(l0, l1, l4, EXCEPTION_INTERNAL_NAME);

		// Inserts a call to defaultReadObject if a new readObject method is
		// being generated.
		visitDefaultRead();

		visitVarInsn(ALOAD, 0);
		visitMethodInsn(INVOKESTATIC, INJECTOR_INTERNAL_NAME, GET_CONTAINER_METHOD_NAME, GET_CONTAINER_METHOD_DESC,
				false);
		visitVarInsn(ASTORE, 2);
		visitLabel(l0);
		visitVarInsn(ALOAD, 0);

		int stackSize = MIN_STACK_SIZE;
		if (inspector.hasObjectInputStream()) {
			visitVarInsn(ALOAD, 1);
			stackSize++;
		}

		final String[][] components = inspector.getComponents();

		boolean increaseByOne = false;
		for (int i = 0; i < components.length; i++, stackSize++) {
			visitVarInsn(ALOAD, 2);
			if (components[i][0] != null) {
				visitLdcInsn(components[i][0]);
				visitLdcInsn(components[i][1]);
				pushByteConstant(mv, i);
				visitMethodInsn(INVOKEINTERFACE, CONTAINER_INTERNAL_NAME, GET_COMPONENT_BY_ID_NAME,
						GET_COMPONENT_BY_ID_DESC, true);

				if (!increaseByOne) {
					increaseByOne = true;
				}
			} else {
				visitLdcInsn(components[i][1]);
				pushByteConstant(mv, i);
				visitMethodInsn(INVOKEINTERFACE, CONTAINER_INTERNAL_NAME, GET_COMPONENT_BY_TYPE_NAME_NAME,
						GET_COMPONENT_BY_TYPE_DESC, true);
			}
			visitTypeInsn(CHECKCAST, components[i][1].replace('.', '/'));
		}

		visitMethodInsn(Opcodes.INVOKESPECIAL, inspector.getInternalClassName(), inspector.getInjectorMethodName(),
				inspector.getInjectorMethodDesc(), false);

		visitLabel(l1);
		final Label l5 = new Label();
		visitJumpInsn(GOTO, l5);
		visitLabel(l2);
		visitFrame(Opcodes.F_FULL, 3, new Object[] { inspector.getInternalClassName(),
				OBJECT_INPUT_STREAM_INTERNAL_NAME, CONTAINER_INTERNAL_NAME }, 1,
				new Object[] { EXCEPTION_INTERNAL_NAME });

		visitVarInsn(ASTORE, 3);
		visitVarInsn(ALOAD, 3);
		visitInsn(ATHROW);
		visitLabel(l3);
		visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { IO_EXCEPTION_INTERNAL_NAME });
		visitVarInsn(ASTORE, 3);
		visitVarInsn(ALOAD, 3);
		visitInsn(ATHROW);
		visitLabel(l4);
		visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { EXCEPTION_INTERNAL_NAME });
		visitVarInsn(ASTORE, 3);
		visitTypeInsn(NEW, IO_EXCEPTION_INTERNAL_NAME);
		visitInsn(DUP);
		visitVarInsn(ALOAD, 3);
		visitMethodInsn(INVOKEVIRTUAL, EXCEPTION_INTERNAL_NAME, GET_MESSAGE_NAME, GET_MESSAGE_DESC, false);
		visitVarInsn(ALOAD, 3);
		visitMethodInsn(INVOKESPECIAL, IO_EXCEPTION_INTERNAL_NAME, CONSTRUCTOR_NAME, CONSTRUCTOR_DESC, false);
		visitInsn(ATHROW);
		visitLabel(l5);
		visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		visitInsn(RETURN);
		visitMaxs(increaseByOne ? stackSize + 1 : stackSize, 4);
	}
}
