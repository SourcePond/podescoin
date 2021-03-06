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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import ch.sourcepond.utils.podescoin.ClassVisitorTest;
import ch.sourcepond.utils.podescoin.IllegalFieldDeclarationException;
import ch.sourcepond.utils.podescoin.TestComponent;
import ch.sourcepond.utils.podescoin.api.Component;

public class ReadObjectFieldInjectionClassVisitorTest extends ClassVisitorTest {

	/**
	 * Test-class for verifying pushByteConstant
	 *
	 */
	public static class VerifyPushByteConstant implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		@Component
		transient TestComponent component1;
		@Component
		transient TestComponent component2;
		@Component
		transient TestComponent component3;
		@Component
		transient TestComponent component4;
		@Component
		transient TestComponent component5;
		@Component
		transient TestComponent component6;
		@Component
		transient TestComponent component7;
		@Component
		transient TestComponent component8;
		@Component
		transient TestComponent component9;
		@Component
		transient TestComponent component10;
	}

	@Test
	public void verifyPushByteConstant() throws Exception {
		final Class<?> enhancedClass = loader.loadClass(VerifyPushByteConstant.class.getName());

		// This should not throw an exception
		final Serializable obj = (Serializable) enhancedClass.newInstance();
		getMethod(obj, "readObject", ObjectInputStream.class).invoke(obj, mock(ObjectInputStream.class));

		verify(injector).initDeserializedObject(Mockito.same(obj),
				Mockito.eq(new String[][] { { "component1", null, TestComponent.class.getName() },
						{ "component2", null, TestComponent.class.getName() },
						{ "component3", null, TestComponent.class.getName() },
						{ "component4", null, TestComponent.class.getName() },
						{ "component5", null, TestComponent.class.getName() },
						{ "component6", null, TestComponent.class.getName() },
						{ "component7", null, TestComponent.class.getName() },
						{ "component8", null, TestComponent.class.getName() },
						{ "component9", null, TestComponent.class.getName() },
						{ "component10", null, TestComponent.class.getName() } }));
	}

	/**
	 * Test-class for verifying pushByteConstant
	 *
	 */
	public static class VerifyPushByteConstantWithId implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		@Component("componentId1")
		private transient TestComponent component1;

		@Component("componentId2")
		private transient TestComponent component2;

		@Component("componentId3")
		private transient TestComponent component3;

		@Component("componentId4")
		private transient TestComponent component4;

		@Component("componentId5")
		private transient TestComponent component5;

		@Component("componentId6")
		private transient TestComponent component6;

		@Component("componentId7")
		private transient TestComponent component7;

		@Component("componentId8")
		private transient TestComponent component8;

		@Component("componentId9")
		private transient TestComponent component9;

		@Component("componentId10")
		private transient TestComponent component10;
	}

	@Test
	public void verifyPushByteConstantWithId() throws Exception {
		final Class<?> enhancedClass = loader.loadClass(VerifyPushByteConstantWithId.class.getName());

		// This should not throw an exception
		final Serializable obj = (Serializable) enhancedClass.newInstance();
		getMethod(obj, "readObject", ObjectInputStream.class).invoke(obj, mock(ObjectInputStream.class));

		verify(injector).initDeserializedObject(Mockito.same(obj),
				Mockito.eq(new String[][] { { "component1", "componentId1", TestComponent.class.getName() },
						{ "component2", "componentId2", TestComponent.class.getName() },
						{ "component3", "componentId3", TestComponent.class.getName() },
						{ "component4", "componentId4", TestComponent.class.getName() },
						{ "component5", "componentId5", TestComponent.class.getName() },
						{ "component6", "componentId6", TestComponent.class.getName() },
						{ "component7", "componentId7", TestComponent.class.getName() },
						{ "component8", "componentId8", TestComponent.class.getName() },
						{ "component9", "componentId9", TestComponent.class.getName() },
						{ "component10", "componentId10", TestComponent.class.getName() } }));
	}

	/**
	 * Test-class for verifying pushByteConstant
	 *
	 */
	public static class VerifyPushByteConstantReadObjectAlreadyDefined implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Component("componentId1")
		private transient TestComponent component1;

		private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
			in.defaultReadObject();
		}
	}

	@Test
	public void verifyPushByteConstantReadObjectAlreadyDefined() throws Exception {
		final Class<?> enhancedClass = loader.loadClass(VerifyPushByteConstantReadObjectAlreadyDefined.class.getName());

		final Serializable obj = (Serializable) enhancedClass.newInstance();
		final ObjectInputStream objInStream = mock(ObjectInputStream.class);
		getMethod(obj, "readObject", ObjectInputStream.class).invoke(obj, objInStream);

		final InOrder order = inOrder(injector, objInStream);

		order.verify(injector).initDeserializedObject(Mockito.same(obj),
				Mockito.eq(new String[][] { { "component1", "componentId1", TestComponent.class.getName() } }));
		order.verify(objInStream).defaultReadObject();
	}

	/**
	 * Test-class for verifying pushByteConstant
	 *
	 */
	public static class DoNotVisitFinalField implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Component("componentId1")
		private transient final TestComponent component1 = new TestComponent();

		private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
			in.defaultReadObject();
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
	public static class DoNotVisitPersistentField implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Component("componentId1")
		private final TestComponent component1 = new TestComponent();

		private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
			in.defaultReadObject();
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
