package ch.sourcepond.utils.podescoin.internal;

import static org.junit.Assert.assertTrue;

import java.io.Serializable;

import org.junit.Test;

import ch.sourcepond.utils.podescoin.ClassVisitorTest;
import ch.sourcepond.utils.podescoin.Recipient;
import ch.sourcepond.utils.podescoin.TestComponent;
import ch.sourcepond.utils.podescoin.api.Component;

public class VerifyClassAnnotationTest extends ClassVisitorTest {

	public static class VerifyClassAnnotation implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Component
		transient TestComponent component1;
	}

	@Test
	public void verifyClassAnnotation() throws Exception {
		final Class<?> cl = loader.loadClass(VerifyClassAnnotation.class.getName());
		assertTrue(cl.isAnnotationPresent(Recipient.class));
	}

}
