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
package ch.sourcepond.utils.podescoin.internal;

import static ch.sourcepond.utils.podescoin.internal.Constants.CONSTRUCTOR_NAME;
import static ch.sourcepond.utils.podescoin.internal.Constants.INJECTOR_INTERNAL_NAME;
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
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.sourcepond.utils.podescoin.Container;

class MethodInjectionClassVisitor extends SerializableClassVisitor {
	private static final Logger LOG = LoggerFactory.getLogger(MethodInjectionClassVisitor.class);
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
	private static final String OBJECT_INPUT_STREAM_INTERNAL_NAME = getInternalName(ObjectInputStream.class);
	private static final String EXCEPTION_INTERNAL_NAME = getInternalName(Exception.class);
	private static final String IO_EXCEPTION_INTERNAL_NAME = getInternalName(IOException.class);
	private static final String GET_MESSAGE_NAME = "getMessage";
	private static final String GET_MESSAGE_DESC = getMethodDescriptor(getType(String.class));
	private static final String CONSTRUCTOR_DESC = getMethodDescriptor(getType(void.class), getType(String.class),
			getType(Throwable.class));
	private static final int MIN_STACK_SIZE = 4;
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
	protected void generateInjectionBody(final MethodVisitor mv) {
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

		int stackSize = MIN_STACK_SIZE;
		if (inspector.hasObjectInputStream()) {
			mv.visitVarInsn(ALOAD, 1);
			stackSize++;
		}

		final String[][] components = inspector.getComponents();

		boolean increaseByOne = false;
		for (int i = 0; i < components.length; i++, stackSize++) {
			mv.visitVarInsn(ALOAD, 2);
			if (components[i][0] != null) {
				mv.visitLdcInsn(components[i][0]);
				mv.visitLdcInsn(components[i][1]);
				pushByteConstant(mv, i);
				mv.visitMethodInsn(INVOKEINTERFACE, CONTAINER_INTERNAL_NAME, GET_COMPONENT_BY_ID_NAME,
						GET_COMPONENT_BY_ID_DESC, true);

				if (!increaseByOne) {
					increaseByOne = true;
				}
			} else {
				mv.visitLdcInsn(components[i][1]);
				pushByteConstant(mv, i);
				mv.visitMethodInsn(INVOKEINTERFACE, CONTAINER_INTERNAL_NAME, GET_COMPONENT_BY_TYPE_NAME_NAME,
						GET_COMPONENT_BY_TYPE_DESC, true);
			}
			mv.visitTypeInsn(CHECKCAST, components[i][1].replace('.', '/'));
		}

		mv.visitMethodInsn(INVOKEVIRTUAL, inspector.getInternalClassName(), inspector.getInjectorMethodName(),
				inspector.getInjectorMethodDesc(), false);

		mv.visitLabel(l1);
		final Label l3 = new Label();
		mv.visitJumpInsn(GOTO, l3);
		mv.visitLabel(l2);
		mv.visitFrame(Opcodes.F_FULL, 3, new Object[] { inspector.getInternalClassName(),
				OBJECT_INPUT_STREAM_INTERNAL_NAME, CONTAINER_INTERNAL_NAME }, 1,
				new Object[] { EXCEPTION_INTERNAL_NAME });
		mv.visitVarInsn(ASTORE, 3);
		mv.visitTypeInsn(NEW, IO_EXCEPTION_INTERNAL_NAME);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 3);
		mv.visitMethodInsn(INVOKEVIRTUAL, EXCEPTION_INTERNAL_NAME, GET_MESSAGE_NAME, GET_MESSAGE_DESC, false);
		mv.visitVarInsn(ALOAD, 3);
		mv.visitMethodInsn(INVOKESPECIAL, IO_EXCEPTION_INTERNAL_NAME, CONSTRUCTOR_NAME, CONSTRUCTOR_DESC, false);
		mv.visitInsn(ATHROW);
		mv.visitLabel(l3);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitInsn(RETURN);
		mv.visitMaxs(increaseByOne ? stackSize + 1 : stackSize, 4);
	}
}
