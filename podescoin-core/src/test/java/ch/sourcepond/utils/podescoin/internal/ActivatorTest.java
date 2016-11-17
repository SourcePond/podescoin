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

import static ch.sourcepond.utils.podescoin.InjectorTest.readBytes;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.osgi.framework.hooks.weaving.WovenClass.TRANSFORMING;

import java.io.InputStream;
import java.io.ObjectInputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.hooks.weaving.WeavingException;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;
import org.osgi.framework.wiring.BundleWiring;

import ch.sourcepond.utils.podescoin.TestObject;

public class ActivatorTest {

	public static final class TestClassLoader extends ClassLoader {
		private final byte[] code;

		public TestClassLoader(final byte[] pCode) {
			super(null);
			code = pCode;
		}

		@Override
		protected Class<?> findClass(final String name) throws ClassNotFoundException {
			if (name.equals(TestObject.class.getName())) {
				return defineClass(name, code, 0, code.length);
			}

			throw new ClassNotFoundException();
		}
	}

	private final Bundle wovenClassBundle = mock(Bundle.class);
	private final BundleWiring wiring = mock(BundleWiring.class);
	private final BundleContext context = mock(BundleContext.class);
	private final Bundle bundle = mock(Bundle.class);
	private final WovenClass wovenClass = mock(WovenClass.class);
	private final Activator activator = new Activator();
	private byte[] transformedCode;

	@Before
	public void setup() {
		when(context.getBundle()).thenReturn(bundle);
		when(wovenClass.getBundleWiring()).thenReturn(wiring);
		when(wiring.getBundle()).thenReturn(wovenClassBundle);
		when(wovenClass.getState()).thenReturn(TRANSFORMING);
		doAnswer(new Answer<byte[]>() {

			@Override
			public byte[] answer(final InvocationOnMock invocation) throws Throwable {
				transformedCode = invocation.getArgument(0);
				return null;
			}
		}).when(wovenClass).setBytes(Mockito.any());
	}

	@Test
	public void start() throws Exception {
		activator.start(context);
		verify(context).registerService(WeavingHook.class, activator, null);
	}

	@Test
	public void stop() throws Exception {
		activator.stop(context);
		verifyZeroInteractions(context);
	}

	@Test
	public void verifyWeavingException_ThrowableOccurred() throws Exception {
		activator.start(context);
		final RuntimeException expected = new RuntimeException("any");
		doThrow(expected).when(wovenClass).getBytes();
		try {
			activator.weave(wovenClass);
			fail("Exception expected here");
		} catch (final WeavingException e) {
			assertSame(expected, e.getCause());
		}
	}

	@Test
	public void verifyWeave() throws Exception {
		activator.start(context);
		try (final InputStream in = getClass()
				.getResourceAsStream("/" + TestObject.class.getName().replace('.', '/') + ".class")) {
			final byte[] code = readBytes(in);
			when(wovenClass.getBytes()).thenReturn(code);
		}
		activator.weave(wovenClass);

		final TestClassLoader loader = new TestClassLoader(transformedCode);
		final Class<?> cl = loader.loadClass(TestObject.class.getName());

		// This method should be present
		cl.getDeclaredMethod("readObject", ObjectInputStream.class);
	}
}
