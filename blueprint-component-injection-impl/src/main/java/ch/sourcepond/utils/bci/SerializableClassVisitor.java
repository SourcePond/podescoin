package ch.sourcepond.utils.bci;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ICONST_2;
import static org.objectweb.asm.Opcodes.ICONST_3;
import static org.objectweb.asm.Opcodes.ICONST_4;
import static org.objectweb.asm.Opcodes.ICONST_5;
import static org.objectweb.asm.Type.getArgumentTypes;
import static org.objectweb.asm.Type.getInternalName;
import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getReturnType;
import static org.objectweb.asm.Type.getType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

abstract class SerializableClassVisitor extends ClassVisitor {
	private static final int _ICONST_0 = 0;
	private static final int _ICONST_1 = 1;
	private static final int _ICONST_2 = 2;
	private static final int _ICONST_3 = 3;
	private static final int _ICONST_4 = 4;
	private static final int _ICONST_5 = 5;
	protected static final String INJECT_BLUEPRINT_COMPONENTS_METHOD_NAME = "_$injectBlueprintComponents";
	protected static final String READ_OBJECT_METHOD_NAME = "readObject";
	protected static final String READ_OBJECT_METHOD_DESC = getMethodDescriptor(getType(void.class),
			getType(ObjectInputStream.class));
	protected static final String CLASS_NOT_FOUND_EXCEPTION_INTERNAL_NAME = getInternalName(
			ClassNotFoundException.class);
	protected static final String IO_EXCEPTION_INTERNAL_NAME = getInternalName(IOException.class);
	protected static final String[] READ_OBJECT_METHOD_EXCEPTIONS = new String[] { IO_EXCEPTION_INTERNAL_NAME,
			CLASS_NOT_FOUND_EXCEPTION_INTERNAL_NAME };
	protected static final String OBJECT_INPUT_STREAM_NAME = ObjectInputStream.class.getName();
	protected static final String VOID_NAME = void.class.getName();
	protected String thisClassInternalName;
	private boolean hasReadObjectMethod;

	protected SerializableClassVisitor(final ClassVisitor pWriter) {
		super(ASM5, pWriter);
	}

	protected void pushByteConstant(final MethodVisitor mv, final int idx) {
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

	@Override
	public void visit(final int version, final int access, final String name, final String signature,
			final String superName, final String[] interfaces) {
		thisClassInternalName = name;
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
			final String[] exceptions) {
		if (isReadObjectMethod(access, name, desc, exceptions) && isEnhancementNecessary()) {
			return new EnhanceReadObjectMethodVisitor(thisClassInternalName,
					super.visitMethod(access, name, desc, signature, exceptions));
		}
		return super.visitMethod(access, name, desc, signature, exceptions);
	}

	protected abstract boolean isEnhancementNecessary();

	protected abstract void enhanceReadObject(MethodVisitor mv);

	private MethodVisitor createMethodVisitor() {
		// If there is already a readObject method present, we need to create a
		// complete new method '_$injectBlueprintComponents'. The original
		// readObject method has already be enhanced so that
		// '_$injectBlueprintComponents' is called at the beginning.
		if (hasReadObjectMethod()) {
			return cv.visitMethod(ACC_PRIVATE, INJECT_BLUEPRINT_COMPONENTS_METHOD_NAME,
					getMethodDescriptor(getType(void.class)), null, null);
		}

		// If no readObject method could be found, it will be created now.
		return cv.visitMethod(ACC_PRIVATE, READ_OBJECT_METHOD_NAME, READ_OBJECT_METHOD_DESC, null,
				READ_OBJECT_METHOD_EXCEPTIONS);
	}

	protected boolean hasReadObjectMethod() {
		return hasReadObjectMethod;
	}

	@Override
	public void visitEnd() {
		// Create injection method if necessary; nly do something if at least 1
		// component is referenced.
		if (isEnhancementNecessary()) {
			enhanceReadObject(createMethodVisitor());
		}
		super.visitEnd();
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
	protected boolean isReadObjectMethod(final int access, final String name, final String desc,
			final String[] exceptions) {
		if (ACC_PRIVATE == access && READ_OBJECT_METHOD_NAME.equals(name) && exceptions != null
				&& exceptions.length == 2) {
			if (IO_EXCEPTION_INTERNAL_NAME.equals(exceptions[0])
					&& CLASS_NOT_FOUND_EXCEPTION_INTERNAL_NAME.equals(exceptions[1])) {
				final Type returnType = getReturnType(desc);

				if (VOID_NAME.equals(returnType.getClassName())) {
					final Type[] argumentTypes = getArgumentTypes(desc);

					// We need this information later when we generate the
					// concrete
					// injection method.
					final boolean b = argumentTypes.length == 1
							&& OBJECT_INPUT_STREAM_NAME.equals(argumentTypes[0].getClassName());
					if (b && !hasReadObjectMethod) {
						hasReadObjectMethod = true;
					}

					return b;
				}
			}
		}
		return false;
	}
}
