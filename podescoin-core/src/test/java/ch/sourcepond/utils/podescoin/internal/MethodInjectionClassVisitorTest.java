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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Test;
import org.mockito.InOrder;

import ch.sourcepond.utils.podescoin.ClassVisitorTest;
import ch.sourcepond.utils.podescoin.Recipient;
import ch.sourcepond.utils.podescoin.TestComponent;
import ch.sourcepond.utils.podescoin.internal.method.SuperMethodInvokationException;

public class MethodInjectionClassVisitorTest extends ClassVisitorTest {

	private static Object getFieldValue(final String pFieldName, final Object pObj) throws Exception {
		final Field f = pObj.getClass().getDeclaredField(pFieldName);
		f.setAccessible(true);
		try {
			return f.get(pObj);
		} finally {
			f.setAccessible(false);
		}
	}

	@Recipient
	public static class ReadObjectSpecified_WithType implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		boolean injectCalledBeforeInject;
		private TestComponent component1;

		@Inject
		public void injectServices(final TestComponent pComponent1) {
			component1 = pComponent1;
		}

		private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
			injectCalledBeforeInject = component1 != null;
		}
	}

	@Test
	public void readObjectSpecified_WithType() throws Exception {
		final Class<?> enhancedClass = loader.loadClass(ReadObjectSpecified_WithType.class.getName());
		when(injector.getComponentByTypeName(TestComponent.class.getName(), 0)).thenReturn(component1);

		// This should not throw an exception
		final Serializable obj = (Serializable) enhancedClass.newInstance();
		getMethod(obj, "readObject", ObjectInputStream.class).invoke(obj, mock(ObjectInputStream.class));

		verify(injector).getComponentByTypeName(TestComponent.class.getName(), 0);
		assertSame(component1, getFieldValue("component1", obj));
		assertTrue((Boolean) getFieldValue("injectCalledBeforeInject", obj));
	}

	@Recipient
	public static class NoReadObjectSpecified_WithType implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		TestComponent component1;

		@Inject
		public void injectServices(final TestComponent pComponent1) {
			component1 = pComponent1;
		}
	}

	@Test
	public void noReadObjectSpecified_WithType() throws Exception {
		final Class<?> enhancedClass = loader.loadClass(NoReadObjectSpecified_WithType.class.getName());
		when(injector.getComponentByTypeName(TestComponent.class.getName(), 0)).thenReturn(component1);

		// This should not throw an exception
		final Serializable obj = (Serializable) enhancedClass.newInstance();
		getMethod(obj, "readObject", ObjectInputStream.class).invoke(obj, mock(ObjectInputStream.class));

		verify(injector).getComponentByTypeName(TestComponent.class.getName(), 0);
		assertSame(component1, getFieldValue("component1", obj));
	}

	@Recipient
	public static class NoReadObjectSpecified_WithType_And_ObjectInputStream implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		TestComponent component1;

		@Inject
		public void injectServices(final ObjectInputStream in, final TestComponent pComponent1)
				throws ClassNotFoundException, IOException {
			in.defaultReadObject();
			component1 = pComponent1;
		}
	}

	@Test
	public void noReadObjectSpecified_WithType_And_ObjectInputStream() throws Exception {
		final Class<?> enhancedClass = loader
				.loadClass(NoReadObjectSpecified_WithType_And_ObjectInputStream.class.getName());
		when(injector.getComponentByTypeName(TestComponent.class.getName(), 0)).thenReturn(component1);

		// This should not throw an exception
		final Serializable obj = (Serializable) enhancedClass.newInstance();
		final ObjectInputStream in = mock(ObjectInputStream.class);
		getMethod(obj, "readObject", ObjectInputStream.class).invoke(obj, in);

		verify(injector).getComponentByTypeName(TestComponent.class.getName(), 0);
		verify(in).defaultReadObject();
		assertSame(component1, getFieldValue("component1", obj));
	}

	@Recipient
	public static class NoReadObjectSpecified_WithComponentId implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		TestComponent component1;
		TestComponent component2;

		@Inject
		public void injectServices(@Named("componentId1") final TestComponent pComponent1,
				@Named("componentId2") final TestComponent pComponent2) {
			component1 = pComponent1;
			component2 = pComponent2;
		}
	}

	@Test
	public void noReadObjectSpecified_WithComponentId() throws Exception {
		final Class<?> enhancedClass = loader.loadClass(NoReadObjectSpecified_WithComponentId.class.getName());
		when(injector.getComponentById("componentId1", TestComponent.class.getName(), 0)).thenReturn(component1);
		when(injector.getComponentById("componentId2", TestComponent.class.getName(), 1)).thenReturn(component2);

		// This should not throw an exception
		final Serializable obj = (Serializable) enhancedClass.newInstance();
		getMethod(obj, "readObject", ObjectInputStream.class).invoke(obj, mock(ObjectInputStream.class));

		final InOrder order = inOrder(injector);
		order.verify(injector).getComponentById("componentId1", TestComponent.class.getName(), 0);
		order.verify(injector).getComponentById("componentId2", TestComponent.class.getName(), 1);

		assertSame(component1, getFieldValue("component1", obj));
		assertSame(component2, getFieldValue("component2", obj));
	}

	@Recipient
	public static class NoReadObjectSpecified_WithComponentId_And_ObjectInputStream implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private TestComponent component1;
		private TestComponent component2;

		@Inject
		public void injectServices(final ObjectInputStream in, @Named("componentId1") final TestComponent pComponent1,
				@Named("componentId2") final TestComponent pComponent2) throws IOException, ClassNotFoundException {
			in.defaultReadObject();
			component1 = pComponent1;
			component2 = pComponent2;

			System.out.println(in);
			System.out.println(component1);
			System.out.println(component2);
		}
	}

	@Test
	public void noReadObjectSpecified_WithComponentId_And_ObjectInputStream() throws Exception {
		final Class<?> enhancedClass = loader
				.loadClass(NoReadObjectSpecified_WithComponentId_And_ObjectInputStream.class.getName());
		when(injector.getComponentById("componentId1", TestComponent.class.getName(), 0)).thenReturn(component1);
		when(injector.getComponentById("componentId2", TestComponent.class.getName(), 1)).thenReturn(component2);

		// This should not throw an exception
		final Serializable obj = (Serializable) enhancedClass.newInstance();
		final ObjectInputStream in = mock(ObjectInputStream.class);
		getMethod(obj, "readObject", ObjectInputStream.class).invoke(obj, in);

		verify(in).defaultReadObject();
		final InOrder order = inOrder(injector);
		order.verify(injector).getComponentById("componentId1", TestComponent.class.getName(), 0);
		order.verify(injector).getComponentById("componentId2", TestComponent.class.getName(), 1);

		assertSame(component1, getFieldValue("component1", obj));
		assertSame(component2, getFieldValue("component2", obj));
	}

	@Recipient
	public static class NoReadObjectSpecified_WithComponentId_ThrowException implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final Exception exception = new Exception();

		@Inject
		public void injectServices(@Named("componentId1") final TestComponent pComponent1,
				@Named("componentId2") final TestComponent pComponent2) throws Exception {
			throw exception;
		}
	}

	@Test
	public void noReadObjectSpecified_WithComponentId_ThrowException() throws Exception {
		final Class<?> enhancedClass = loader
				.loadClass(NoReadObjectSpecified_WithComponentId_ThrowException.class.getName());
		when(injector.getComponentById("componentId1", TestComponent.class.getName(), 0)).thenReturn(component1);
		when(injector.getComponentById("componentId2", TestComponent.class.getName(), 1)).thenReturn(component2);

		// This should not throw an exception
		final Serializable obj = (Serializable) enhancedClass.newInstance();
		try {
			getMethod(obj, "readObject", ObjectInputStream.class).invoke(obj, mock(ObjectInputStream.class));
			fail("Exception expected here");
		} catch (final InvocationTargetException expected) {
			assertEquals(IOException.class, expected.getTargetException().getClass());
			assertSame(expected.getTargetException().getCause(), getFieldValue("exception", obj));
		}

		final InOrder order = inOrder(injector);
		order.verify(injector).getComponentById("componentId1", TestComponent.class.getName(), 0);
		order.verify(injector).getComponentById("componentId2", TestComponent.class.getName(), 1);
	}

	@Recipient
	public static class DoNotCallSuperMethod_A implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Inject
		public void inject(final ObjectInputStream in) throws ClassNotFoundException, IOException {
			in.defaultReadObject();
		}
	}

	@Recipient
	public static class DoNotCallSuperMethod_B extends DoNotCallSuperMethod_A {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	}

	@Recipient
	public static class DoNotCallSuperMethod_C extends DoNotCallSuperMethod_B {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Inject
		@Override
		public void inject(final ObjectInputStream in) throws ClassNotFoundException, IOException {
			super.inject(in);
			System.out.println("Do something");
		}
	}

	@Test
	public void doNotCallSuperMethod() throws Exception {
		try {
			loader.loadClass(DoNotCallSuperMethod_C.class.getName());
			fail("Exception expected");
		} catch (final SuperMethodInvokationException expected) {
			// expected
		}
	}

	public static ClassNotFoundException EXPECTED_CNF_EX = new ClassNotFoundException("");

	@Recipient
	public static class ClassNotFoundExceptionReThrown implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Inject
		public void inject(final TestComponent c) throws ClassNotFoundException, IOException {
			throw EXPECTED_CNF_EX;
		}
	}

	@Test
	public void classNotFoundExceptionReThrown() throws Exception {
		final Object obj = loader.loadClass(ClassNotFoundExceptionReThrown.class.getName()).newInstance();
		try {
			getMethod(obj, "readObject", ObjectInputStream.class).invoke(obj, mock(ObjectInputStream.class));
			fail("Exception expected");
		} catch (final InvocationTargetException expected) {
			assertSame(EXPECTED_CNF_EX, expected.getTargetException());
		}
	}

	public static ClassNotFoundException EXPECTED_IO_EX = new ClassNotFoundException("");

	@Recipient
	public static class IOExceptionReThrown implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Inject
		public void inject(final TestComponent c) throws ClassNotFoundException, IOException {
			throw EXPECTED_IO_EX;
		}
	}

	@Test
	public void ioExceptionReThrown() throws Exception {
		final Object obj = loader.loadClass(IOExceptionReThrown.class.getName()).newInstance();
		try {
			getMethod(obj, "readObject", ObjectInputStream.class).invoke(obj, mock(ObjectInputStream.class));
			fail("Exception expected");
		} catch (final InvocationTargetException expected) {
			assertSame(EXPECTED_IO_EX, expected.getTargetException());
		}
	}

	public static RuntimeException UNEXPECTED_EX = new RuntimeException();

	@Recipient
	public static class WrapUnexpectedExceptionIntoIOException implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Inject
		public void inject(final TestComponent c) throws ClassNotFoundException, IOException {
			throw UNEXPECTED_EX;
		}
	}

	@Test
	public void wrapUnexpectedExceptionIntoIOException() throws Exception {
		final Object obj = loader.loadClass(WrapUnexpectedExceptionIntoIOException.class.getName()).newInstance();
		try {
			getMethod(obj, "readObject", ObjectInputStream.class).invoke(obj, mock(ObjectInputStream.class));
			fail("Exception expected");
		} catch (final InvocationTargetException expected) {
			assertNotSame(UNEXPECTED_EX, expected.getTargetException());
			assertTrue(expected.getTargetException() instanceof IOException);
		}
	}
}
