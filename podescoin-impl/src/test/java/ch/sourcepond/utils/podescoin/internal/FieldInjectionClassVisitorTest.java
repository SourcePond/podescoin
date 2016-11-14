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

import static ch.sourcepond.utils.podescoin.internal.SerializableClassVisitor.INJECT_BLUEPRINT_COMPONENTS_METHOD_NAME;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.objectweb.asm.ClassVisitor;

import ch.sourcepond.utils.podescoin.ClassVisitorTest;
import ch.sourcepond.utils.podescoin.TestClassLoader;
import ch.sourcepond.utils.podescoin.TestComponent;
import ch.sourcepond.utils.podescoin.internal.FieldInjectionClassVisitor;

public class FieldInjectionClassVisitorTest extends ClassVisitorTest {

	@Override
	protected ClassVisitor newVisitor() {
		return new FieldInjectionClassVisitor(writer);
	}

	/**
	 * Test-class for verifying pushByteConstant
	 *
	 */
	public static class VerifyPushByteConstant implements Serializable {
		@Inject
		private transient TestComponent component1;
		@Inject
		private transient TestComponent component2;
		@Inject
		private transient TestComponent component3;
		@Inject
		private transient TestComponent component4;
		@Inject
		private transient TestComponent component5;
		@Inject
		private transient TestComponent component6;
		@Inject
		private transient TestComponent component7;
		@Inject
		private transient TestComponent component8;
		@Inject
		private transient TestComponent component9;
		@Inject
		private transient TestComponent component10;
	}

	@Test
	public void verifyPushByteConstant() throws Exception {
		loader = new TestClassLoader(visitor, writer, VerifyPushByteConstant.class, bundle);
		final Class<?> enhancedClass = loader.loadClass(VerifyPushByteConstant.class.getName());

		try {
			// This method should NOT exist
			enhancedClass.getDeclaredMethod(INJECT_BLUEPRINT_COMPONENTS_METHOD_NAME);
			fail("Exception expected");
		} catch (final NoSuchMethodException expected) {
			// noop
		}

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
		@Named("componentId1")
		@Inject
		private transient TestComponent component1;
		@Named("componentId2")
		@Inject
		private transient TestComponent component2;
		@Named("componentId3")
		@Inject
		private transient TestComponent component3;
		@Named("componentId4")
		@Inject
		private transient TestComponent component4;
		@Named("componentId5")
		@Inject
		private transient TestComponent component5;
		@Named("componentId6")
		@Inject
		private transient TestComponent component6;
		@Named("componentId7")
		@Inject
		private transient TestComponent component7;
		@Named("componentId8")
		@Inject
		private transient TestComponent component8;
		@Named("componentId9")
		@Inject
		private transient TestComponent component9;
		@Named("componentId10")
		@Inject
		private transient TestComponent component10;
	}

	@Test
	public void verifyPushByteConstantWithId() throws Exception {
		loader = new TestClassLoader(visitor, writer, VerifyPushByteConstantWithId.class, bundle);
		final Class<?> enhancedClass = loader.loadClass(VerifyPushByteConstantWithId.class.getName());

		try {
			// This method should NOT exist
			enhancedClass.getDeclaredMethod(INJECT_BLUEPRINT_COMPONENTS_METHOD_NAME);
			fail("Exception expected");
		} catch (final NoSuchMethodException expected) {
			// noop
		}

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
		@Named("componentId1")
		@Inject
		private transient TestComponent component1;

		private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
			in.defaultReadObject();
		}
	}

	@Test
	public void verifyPushByteConstantReadObjectAlreadyDefined() throws Exception {
		loader = new TestClassLoader(visitor, writer, VerifyPushByteConstantReadObjectAlreadyDefined.class, bundle);
		final Class<?> enhancedClass = loader.loadClass(VerifyPushByteConstantReadObjectAlreadyDefined.class.getName());

		// This method should exist
		enhancedClass.getDeclaredMethod(INJECT_BLUEPRINT_COMPONENTS_METHOD_NAME);

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
		@Named("componentId1")
		@Inject
		private transient final TestComponent component1 = new TestComponent();

		private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
			in.defaultReadObject();
		}
	}

	@Test
	public void doNotVisitFinalField() throws Exception {
		loader = new TestClassLoader(visitor, writer, DoNotVisitFinalField.class, bundle);
		final Class<?> enhancedClass = loader.loadClass(DoNotVisitFinalField.class.getName());

		try {
			// This method should NOT exist
			enhancedClass.getDeclaredMethod(INJECT_BLUEPRINT_COMPONENTS_METHOD_NAME);
			fail("Exception expected");
		} catch (final NoSuchMethodException expected) {
			// noop
		}

		// This should not throw an exception
		final Serializable obj = (Serializable) enhancedClass.newInstance();
		getMethod(obj, "readObject", ObjectInputStream.class).invoke(obj, mock(ObjectInputStream.class));

		verifyZeroInteractions(injector);
	}

	/**
	 * Test-class for verifying pushByteConstant
	 *
	 */
	public static class DoNotVisitPersistentField implements Serializable {
		@Named("componentId1")
		@Inject
		private final TestComponent component1 = new TestComponent();

		private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
			in.defaultReadObject();
		}
	}

	@Test
	public void doNotVisitPersistentField() throws Exception {
		loader = new TestClassLoader(visitor, writer, DoNotVisitPersistentField.class, bundle);
		final Class<?> enhancedClass = loader.loadClass(DoNotVisitPersistentField.class.getName());

		try {
			// This method should NOT exist
			enhancedClass.getDeclaredMethod(INJECT_BLUEPRINT_COMPONENTS_METHOD_NAME);
			fail("Exception expected");
		} catch (final NoSuchMethodException expected) {
			// noop
		}

		// This should not throw an exception
		final Serializable obj = (Serializable) enhancedClass.newInstance();
		getMethod(obj, "readObject", ObjectInputStream.class).invoke(obj, mock(ObjectInputStream.class));

		verifyZeroInteractions(injector);
	}
}
