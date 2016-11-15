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

import javax.inject.Inject;

import org.junit.Test;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import ch.sourcepond.utils.podescoin.ClassVisitorTest;
import ch.sourcepond.utils.podescoin.TestComponent;

public class ReadObjectCallOrderTest extends ClassVisitorTest {
	public static final String PARENT = "parent";
	public static final String CHILD = "child";
	public static List<String> readObjectCalls = new LinkedList<>();
	public static List<String> injectCalls = new ArrayList<>(2);

	public static class Parent implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Inject
		void doInject(TestComponent pComponent) {
			injectCalls.add(PARENT);
		}

		private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
			readObjectCalls.add(PARENT);
		}
	}

	public static class Child extends Parent {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Inject
		void doInject(TestComponent pComponent) {
			injectCalls.add(CHILD);
		}

		private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
			readObjectCalls.add(CHILD);
		}
	}

	@Override
	protected ClassVisitor newVisitor() {
		return new MethodInjectionClassVisitor(new ClassWriter(ClassWriter.COMPUTE_MAXS), new InspectForInjectorMethodClassVisitor(null));
	}

	@Test
	public void verifyCallOrderWhenChildExtendsParent() throws Exception {
		loader = new MethodInjectorTestClassLoader(visitor, new ClassWriter(ClassWriter.COMPUTE_MAXS), Child.class, bundle);
		final Object testObject = loader.loadClass(Child.class.getName()).newInstance();
		
		final TestComponent component = mock(TestComponent.class);
		when(injector.getComponentByTypeName(TestComponent.class.getName(), 0)).thenReturn(component);
		

		final ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try (final ObjectOutputStream out = new ObjectOutputStream(bout)) {
			out.writeObject(testObject);
		}

		try (final ObjectInputStream in = new EnhancedClassAwareObjectInputStream(loader, new ByteArrayInputStream(bout.toByteArray()))) {
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
