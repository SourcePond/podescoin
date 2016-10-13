package ch.sourcepond.utils.bci.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import ch.sourcepond.utils.bci.Container;
import ch.sourcepond.utils.bci.Injector;
import ch.sourcepond.utils.bci.TestComponent;

public class TestClass implements Serializable {

	public void initObject(final TestComponent pComponent, final TestComponent pComponent1,
			final TestComponent pComponent2, final TestComponent pComponent3, final TestComponent pComponent4,
			final TestComponent pComponent5, final TestComponent pComponent6, final TestComponent pComponent7,
			final TestComponent pComponent8, final TestComponent pComponent9) throws Exception {

	}

	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
		final Container injector = Injector.getContainer(this);
		try {
			initObject(injector.getComponentById("componentId1", "ch.sourcepond.utils.bci.TestComponent"),
					injector.getComponentById("componentId2", "ch.sourcepond.utils.bci.TestComponent"),
					injector.getComponentByTypeName("ch.sourcepond.utils.bci.TestComponent"),
					injector.getComponentByTypeName("ch.sourcepond.utils.bci.TestComponent"),
					injector.getComponentByTypeName("ch.sourcepond.utils.bci.TestComponent"),
					injector.getComponentById("componentId3", "ch.sourcepond.utils.bci.TestComponent"),
					injector.getComponentById("componentId4", "ch.sourcepond.utils.bci.TestComponent"),
					injector.getComponentById("componentId5", "ch.sourcepond.utils.bci.TestComponent"),
					injector.getComponentByTypeName("ch.sourcepond.utils.bci.TestComponent"),
					injector.getComponentById("componentId6", "ch.sourcepond.utils.bci.TestComponent"));
		} catch (final Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
}
