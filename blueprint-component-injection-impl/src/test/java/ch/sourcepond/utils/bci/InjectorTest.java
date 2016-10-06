package ch.sourcepond.utils.bci;

import static ch.sourcepond.utils.bci.BundleInjectorTest.COMPONENT_ID;
import static ch.sourcepond.utils.bci.BundleInjectorTest.FIELD_NAME;
import static ch.sourcepond.utils.bci.Injector.injectComponents;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;

public class InjectorTest {

	public class TestClassLoader extends ClassLoader implements BundleReference {
		private Class<?> cl;

		public TestClassLoader() {
			super(null);
		}

		@Override
		protected Class<?> findClass(final String name) throws ClassNotFoundException {
			if (cl == null) {
				try (final InputStream in = InjectorTest.class
						.getResourceAsStream("/" + name.replace('.', '/') + ".class")) {
					final ByteArrayOutputStream out = new ByteArrayOutputStream(64);
					final byte[] buffer = new byte[64];
					int read = 0;

					while ((read = in.read(buffer)) != -1) {
						out.write(buffer, 0, read);
					}

					final byte[] classData = out.toByteArray();
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

	private final TestClassLoader loader = new TestClassLoader();

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
		Injector.factory = factory;

		final Class<?> cl = loader.loadClass(TestObject.class.getName());
		obj = (Serializable) cl.newInstance();
		arguments = new String[][] { { FIELD_NAME, COMPONENT_ID, obj.getClass().getName() } };
		when(bundle.getBundleContext()).thenReturn(context);
		when(factory.newInjector(bundle)).thenReturn(injector);
	}

	@Test
	public void initComponents() throws Exception {
		injectComponents(obj, arguments);
		verify(injector).initDeserializedObject(obj, arguments);
	}

}
