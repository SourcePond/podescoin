package ch.sourcepond.utils.podescoin;

import static ch.sourcepond.utils.podescoin.BundleInjectorTest.COMPONENT_ID;
import static ch.sourcepond.utils.podescoin.BundleInjectorTest.FIELD_NAME;
import static ch.sourcepond.utils.podescoin.Injector.injectComponents;
import static java.lang.Thread.sleep;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.concurrent.ExecutorService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;

import ch.sourcepond.utils.podescoin.BundleInjectorFactory;
import ch.sourcepond.utils.podescoin.Injector;
import ch.sourcepond.utils.podescoin.internal.BundleInjector;

public class InjectorTest {

	public static class InitComponents implements Runnable {
		private final Serializable deserializedObject;
		private final String[][] componentToFields;

		public InitComponents(final Serializable pDeserializedObject, final String[][] pComponentToFields) {
			deserializedObject = pDeserializedObject;
			componentToFields = pComponentToFields;
		}

		@Override
		public void run() {
			Injector.injectComponents(deserializedObject, componentToFields);
		}

	}

	public static class TimedAnswer implements Answer<BundleInjector> {
		private final BundleInjector injector;
		private final long timeout;

		public TimedAnswer(final BundleInjector injector, final long timeout) {
			this.injector = injector;
			this.timeout = timeout;
		}

		@Override
		public BundleInjector answer(final InvocationOnMock invocation) throws Throwable {
			sleep(timeout);
			return injector;
		}

	}

	public static byte[] readBytes(final InputStream in) throws IOException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream(64);
		final byte[] buffer = new byte[64];
		int read = 0;

		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}

		return out.toByteArray();
	}

	public static class TestClassLoader extends ClassLoader implements BundleReference {
		private Class<?> cl;
		private Bundle bundle;

		public TestClassLoader(final Bundle pBundle) {
			super(null);
			setBundle(pBundle);
		}

		public void setBundle(final Bundle pBundle) {
			bundle = pBundle;
		}

		@Override
		protected Class<?> findClass(final String name) throws ClassNotFoundException {
			if (cl == null) {
				try (final InputStream in = InjectorTest.class
						.getResourceAsStream("/" + name.replace('.', '/') + ".class")) {
					final byte[] classData = readBytes(in);
					cl = defineClass(name, classData, 0, classData.length);
				} catch (final IOException e) {
					throw new ClassNotFoundException(e.getMessage(), e);
				}
			}
			return cl;
		}

		@Override
		public Bundle getBundle() {
			return bundle;
		}
	}

	private TestClassLoader loader;

	@Mock
	private Bundle bundle;

	@Mock
	private BundleContext context;

	@Mock
	private BundleInjectorFactory factory;

	@Mock
	private BundleInjector injector;

	private Serializable obj;
	private String[][] arguments;

	@Before
	public void setup() throws Exception {
		initMocks(this);
		loader = new TestClassLoader(bundle);
		Injector.factory = factory;

		final Class<?> cl = loader.loadClass(TestObject.class.getName());
		obj = (Serializable) cl.newInstance();
		arguments = new String[][] { { FIELD_NAME, COMPONENT_ID, obj.getClass().getName() } };
		when(bundle.getBundleContext()).thenReturn(context);
		when(factory.newInjector(bundle)).thenReturn(injector);
	}

	@After
	public void tearDown() {
		Injector.injectors.clear();
	}

	@Test
	public void verifySameInjectorInstanceForAnyThread() throws Exception {
		final BundleInjector secondInjector = mock(BundleInjector.class);
		when(factory.newInjector(bundle)).thenAnswer(new TimedAnswer(injector, 100))
				.thenAnswer(new TimedAnswer(secondInjector, 200));

		final ExecutorService executor = newFixedThreadPool(2);
		try {
			executor.execute(new InitComponents(obj, arguments));
			executor.execute(new InitComponents(obj, arguments));
		} finally {
			executor.shutdown();
			executor.awaitTermination(5, SECONDS);
		}

		verify(factory, times(2)).newInjector(bundle);
		verify(context).addServiceListener(injector);

	}

	@Test(expected = NullPointerException.class)
	public void initComponent_DeserializedObject() {
		injectComponents(null, arguments);
	}

	@Test(expected = NullPointerException.class)
	public void initComponent_ArrayIsNull() {
		injectComponents(obj, null);
	}

	@Test
	public void initComponents_ArrayIsEmpty() throws Exception {
		injectComponents(obj, new String[0][0]);
		verifyZeroInteractions(injector);
	}

	@Test(expected = IllegalStateException.class)
	public void initComponents_NoBundleFound() throws Exception {
		loader.setBundle(null);
		injectComponents(obj, arguments);
	}

	@Test(expected = IllegalArgumentException.class)
	public void initComponents_ExceptionInInitMethod() throws Exception {
		final NoSuchFieldException expected = new NoSuchFieldException(FIELD_NAME);
		doThrow(expected).when(injector).initDeserializedObject(obj, arguments);
		injectComponents(obj, arguments);
		verify(injector).initDeserializedObject(obj, arguments);
	}

	@Test
	public void initComponents() throws Exception {
		injectComponents(obj, arguments);
		verify(injector).initDeserializedObject(obj, arguments);
	}

	@Test
	public void verifyInjectorPerBundle() throws Exception {
		injectComponents(obj, arguments);
		injectComponents(obj, arguments);

		// Should be called exactly once
		verify(factory).newInjector(bundle);
		verify(context).addServiceListener(injector);
	}
}
