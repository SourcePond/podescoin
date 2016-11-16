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

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getType;

import java.io.ObjectInputStream;

import org.objectweb.asm.MethodVisitor;

import ch.sourcepond.utils.podescoin.internal.field.FieldInjectionClassVisitor;

final class EnhanceReadObjectMethodVisitor extends MethodVisitor {
	static final String INJECT_BLUEPRINT_COMPONENTS_METHOD_DESC = getMethodDescriptor(getType(void.class),
			getType(ObjectInputStream.class));
	private final String thisClassInternalName;

	EnhanceReadObjectMethodVisitor(final String pThisClassInternalName, final MethodVisitor pDelegate) {
		super(ASM5, pDelegate);
		thisClassInternalName = pThisClassInternalName;
	}

	@Override
	public void visitCode() {
		super.visitCode();
		visitVarInsn(ALOAD, 0);
		visitVarInsn(ALOAD, 1);
		visitMethodInsn(INVOKEVIRTUAL, thisClassInternalName,
				FieldInjectionClassVisitor.INJECT_BLUEPRINT_COMPONENTS_METHOD_NAME,
				INJECT_BLUEPRINT_COMPONENTS_METHOD_DESC, false);
	}
}
