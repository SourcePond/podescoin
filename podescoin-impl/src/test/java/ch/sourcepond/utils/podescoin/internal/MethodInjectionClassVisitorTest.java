package ch.sourcepond.utils.podescoin.internal;

import static ch.sourcepond.utils.podescoin.internal.SerializableClassVisitor.INJECT_BLUEPRINT_COMPONENTS_METHOD_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.objectweb.asm.ClassVisitor;

import ch.sourcepond.utils.podescoin.ClassVisitorTest;
import ch.sourcepond.utils.podescoin.TestComponent;
import ch.sourcepond.utils.podescoin.internal.InspectForInjectorMethodClassVisitor;
import ch.sourcepond.utils.podescoin.internal.MethodInjectionClassVisitor;

public class MethodInjectionClassVisitorTest extends ClassVisitorTest {

	@Mock
	private TestComponent component1;

	@Mock
	private TestComponent component2;

	@Override
	protected ClassVisitor newVisitor() {
		return new MethodInjectionClassVisitor(writer, new InspectForInjectorMethodClassVisitor(visitor));
	}

	private static Object getFieldValue(final String pFieldName, final Object pObj) throws Exception {
		final Field f = pObj.getClass().getDeclaredField(pFieldName);
		f.setAccessible(true);
		try {
			return f.get(pObj);
		} finally {
			f.setAccessible(false);
		}
	}

	public static class NoReadObjectSpecified_WithType implements Serializable {
		private TestComponent component1;

		@Inject
		public void injectServices(final TestComponent pComponent1) {
			component1 = pComponent1;
		}
	}

	@Test
	public void noReadObjectSpecified_WithType() throws Exception {
		loader = new MethodInjectorTestClassLoader(visitor, writer, NoReadObjectSpecified_WithType.class, bundle);
		final Class<?> enhancedClass = loader.loadClass(NoReadObjectSpecified_WithType.class.getName());

		try {
			// This method should NOT exist
			enhancedClass.getDeclaredMethod(INJECT_BLUEPRINT_COMPONENTS_METHOD_NAME);
			fail("Exception expected");
		} catch (final NoSuchMethodException expected) {
			// noop
		}

		when(injector.getComponentByTypeName(TestComponent.class.getName(), 0)).thenReturn(component1);

		// This should not throw an exception
		final Serializable obj = (Serializable) enhancedClass.newInstance();
		getMethod(obj, "readObject", ObjectInputStream.class).invoke(obj, mock(ObjectInputStream.class));

		verify(injector).getComponentByTypeName(TestComponent.class.getName(), 0);
		assertSame(component1, getFieldValue("component1", obj));
	}

	public static class NoReadObjectSpecified_WithComponentId implements Serializable {
		private TestComponent component1;
		private TestComponent component2;

		@Inject
		public void injectServices(@Named("componentId1") final TestComponent pComponent1,
				@Named("componentId2") final TestComponent pComponent2) {
			component1 = pComponent1;
			component2 = pComponent2;
		}
	}

	@Test
	public void noReadObjectSpecified_WithComponentId() throws Exception {
		loader = new MethodInjectorTestClassLoader(visitor, writer, NoReadObjectSpecified_WithComponentId.class,
				bundle);
		final Class<?> enhancedClass = loader.loadClass(NoReadObjectSpecified_WithComponentId.class.getName());

		try {
			// This method should NOT exist
			enhancedClass.getDeclaredMethod(INJECT_BLUEPRINT_COMPONENTS_METHOD_NAME);
			fail("Exception expected");
		} catch (final NoSuchMethodException expected) {
			// noop
		}

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

	public static class NoReadObjectSpecified_WithComponentId_ThrowException implements Serializable {
		private final Exception exception = new Exception();

		@Inject
		public void injectServices(@Named("componentId1") final TestComponent pComponent1,
				@Named("componentId2") final TestComponent pComponent2) throws Exception {
			throw exception;
		}
	}

	@Test
	public void noReadObjectSpecified_WithComponentId_ThrowException() throws Exception {
		loader = new MethodInjectorTestClassLoader(visitor, writer,
				NoReadObjectSpecified_WithComponentId_ThrowException.class, bundle);
		final Class<?> enhancedClass = loader
				.loadClass(NoReadObjectSpecified_WithComponentId_ThrowException.class.getName());

		try {
			// This method should NOT exist
			enhancedClass.getDeclaredMethod(INJECT_BLUEPRINT_COMPONENTS_METHOD_NAME);
			fail("Exception expected");
		} catch (final NoSuchMethodException expected) {
			// noop
		}

		when(injector.getComponentById("componentId1", TestComponent.class.getName(), 0)).thenReturn(component1);
		when(injector.getComponentById("componentId2", TestComponent.class.getName(), 1)).thenReturn(component2);

		// This should not throw an exception
		final Serializable obj = (Serializable) enhancedClass.newInstance();
		try {
			getMethod(obj, "readObject", ObjectInputStream.class).invoke(obj, mock(ObjectInputStream.class));
			fail("Exception expected here");
		} catch (final InvocationTargetException expected) {
			assertEquals(IllegalStateException.class, expected.getTargetException().getClass());
			assertSame(expected.getTargetException().getCause(), getFieldValue("exception", obj));
		}

		final InOrder order = inOrder(injector);
		order.verify(injector).getComponentById("componentId1", TestComponent.class.getName(), 0);
		order.verify(injector).getComponentById("componentId2", TestComponent.class.getName(), 1);
	}
}
