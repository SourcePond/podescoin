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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.junit.Test;

import ch.sourcepond.utils.podescoin.ClassVisitorTest;
import ch.sourcepond.utils.podescoin.TestComponent;
import ch.sourcepond.utils.podescoin.api.Component;
import ch.sourcepond.utils.podescoin.api.ReadObject;

/**
 *
 */
public class DefaultReadObjectTest extends ClassVisitorTest {
	private final ObjectInputStream objInStream = mock(ObjectInputStream.class);

	private void callReadObject(final Class<?> pOriginalClass) throws Exception {
		final Class<?> enhancedClass = loader.loadClass(pOriginalClass.getName());
		final Serializable obj = (Serializable) enhancedClass.newInstance();
		getMethod(obj, "readObject", ObjectInputStream.class).invoke(obj, objInStream);
	}

	public static class InjectorMethodWithCustomReadObject implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@ReadObject
		void readObject(final ObjectInputStream in, final TestComponent component) {

		}

		private void readObject(final ObjectInputStream in) throws ClassNotFoundException, IOException {

		}
	}

	@Test
	public void injectorMethodWithCustomReadObject() throws Exception {
		callReadObject(InjectorMethodWithCustomReadObject.class);

		// defaultReadObject should not have been called.
		verify(objInStream, never()).defaultReadObject();
	}

	public static class InjectorMethod implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@ReadObject
		void readObject(final ObjectInputStream in, final TestComponent component) {

		}

	}

	@Test
	public void injectorMethod() throws Exception {
		callReadObject(InjectorMethod.class);

		// defaultReadObject should have been called.
		verify(objInStream).defaultReadObject();
	}

	public static class FieldInjectionWithCustomReadObject implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Component
		public transient TestComponent component;

		private void readObject(final ObjectInputStream in) throws ClassNotFoundException, IOException {

		}
	}

	@Test
	public void fieldInjectionWithCustomReadObject() throws Exception {
		callReadObject(FieldInjectionWithCustomReadObject.class);

		// defaultReadObject should not have been called.
		verify(objInStream, never()).defaultReadObject();
	}

	public static class FieldInjection implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Component
		public transient TestComponent component;

	}

	@Test
	public void fieldInjection() throws Exception {
		callReadObject(FieldInjection.class);

		// defaultReadObject should have been called.
		verify(objInStream).defaultReadObject();
	}

	public static class ClassWithFieldsAndInjectionMethod implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Component
		transient TestComponent component1;

		TestComponent component2;

		@ReadObject
		void readObject(final ObjectInputStream in, final TestComponent pComponent2)
				throws ClassNotFoundException, IOException {
			component2 = pComponent2;
		}
	}

	@Test
	public void classWithFieldsAndInjectionMethod() throws Exception {
		callReadObject(ClassWithFieldsAndInjectionMethod.class);

		// defaultReadObject should have been called.
		verify(objInStream).defaultReadObject();
	}

	public static class ClassWithFieldsAndInjectionMethodWithCustomReadObject implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Component
		transient TestComponent component1;

		TestComponent component2;

		@ReadObject
		void readObject(final ObjectInputStream in, final TestComponent pComponent2)
				throws ClassNotFoundException, IOException {
			component2 = pComponent2;
		}

		private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {

		}
	}

	@Test
	public void classWithFieldsAndInjectionMethodWithCustomReadObject() throws Exception {
		callReadObject(ClassWithFieldsAndInjectionMethodWithCustomReadObject.class);

		// defaultReadObject should not have been called.
		verify(objInStream, never()).defaultReadObject();
	}

}
