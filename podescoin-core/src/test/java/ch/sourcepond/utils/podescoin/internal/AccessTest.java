package ch.sourcepond.utils.podescoin.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.objectweb.asm.Opcodes;

public class AccessTest {

	@Test
	public void isPrivate() {
		assertTrue(Access.isPrivate(Opcodes.ACC_PRIVATE));
		assertFalse(Access.isPrivate(0));
	}

	@Test
	public void isProtected() {
		assertTrue(Access.isProtected(Opcodes.ACC_PROTECTED));
		assertFalse(Access.isProtected(0));
	}

	@Test
	public void isPublic() {
		assertTrue(Access.isPublic(Opcodes.ACC_PUBLIC));
		assertFalse(Access.isPublic(0));
	}

	@Test
	public void isFinal() {
		assertTrue(Access.isFinal(Opcodes.ACC_FINAL));
		assertFalse(Access.isFinal(0));
	}

	@Test
	public void isTransient() {
		assertTrue(Access.isTransient(Opcodes.ACC_TRANSIENT));
		assertFalse(Access.isTransient(0));
	}
}
