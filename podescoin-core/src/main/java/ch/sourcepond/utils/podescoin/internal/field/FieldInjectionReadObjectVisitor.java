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
package ch.sourcepond.utils.podescoin.internal.field;

import static ch.sourcepond.utils.podescoin.internal.Constants.INJECTOR_INTERNAL_NAME;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ICONST_2;
import static org.objectweb.asm.Opcodes.ICONST_3;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Type.getInternalName;
import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getType;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;

import org.objectweb.asm.MethodVisitor;

import ch.sourcepond.utils.podescoin.internal.InspectClassVisitor;
import ch.sourcepond.utils.podescoin.internal.ReadObjectVisitor;

final class FieldInjectionReadObjectVisitor extends ReadObjectVisitor {
	static final String INJECT_BLUEPRINT_COMPONENTS_METHOD_DESC = getMethodDescriptor(getType(void.class),
			getType(ObjectInputStream.class));
	private static final String INJECTOR_METHOD_NAME = "injectComponents";
	private static final String INJECTOR_METHOD_DESC = getMethodDescriptor(getType(void.class),
			getType(Serializable.class), getType(String[][].class));
	private static final String FIRST_DIMENSION_INTERNAL_NAME = getInternalName(String[].class);
	private static final String SECOND_DIMENSION_INTERNAL_NAME = getInternalName(String.class);
	private List<String[]> namedComponents;

	FieldInjectionReadObjectVisitor(final InspectClassVisitor pInspector, final MethodVisitor pDelegate) {
		super(pInspector, pDelegate);
	}

	void setNamedComponents(final List<String[]> pNamedComponents) {
		namedComponents = pNamedComponents;
	}

	@Override
	public void visitEnhance() {
		final String[][] namedComponentArr = new String[namedComponents.size()][2];
		namedComponents.toArray(namedComponentArr);

		visitCode();

		// Load 'this' reference on operand stack (first operand for calling
		// static method ch.sourcepond.utils.podescoin.Injector#injectComponent)
		visitVarInsn(ALOAD, 0);

		// Push length of two-dimensional array on operand stack (first
		// operand for ANEWARRAY)
		visitIntInsn(BIPUSH, namedComponentArr.length);

		// Push internal name of String[].class on operand stack. Note: the
		// internal name is '[Ljava/lang/String;' and NOT
		// '[[Ljava/lang/String;' because we will only create the first
		// dimension of the two-dimension string array. The second dimension
		// will contain the field-name and the component-id.
		visitTypeInsn(ANEWARRAY, FIRST_DIMENSION_INTERNAL_NAME);

		// Duplicate the array reference on top of the operand stack. This
		// is necessary because we start now to add the second dimension
		// arrays. The original reference will later be used as second
		// operand for calling static method
		// ch.sourcepond.utils.podescoin.Injector#injectComponent.
		visitInsn(DUP);

		for (int idx = 0; idx < namedComponentArr.length; idx++) {

			// Push the current index on the operand stack (first
			// dimension). This will be the operand for adding the sub-array
			// to the two-dimensional array at the end of the loop.
			pushByteConstant(mv, idx);

			// Push the the constant value '3' on the operand stack. This is
			// used as operand for ANEWARRAY to create an array of size 3.
			visitInsn(ICONST_3);

			// Create sub-array of size '2'
			visitTypeInsn(ANEWARRAY, SECOND_DIMENSION_INTERNAL_NAME);

			// Push a copy of the sub-array reference on the stack; this
			// will be used as array-ref
			// (first operand for AASTORE) for
			// the insertion of the field-name.
			visitInsn(DUP);

			// Push constant value '0' on the operand stack. This is the
			// index in the sub-array where to insert the field-name (second
			// operand of AASTORE)
			visitInsn(ICONST_0);

			// Push the field-name on the operand stack. This is the value
			// written into the sub-array at index 0 (third operand of
			// AASTORE)
			visitLdcInsn(namedComponentArr[idx][0]);

			// Store the field-name into the sub-array
			visitInsn(AASTORE);

			if (namedComponentArr[idx][1] != null) {
				// Push a copy of the sub-array reference on the stack; this
				// will be used as array-ref
				// (first operand for AASTORE) for
				// the insertion of the component-id.
				visitInsn(DUP);
				// Push constant value '1' on the operand stack. This is the
				// index in the sub-array where to insert the field-name
				// (second
				// operand of AASTORE)
				visitInsn(ICONST_1);
				// Push the component-id on the operand stack. This is the
				// value
				// written into the sub-array at index 1 (third operand of
				// AASTORE)
				visitLdcInsn(namedComponentArr[idx][1]);
				// Store the component-id into the sub-array
				visitInsn(AASTORE);
			}

			// Store the field-type into the sub-array
			visitInsn(DUP);
			visitInsn(ICONST_2);
			visitLdcInsn(namedComponentArr[idx][2]);
			visitInsn(AASTORE);

			// Store the sub-array into the main-array
			visitInsn(AASTORE);

			if (idx < namedComponentArr.length - 1) {
				// Push a copy of the main-array reference on the operand
				// stack
				// (first operand of the last AASTORE in this loop)
				visitInsn(DUP);
			}
		}

		// Call the static method 'injectComponent' on class
		// 'ch.sourcepond.utils.podescoin.Injector'. The first and only operand
		// on
		// the operand stack is the main-array.
		visitMethodInsn(INVOKESTATIC, INJECTOR_INTERNAL_NAME, INJECTOR_METHOD_NAME, INJECTOR_METHOD_DESC, false);
		visitInsn(RETURN);

		// Specify maximum operand stack size and maximum local variable
		// count.
		visitMaxs(8, 2);
	}
}
