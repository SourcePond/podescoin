package ch.sourcepond.utils.bci.internal;

import static ch.sourcepond.utils.bci.InjectorTest.readBytes;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.osgi.framework.hooks.weaving.WovenClass.TRANSFORMING;

import java.io.InputStream;
import java.io.ObjectInputStream;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;

import ch.sourcepond.utils.bci.TestObject;
import ch.sourcepond.utils.bci.internal.Activator;

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

	private final BundleContext context = mock(BundleContext.class);
	private final WovenClass wovenClass = mock(WovenClass.class);
	private final Activator activator = new Activator();
	private byte[] transformedCode;

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
	public void verifyWeave() throws Exception {
		when(wovenClass.getState()).thenReturn(TRANSFORMING);
		doAnswer(new Answer<byte[]>() {

			@Override
			public byte[] answer(final InvocationOnMock invocation) throws Throwable {
				transformedCode = invocation.getArgumentAt(0, byte[].class);
				return null;
			}
		}).when(wovenClass).setBytes(Mockito.any());
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
