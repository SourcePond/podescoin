package ch.sourcepond.utils.bci;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_TRANSIENT;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ICONST_2;
import static org.objectweb.asm.Opcodes.ICONST_3;
import static org.objectweb.asm.Opcodes.ICONST_4;
import static org.objectweb.asm.Opcodes.ICONST_5;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Type.getArgumentTypes;
import static org.objectweb.asm.Type.getInternalName;
import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getReturnType;
import static org.objectweb.asm.Type.getType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

final class SerializableClassVisitor extends ClassVisitor {
	private static final int _ICONST_0 = 0;
	private static final int _ICONST_1 = 1;
	private static final int _ICONST_2 = 2;
	private static final int _ICONST_3 = 3;
	private static final int _ICONST_4 = 4;
	private static final int _ICONST_5 = 5;
	private static final String IO_EXCEPTION_INTERNAL_NAME = getInternalName(IOException.class);
	private static final String CLASS_NOT_FOUND_EXCEPTION_INTERNAL_NAME = getInternalName(ClassNotFoundException.class);
	private static final String READ_OBJECT_METHOD_NAME = "readObject";
	private static final String READ_OBJECT_METHOD_DESC = getMethodDescriptor(getType(void.class),
			getType(ObjectInputStream.class));
	private static final String[] READ_OBJECT_METHOD_EXCEPTIONS = new String[] { IO_EXCEPTION_INTERNAL_NAME,
			CLASS_NOT_FOUND_EXCEPTION_INTERNAL_NAME };
	static final String INJECT_BLUEPRINT_COMPONENTS_METHOD_NAME = "_$injectBlueprintComponents";
	private static final String VOID_NAME = void.class.getName();
	private static final String OBJECT_INPUT_STREAM_NAME = ObjectInputStream.class.getName();
	private static final String INJECTOR_INTERNAL_NAME = getInternalName(Injector.class);
	private static final String INJECTOR_METHOD_NAME = "injectComponents";
	private static final String INJECTOR_METHOD_DESC = getMethodDescriptor(getType(void.class),
			getType(Serializable.class), getType(String[][].class));
	private static final String FIRST_DIMENSION_INTERNAL_NAME = getInternalName(String[].class);
	private static final String SECOND_DIMENSION_INTERNAL_NAME = getInternalName(String.class);
	private List<String[]> namedComponents;
	private String thisClassInternalName;
	private boolean hasReadObjectMethod;

	public SerializableClassVisitor(final ClassVisitor pWriter) {
		super(ASM5, pWriter);
	}

	@Override
	public void visit(final int version, final int access, final String name, final String signature,
			final String superName, final String[] interfaces) {
		thisClassInternalName = name;
		super.visit(version, access, name, signature, superName, interfaces);
	}

	/**
	 * Determines whether the current method is the readObject method with
	 * following signature:
	 * 
	 * <pre>
	 * private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	 * </pre>
	 * 
	 * See {@link Serializable} for further information.
	 * 
	 * @param access
	 *            the method's access flags (see {@link Opcodes}). This
	 *            parameter also indicates if the method is synthetic and/or
	 *            deprecated.
	 * @param name
	 *            the method's name.
	 * @param desc
	 *            the method's descriptor (see {@link Type}).
	 * @param exceptions
	 *            the internal names of the method's exception classes (see
	 *            {@link Type#getInternalName()} ). May be null.
	 * @return {@code true} if the method specified is the readObject method as
	 *         described by {@link Serializable}, {@code false} otherwise
	 */
	private static boolean isReadObjectMethod(final int access, final String name, final String desc,
			final String[] exceptions) {
		if (ACC_PRIVATE == access && READ_OBJECT_METHOD_NAME.equals(name) && exceptions != null
				&& exceptions.length == 2) {
			if (IO_EXCEPTION_INTERNAL_NAME.equals(exceptions[0])
					&& CLASS_NOT_FOUND_EXCEPTION_INTERNAL_NAME.equals(exceptions[1])) {
				final Type returnType = getReturnType(desc);

				if (VOID_NAME.equals(returnType.getClassName())) {
					final Type[] argumentTypes = getArgumentTypes(desc);
					return argumentTypes.length == 1
							&& OBJECT_INPUT_STREAM_NAME.equals(argumentTypes[0].getClassName());
				}
			}
		}
		return false;
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

	private boolean isReadObjectEnhancementNecessary() {
		return namedComponents != null;
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
			final String[] exceptions) {
		if (isReadObjectMethod(access, name, desc, exceptions)) {

			// We need this information later when we generate the concrete
			// injection method.
			hasReadObjectMethod = true;

			if (isReadObjectEnhancementNecessary()) {
				return new EnhanceReadObjectMethodVisitor(thisClassInternalName,
						super.visitMethod(access, name, desc, signature, exceptions));
			}
		}
		return super.visitMethod(access, name, desc, signature, exceptions);
	}

	public void addNamedComponent(final String pFieldName, final String pComponentIdOrNull, final String pTypeName) {
		if (namedComponents == null) {
			namedComponents = new LinkedList<>();
		}
		namedComponents.add(new String[] { pFieldName, pComponentIdOrNull, pTypeName });
	}

	private void pushByteConstant(final MethodVisitor mv, final int idx) {
		switch (idx) {
		case _ICONST_0: {
			mv.visitInsn(ICONST_0);
			break;
		}
		case _ICONST_1: {
			mv.visitInsn(ICONST_1);
			break;
		}
		case _ICONST_2: {
			mv.visitInsn(ICONST_2);
			break;
		}
		case _ICONST_3: {
			mv.visitInsn(ICONST_3);
			break;
		}
		case _ICONST_4: {
			mv.visitInsn(ICONST_4);
			break;
		}
		case _ICONST_5: {
			mv.visitInsn(ICONST_5);
			break;
		}
		default: {
			mv.visitIntInsn(BIPUSH, idx);
		}
		}
	}

	private MethodVisitor createMethodVisitor() {
		// If there is already a readObject method present, we need to create a
		// complete new method '_$injectBlueprintComponents'. The original
		// readObject method has already be enhanced so that
		// '_$injectBlueprintComponents' is called at the beginning.
		if (hasReadObjectMethod) {
			return cv.visitMethod(ACC_PRIVATE, INJECT_BLUEPRINT_COMPONENTS_METHOD_NAME,
					getMethodDescriptor(getType(void.class)), null, null);
		}

		// If no readObject method could be found, it will be created now.
		return cv.visitMethod(ACC_PRIVATE, READ_OBJECT_METHOD_NAME, READ_OBJECT_METHOD_DESC, null,
				READ_OBJECT_METHOD_EXCEPTIONS);
	}

	private void generateInjectionMethod() {
		// Only do something if at least 1 component is referenced.
		if (isReadObjectEnhancementNecessary()) {
			final String[][] namedComponentArr = new String[namedComponents.size()][2];
			namedComponents.toArray(namedComponentArr);

			final MethodVisitor mv = createMethodVisitor();
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

	@Override
	public void visitEnd() {
		// Create injection method if necessary
		generateInjectionMethod();
	}
}
