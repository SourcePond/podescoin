package ch.sourcepond.utils.bci;

import static ch.sourcepond.utils.bci.Constants.INJECTOR_INTERNAL_NAME;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_TRANSIENT;
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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

final class FieldInjectionClassVisitor extends SerializableClassVisitor {
	private static final String INJECTOR_METHOD_NAME = "injectComponents";
	private static final String INJECTOR_METHOD_DESC = getMethodDescriptor(getType(void.class),
			getType(Serializable.class), getType(String[][].class));
	private static final String FIRST_DIMENSION_INTERNAL_NAME = getInternalName(String[].class);
	private static final String SECOND_DIMENSION_INTERNAL_NAME = getInternalName(String.class);
	private List<String[]> namedComponents;

	public FieldInjectionClassVisitor(final ClassVisitor pWriter) {
		super(pWriter);
	}

	@Override
	public FieldVisitor visitField(final int access, final String name, final String desc, final String signature,
			final Object value) {
		if ((access & ACC_TRANSIENT) != 0 && (access & Opcodes.ACC_FINAL) == 0) {
			return new ComponentFieldVisitor(this, cv.visitField(access, name, desc, signature, value), name,
					getType(desc).getClassName());
		}
		return super.visitField(access, name, desc, signature, value);
	}

	@Override
	protected boolean isEnhancementNecessary() {
		return namedComponents != null;
	}

	public void addNamedComponent(final String pFieldName, final String pComponentIdOrNull, final String pTypeName) {
		if (namedComponents == null) {
			namedComponents = new LinkedList<>();
		}
		namedComponents.add(new String[] { pFieldName, pComponentIdOrNull, pTypeName });
	}

	@Override
	protected void enhanceReadObject(final MethodVisitor mv) {
		final String[][] namedComponentArr = new String[namedComponents.size()][2];
		namedComponents.toArray(namedComponentArr);

		mv.visitCode();

		// Load 'this' reference on operand stack (first operand for calling
		// static method ch.sourcepond.utils.bci.Injector#injectComponent)
		mv.visitVarInsn(ALOAD, 0);

		// Push length of two-dimensional array on operand stack (first
		// operand for ANEWARRAY)
		mv.visitIntInsn(BIPUSH, namedComponentArr.length);

		// Push internal name of String[].class on operand stack. Note: the
		// internal name is '[Ljava/lang/String;' and NOT
		// '[[Ljava/lang/String;' because we will only create the first
		// dimension of the two-dimension string array. The second dimension
		// will contain the field-name and the component-id.
		mv.visitTypeInsn(ANEWARRAY, FIRST_DIMENSION_INTERNAL_NAME);

		// Duplicate the array reference on top of the operand stack. This
		// is necessary because we start now to add the second dimension
		// arrays. The original reference will later be used as second
		// operand for calling static method
		// ch.sourcepond.utils.bci.Injector#injectComponent.
		mv.visitInsn(DUP);

		for (int idx = 0; idx < namedComponentArr.length; idx++) {

			// Push the current index on the operand stack (first
			// dimension). This will be the operand for adding the sub-array
			// to the two-dimensional array at the end of the loop.
			pushByteConstant(mv, idx);

			// Push the the constant value '3' on the operand stack. This is
			// used as operand for ANEWARRAY to create an array of size 3.
			mv.visitInsn(ICONST_3);

			// Create sub-array of size '2'
			mv.visitTypeInsn(ANEWARRAY, SECOND_DIMENSION_INTERNAL_NAME);

			// Push a copy of the sub-array reference on the stack; this
			// will be used as array-ref
			// (first operand for AASTORE) for
			// the insertion of the field-name.
			mv.visitInsn(DUP);

			// Push constant value '0' on the operand stack. This is the
			// index in the sub-array where to insert the field-name (second
			// operand of AASTORE)
			mv.visitInsn(ICONST_0);

			// Push the field-name on the operand stack. This is the value
			// written into the sub-array at index 0 (third operand of
			// AASTORE)
			mv.visitLdcInsn(namedComponentArr[idx][0]);

			// Store the field-name into the sub-array
			mv.visitInsn(AASTORE);

			if (namedComponentArr[idx][1] != null) {
				// Push a copy of the sub-array reference on the stack; this
				// will be used as array-ref
				// (first operand for AASTORE) for
				// the insertion of the component-id.
				mv.visitInsn(DUP);
				// Push constant value '1' on the operand stack. This is the
				// index in the sub-array where to insert the field-name
				// (second
				// operand of AASTORE)
				mv.visitInsn(ICONST_1);
				// Push the component-id on the operand stack. This is the
				// value
				// written into the sub-array at index 1 (third operand of
				// AASTORE)
				mv.visitLdcInsn(namedComponentArr[idx][1]);
				// Store the component-id into the sub-array
				mv.visitInsn(AASTORE);
			}

			// Store the field-type into the sub-array
			mv.visitInsn(DUP);
			mv.visitInsn(ICONST_2);
			mv.visitLdcInsn(namedComponentArr[idx][2]);
			mv.visitInsn(AASTORE);

			// Store the sub-array into the main-array
			mv.visitInsn(AASTORE);

			if (idx < namedComponentArr.length - 1) {
				// Push a copy of the main-array reference on the operand
				// stack
				// (first operand of the last AASTORE in this loop)
				mv.visitInsn(DUP);
			}
		}

		// Call the static method 'injectComponent' on class
		// 'ch.sourcepond.utils.bci.Injector'. The first and only operand on
		// the operand stack is the main-array.
		mv.visitMethodInsn(INVOKESTATIC, INJECTOR_INTERNAL_NAME, INJECTOR_METHOD_NAME, INJECTOR_METHOD_DESC, false);
		mv.visitInsn(RETURN);

		// Specify maximum operand stack size and maximum local variable
		// count.
		mv.visitMaxs(8, 2);
	}
}
