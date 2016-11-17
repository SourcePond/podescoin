package ch.sourcepond.utils.podescoin.internal;

import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ICONST_2;
import static org.objectweb.asm.Opcodes.ICONST_3;
import static org.objectweb.asm.Opcodes.ICONST_4;
import static org.objectweb.asm.Opcodes.ICONST_5;
import static org.objectweb.asm.Opcodes.RETURN;

import org.objectweb.asm.MethodVisitor;

public abstract class ReadObjectVisitor extends MethodVisitor {
	private static final int _ICONST_0 = 0;
	private static final int _ICONST_1 = 1;
	private static final int _ICONST_2 = 2;
	private static final int _ICONST_3 = 3;
	private static final int _ICONST_4 = 4;
	private static final int _ICONST_5 = 5;
	protected final InspectClassVisitor inspector;
	private boolean codeVisited = false;
	private int maxStack;
	private int maxLocals;

	public ReadObjectVisitor(final InspectClassVisitor pInspector, final MethodVisitor mv) {
		super(ASM5, mv);
		inspector = pInspector;
	}

	protected final void pushByteConstant(final MethodVisitor mv, final int idx) {
		switch (idx) {
		case _ICONST_0: {
			visitInsn(ICONST_0);
			break;
		}
		case _ICONST_1: {
			visitInsn(ICONST_1);
			break;
		}
		case _ICONST_2: {
			visitInsn(ICONST_2);
			break;
		}
		case _ICONST_3: {
			visitInsn(ICONST_3);
			break;
		}
		case _ICONST_4: {
			visitInsn(ICONST_4);
			break;
		}
		case _ICONST_5: {
			visitInsn(ICONST_5);
			break;
		}
		default: {
			visitIntInsn(BIPUSH, idx);
		}
		}
	}

	@Override
	public final void visitCode() {
		if (inspector.isInEnhanceMode()) {
			if (!codeVisited) {
				super.visitCode();
				codeVisited = true;
			}
		} else {
			super.visitCode();
		}
	}

	@Override
	public final void visitInsn(final int opcode) {
		if (opcode == RETURN) {
			return;
		}
		super.visitInsn(opcode);
	}

	@Override
	public final void visitMaxs(final int maxStack, final int maxLocals) {
		if (maxStack > this.maxStack) {
			this.maxStack = maxStack;
		}

		if (maxLocals > this.maxLocals) {
			this.maxLocals = maxLocals;
		}
	}

	@Override
	public final void visitEnd() {
		// noop
	}

	public abstract void visitEnhance();

	public void visitEndEnhance() {
		super.visitInsn(RETURN);
		super.visitMaxs(maxStack, maxLocals);
		super.visitEnd();
	}
}
