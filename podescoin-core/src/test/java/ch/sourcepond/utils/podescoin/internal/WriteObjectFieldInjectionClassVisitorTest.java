/*Copyright (C) 2016 Roland Hauser, <sourcepond@gmail.com>
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package ch.sourcepond.utils.podescoin.internal;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import ch.sourcepond.utils.podescoin.ClassVisitorTest;
import ch.sourcepond.utils.podescoin.IllegalFieldDeclarationException;
import ch.sourcepond.utils.podescoin.TestComponent;
import ch.sourcepond.utils.podescoin.api.Recipient;

public class WriteObjectFieldInjectionClassVisitorTest extends ClassVisitorTest {

	/**
	 * Test-class for verifying pushByteConstant
	 *
	 */
	@Recipient
	public static class VerifyPushByteConstant implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		@Inject
		transient TestComponent component1;
		@Inject
		transient TestComponent component2;
		@Inject
		transient TestComponent component3;
		@Inject
		transient TestComponent component4;
		@Inject
		transient TestComponent component5;
		@Inject
		transient TestComponent component6;
		@Inject
		transient TestComponent component7;
		@Inject
		transient TestComponent component8;
		@Inject
		transient TestComponent component9;
		@Inject
		transient TestComponent component10;
	}

	@Test
	public void verifyNoWriteObjectGenerated() throws Exception {
		final Class<?> enhancedClass = loader.loadClass(VerifyPushByteConstant.class.getName());

		// This should not throw an exception
		final Serializable obj = (Serializable) enhancedClass.newInstance();

		try {
			getMethod(obj, "writeObject", ObjectOutputStream.class).invoke(obj, mock(ObjectOutputStream.class));
			fail("Exception expected");
		} catch (final NoSuchMethodException e) {
			// noop
		}
	}

	/**
	 * Test-class for verifying pushByteConstant
	 *
	 */
	@Recipient
	public static class VerifyPushByteConstantWriteObjectAlreadyDefined implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Named("componentId1")
		@Inject
		private transient TestComponent component1;

		private void writeObject(final ObjectOutputStream out) throws IOException {
			out.defaultWriteObject();
		}
	}

	@Test
	public void verifyWriteObjectAlreadyDefined() throws Exception {
		final Class<?> enhancedClass = loader
				.loadClass(VerifyPushByteConstantWriteObjectAlreadyDefined.class.getName());

		final Serializable obj = (Serializable) enhancedClass.newInstance();
		final ObjectOutputStream objOutStream = mock(ObjectOutputStream.class);
		getMethod(obj, "writeObject", ObjectOutputStream.class).invoke(obj, objOutStream);

		final InOrder order = inOrder(injector, objOutStream);

		order.verify(injector).initDeserializedObject(Mockito.same(obj),
				Mockito.eq(new String[][] { { "component1", "componentId1", TestComponent.class.getName() } }));
		order.verify(objOutStream).defaultWriteObject();
	}

	/**
	 * Test-class for verifying pushByteConstant
	 *
	 */
	@Recipient
	public static class DoNotVisitFinalField implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Named("componentId1")
		@Inject
		private transient final TestComponent component1 = new TestComponent();

		private void writeObject(final ObjectOutputStream out) throws IOException {
			out.defaultWriteObject();
		}
	}

	@Test
	public void doNotVisitFinalField() throws Exception {
		try {
			loader.loadClass(DoNotVisitFinalField.class.getName());
			fail("Exception expected here");
		} catch (final IllegalFieldDeclarationException expected) {
			// noop
		}

		verifyZeroInteractions(injector);
	}

	/**
	 * Test-class for verifying pushByteConstant
	 *
	 */
	@Recipient
	public static class DoNotVisitPersistentField implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Named("componentId1")
		@Inject
		private final TestComponent component1 = new TestComponent();

		private void writeObject(final ObjectOutputStream out) throws IOException {
			out.defaultWriteObject();
		}
	}

	@Test
	public void doNotVisitPersistentField() throws Exception {
		try {
			loader.loadClass(DoNotVisitPersistentField.class.getName());
			fail("Exception expected");
		} catch (final IllegalFieldDeclarationException ex) {
			// noop
		}

		verifyZeroInteractions(injector);
	}
}
