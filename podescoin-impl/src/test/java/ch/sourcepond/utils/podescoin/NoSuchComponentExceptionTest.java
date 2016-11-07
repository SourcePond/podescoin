package ch.sourcepond.utils.podescoin;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.sourcepond.utils.podescoin.NoSuchComponentException;

public class NoSuchComponentExceptionTest {

	@Test
	public void verifyMessage() {
		final NoSuchComponentException ex = new NoSuchComponentException("anyField", 0, Object.class);
		assertEquals("No component found which matches field 'anyField' with type 'java.lang.Object'", ex.getMessage());
	}
}
