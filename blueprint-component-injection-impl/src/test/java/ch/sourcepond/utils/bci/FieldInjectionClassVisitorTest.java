package ch.sourcepond.utils.bci;

import static ch.sourcepond.utils.bci.FieldInjectionClassVisitor.INJECT_BLUEPRINT_COMPONENTS_METHOD_NAME;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.objectweb.asm.ClassReader.SKIP_DEBUG;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;

public class FieldInjectionClassVisitorTest {

	public static class TestClassLoader extends ClassLoader implements BundleReference {
		private final FieldInjectionClassVisitor visitor;
		private final ClassWriter writer;
		private final Class<?> testSerializableClass;
		private final Bundle bundle;
		private Class<?> cl;

		public TestClassLoader(final FieldInjectionClassVisitor pVisitor, final ClassWriter pWriter,
				final Class<?> pTestSerializableClass, final Bundle pBundle) {
			super(null);
			visitor = pVisitor;
			writer = pWriter;
			testSerializableClass = pTestSerializableClass;
			bundle = pBundle;
		}

		@Override
		protected Class<?> findClass(final String name) throws ClassNotFoundException {
			if (name.equals(testSerializableClass.getName())) {
				if (cl == null) {
					try (final InputStream in = getClass()
							.getResourceAsStream("/" + name.replace('.', '/') + ".class")) {
						final ClassReader reader = new ClassReader(in);
						reader.accept(visitor, SKIP_DEBUG);
						final byte[] classData = writer.toByteArray();
						cl = defineClass(name, classData, 0, classData.length);
					} catch (final IOException e) {
						throw new ClassNotFoundException(e.getMessage(), e);
					}
				}
				return cl;
			}
			return FieldInjectionClassVisitorTest.class.getClassLoader().loadClass(name);
		}

		@Override
		public Bundle getBundle() {
			return bundle;
		}
	}

	private final ClassWriter writer = new ClassWriter(0);
	@Mock
	private BundleInjector injector;

	@Mock
	private BundleInjectorFactory factory;

	@Mock
	private Bundle bundle;

	@Mock
	private BundleContext context;

	private TestClassLoader loader;
	private FieldInjectionClassVisitor visitor;

	@Before
	public void setup() {
		initMocks(this);
		when(factory.newInjector(bundle)).thenReturn(injector);
		when(bundle.getBundleContext()).thenReturn(context);
		Injector.factory = factory;
		visitor = new FieldInjectionClassVisitor(writer);
	}

	@After
	public void tearDown() {
		Injector.injectors.clear();
	}

	private void setComponent(final Object pObj, final String pFieldName, final TestComponent pComponent)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		final Field field = pObj.getClass().getDeclaredField(pFieldName);
		field.setAccessible(true);
		try {
			field.set(pObj, pComponent);
		} finally {
			field.setAccessible(false);
		}
	}

	private TestComponent getComponent(final Object pObj, final String pFieldName)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		final Field field = pObj.getClass().getDeclaredField(pFieldName);
		field.setAccessible(true);
		return (TestComponent) field.get(pObj);
	}

	private Method getMethod(final Object pObj, final String pName, final Class<?>... pArgumentTypes)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		final Method method = pObj.getClass().getDeclaredMethod(pName, pArgumentTypes);
		method.setAccessible(true);
		return method;
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
