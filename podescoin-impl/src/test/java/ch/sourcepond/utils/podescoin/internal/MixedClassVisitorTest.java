package ch.sourcepond.utils.podescoin.internal;

import static org.junit.Assert.assertNotNull;

import java.io.Serializable;

import javax.inject.Inject;

import org.junit.Test;

import ch.sourcepond.utils.podescoin.ClassVisitorTest;
import ch.sourcepond.utils.podescoin.TestComponent;

public class MixedClassVisitorTest extends ClassVisitorTest {

	public static class ClassWithFieldsAndInjectionMethod implements Serializable {

		@Inject
		private transient TestComponent component1;

		private TestComponent component2;

		@Inject
		void inject(final TestComponent pComponent2) {
			assertNotNull("Field should have been injected before calling the injector method", component1);
			component2 = pComponent2;
		}
	}

	@Test
	public void verifyInjectionDoneInOrder() throws Exception {
		final Serializable obj = (Serializable) loader.loadClass(ClassWithFieldsAndInjectionMethod.class.getName())
				.newInstance();
		cloneObject(obj);
	}
}
