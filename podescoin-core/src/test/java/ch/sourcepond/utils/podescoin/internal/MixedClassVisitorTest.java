package ch.sourcepond.utils.podescoin.internal;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import ch.sourcepond.utils.podescoin.ClassVisitorTest;
import ch.sourcepond.utils.podescoin.TestComponent;
import ch.sourcepond.utils.podescoin.api.Component;
import ch.sourcepond.utils.podescoin.api.ReadObject;
import ch.sourcepond.utils.podescoin.internal.ReadObjectFieldInjectionClassVisitorTest.VerifyPushByteConstantReadObjectAlreadyDefined;

public class MixedClassVisitorTest extends ClassVisitorTest {

	public static class ClassWithFieldsAndInjectionMethod implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Component
		transient TestComponent component1;

		TestComponent component2;

		@ReadObject
		void inject(final ObjectInputStream in, final TestComponent pComponent2)
				throws ClassNotFoundException, IOException {
			in.defaultReadObject();
			component2 = pComponent2;
		}
	}

	@Test
	public void verifyInjectionDoneInOrder() throws Exception {
		final Class<?> enhancedClass = loader.loadClass(VerifyPushByteConstantReadObjectAlreadyDefined.class.getName());

		final Serializable obj = (Serializable) enhancedClass.newInstance();
		final ObjectInputStream objInStream = mock(ObjectInputStream.class);
		getMethod(obj, "readObject", ObjectInputStream.class).invoke(obj, objInStream);

		final InOrder order = inOrder(injector, objInStream);

		order.verify(injector).initDeserializedObject(Mockito.same(obj),
				Mockito.eq(new String[][] { { "component1", "componentId1", TestComponent.class.getName() } }));
		order.verify(objInStream).defaultReadObject();
	}
}
