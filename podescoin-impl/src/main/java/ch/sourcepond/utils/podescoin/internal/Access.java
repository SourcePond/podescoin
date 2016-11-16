package ch.sourcepond.utils.podescoin.internal;

import org.objectweb.asm.Opcodes;

public class Access {

	public static boolean isPrivate(final int access) {
		return (access & Opcodes.ACC_PRIVATE) != 0;
	}

	public static boolean isProtected(final int access) {
		return (access & Opcodes.ACC_PROTECTED) != 0;
	}

	public static boolean isPublic(final int access) {
		return (access & Opcodes.ACC_PUBLIC) != 0;
	}

	public static boolean isFinal(final int access) {
		return (access & Opcodes.ACC_FINAL) != 0;
	}

	public static boolean isTransient(final int access) {
		return (access & Opcodes.ACC_TRANSIENT) != 0;
	}

	public static boolean isVolatile(final int access) {
		return (access & Opcodes.ACC_VOLATILE) != 0;
	}
}
