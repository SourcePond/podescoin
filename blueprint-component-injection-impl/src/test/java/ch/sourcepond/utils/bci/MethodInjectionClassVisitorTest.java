package ch.sourcepond.utils.bci;

import static ch.sourcepond.utils.bci.SerializableClassVisitor.INJECT_BLUEPRINT_COMPONENTS_METHOD_NAME;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Field;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.objectweb.asm.ClassVisitor;

public class MethodInjectionClassVisitorTest extends ClassVisitorTest {

	@Mock
	private TestComponent component1;

	@Mock
	private TestComponent component2;

	@Override
	protected ClassVisitor newVisitor() {
		return new MethodInjectionClassVisitor(writer, new InspectForInjectorMethodClassVisitor(visitor));
	}

	public static class NoReadObjectSpecified implements Serializable {
		public TestComponent component1;
		public TestComponent component2;

		@Inject
		public void injectServices(@Named("componentId1") final TestComponent pComponent1,
				@Named("componentId2") final TestComponent pComponent2) {
			component1 = pComponent1;
			component2 = pComponent2;
		}
	}

	private Object getFieldValue(final String pFieldName, final Object pObj) throws Exception {
		final Field f = pObj.getClass().getDeclaredField(pFieldName);
		f.setAccessible(true);
		try {
			return f.get(pObj);
		} finally {
			f.setAccessible(false);
		}
	}

	@Test
	public void noReadObjectSpecified_WithComponentId() throws Exception {
		loader = new MethodInjectorTestClassLoader(visitor, writer, NoReadObjectSpecified.class, bundle);
		final Class<?> enhancedClass = loader.loadClass(NoReadObjectSpecified.class.getName());

		try {
			// This method should NOT exist
			enhancedClass.getDeclaredMethod(INJECT_BLUEPRINT_COMPONENTS_METHOD_NAME);
			fail("Exception expected");
		} catch (final NoSuchMethodException expected) {
			// noop
		}

		when(injector.getComponentById("componentId1")).thenReturn(component1);
		when(injector.getComponentById("componentId2")).thenReturn(component2);

		// This should not throw an exception
		final Serializable obj = (Serializable) enhancedClass.newInstance();
		getMethod(obj, "readObject", ObjectInputStream.class).invoke(obj, mock(ObjectInputStream.class));

		final InOrder order = inOrder(injector);
		order.verify(injector).getComponentById("componentId1");
		order.verify(injector).getComponentById("componentId2");

		assertSame(component1, getFieldValue("component1", obj));
		assertSame(component2, getFieldValue("component2", obj));
	}
}
