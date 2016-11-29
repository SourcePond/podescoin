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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import ch.sourcepond.utils.podescoin.ClassVisitorTest;
import ch.sourcepond.utils.podescoin.TestComponent;
import ch.sourcepond.utils.podescoin.api.ReadObject;
import ch.sourcepond.utils.podescoin.api.Recipient;
import ch.sourcepond.utils.podescoin.internal.util.PodesCoinObjectInputStream;

public class ReadObjectCallOrderTest extends ClassVisitorTest {
	public static final String PARENT = "parent";
	public static final String CHILD = "child";
	public static List<String> readObjectCalls = new LinkedList<>();
	public static List<String> injectCalls = new ArrayList<>(2);

	@Recipient
	public static class Parent implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@ReadObject
		void readObject(final TestComponent pComponent) {
			injectCalls.add(PARENT);
		}

		private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
			readObjectCalls.add(PARENT);
		}
	}

	@Recipient
	public static class Child extends Parent {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		@ReadObject
		void readObject(final TestComponent pComponent) {
			injectCalls.add(CHILD);
		}

		private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
			readObjectCalls.add(CHILD);
		}
	}

	@Test
	public void verifyCallOrderWhenChildExtendsParent() throws Exception {
		final Object testObject = loader.loadClass(Child.class.getName()).newInstance();

		final TestComponent component = mock(TestComponent.class);
		when(injector.getComponentByTypeName(TestComponent.class.getName(), 0)).thenReturn(component);

		final ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try (final ObjectOutputStream out = new ObjectOutputStream(bout)) {
			out.writeObject(testObject);
		}

		try (final ObjectInputStream in = new PodesCoinObjectInputStream(loader,
				new ByteArrayInputStream(bout.toByteArray()))) {
			in.readObject();
		}

		assertEquals(2, readObjectCalls.size());
		assertEquals(PARENT, readObjectCalls.get(0));
		assertEquals(CHILD, readObjectCalls.get(1));

		assertEquals(2, injectCalls.size());
		assertEquals(PARENT, injectCalls.get(0));
		assertEquals(CHILD, injectCalls.get(1));
	}
}
