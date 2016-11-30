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
import static ch.sourcepond.utils.podescoin.internal.inspector.DefaultStreamCallGenerator.OBJECT_OUTPUT_STREAM_INTERNAL_NAME;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import ch.sourcepond.utils.podescoin.internal.inspector.DefaultStreamCallGenerator;
import ch.sourcepond.utils.podescoin.internal.inspector.Inspector;

public class WriteObjectEnhancer extends InjectorMethodEnhancer {
	protected final Label l2 = new Label();
	protected final Label l3 = new Label();
	protected final Label l4 = new Label();

	WriteObjectEnhancer(final Inspector pInspector, final MethodVisitor pDelegate, final boolean pEnhanceMode,
			final DefaultStreamCallGenerator pDefaultStreamCallGenerator) {
		super(pInspector, pDelegate, pEnhanceMode, pDefaultStreamCallGenerator);
	}

	@Override
	protected void tryBlock() {
		visitTryCatchBlock(l0, l1, l2, IO_EXCEPTION_INTERNAL_NAME);
		visitTryCatchBlock(l0, l1, l3, EXCEPTION_INTERNAL_NAME);
	}

	@Override
	protected void catchBlock() {
		visitJumpInsn(GOTO, l4);
		visitLabel(l2);
		visitFrame(Opcodes.F_FULL, 3, new Object[] { inspector.getInternalClassName(),
				OBJECT_OUTPUT_STREAM_INTERNAL_NAME, CONTAINER_INTERNAL_NAME }, 1,
				new Object[] { IO_EXCEPTION_INTERNAL_NAME });
		visitVarInsn(ASTORE, 3);
		visitVarInsn(ALOAD, 3);
		visitInsn(ATHROW);
		visitLabel(l3);
		visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { EXCEPTION_INTERNAL_NAME });
		visitVarInsn(ASTORE, 3);
		visitTypeInsn(NEW, IO_EXCEPTION_INTERNAL_NAME);
		visitInsn(DUP);
		visitVarInsn(ALOAD, 3);
		visitMethodInsn(INVOKEVIRTUAL, EXCEPTION_INTERNAL_NAME, GET_MESSAGE_NAME, GET_MESSAGE_DESC, false);
		visitVarInsn(ALOAD, 3);
		visitMethodInsn(INVOKESPECIAL, IO_EXCEPTION_INTERNAL_NAME, CONSTRUCTOR_NAME, CONSTRUCTOR_DESC, false);
		visitInsn(ATHROW);
		visitLabel(l4);
	}
}
