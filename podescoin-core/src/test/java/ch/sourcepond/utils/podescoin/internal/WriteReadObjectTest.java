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
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.junit.Test;

import ch.sourcepond.utils.podescoin.ClassVisitorTest;
import ch.sourcepond.utils.podescoin.TestComponent;
import ch.sourcepond.utils.podescoin.api.Recipient;
import ch.sourcepond.utils.podescoin.api.WriteObject;

/**
 *
 */
public class WriteReadObjectTest extends ClassVisitorTest {
	private final ObjectOutputStream objOutStream = mock(ObjectOutputStream.class);

	private void callWriteObject(final Class<?> pOriginalClass) throws Exception {
		final Class<?> enhancedClass = loader.loadClass(pOriginalClass.getName());
		final Serializable obj = (Serializable) enhancedClass.newInstance();
		getMethod(obj, "writeObject", ObjectOutputStream.class).invoke(obj, objOutStream);
	}

	@Recipient
	public static class InjectorMethodWithCustomWriteObject implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@WriteObject
		void writeObject(final ObjectOutputStream out, final TestComponent component) {

		}

		private void writeObject(final ObjectOutputStream out) throws IOException {

		}
	}

	@Test
	public void injectorMethodWithCustomReadObject() throws Exception {
		callWriteObject(InjectorMethodWithCustomWriteObject.class);

		// defaultReadObject should not have been called.
		verify(objOutStream, never()).defaultWriteObject();
	}

	@Recipient
	public static class InjectorMethod implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@WriteObject
		void writeObject(final ObjectOutputStream out, final TestComponent component) {

		}

	}

	@Test
	public void injectorMethod() throws Exception {
		callWriteObject(InjectorMethod.class);

		// defaultReadObject should have been called.
		verify(objOutStream).defaultWriteObject();
	}
}
