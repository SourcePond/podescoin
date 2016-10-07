package ch.sourcepond.utils.bci;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NoSuchComponentExceptionTest {

	@Test
	public void verifyMessage() {
		final NoSuchComponentException ex = new NoSuchComponentException("anyField", Object.class);
		assertEquals("No component found which matches field 'anyField' with type 'java.lang.Object'", ex.getMessage());
	}
}
