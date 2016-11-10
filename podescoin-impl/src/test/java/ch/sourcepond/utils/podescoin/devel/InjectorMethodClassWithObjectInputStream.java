package ch.sourcepond.utils.podescoin.devel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import ch.sourcepond.utils.podescoin.Container;
import ch.sourcepond.utils.podescoin.Injector;
import ch.sourcepond.utils.podescoin.TestComponent;

public class InjectorMethodClassWithObjectInputStream implements Serializable {

	public void initObject(final ObjectInputStream in, final TestComponent pComponent, final TestComponent pComponent1,
			final TestComponent pComponent2, final TestComponent pComponent3, final TestComponent pComponent4,
			final TestComponent pComponent5, final TestComponent pComponent6, final TestComponent pComponent7,
			final TestComponent pComponent8, final TestComponent pComponent9) throws Exception {

	}

	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
		final Container injector = Injector.getContainer(this);
		try {
			initObject(in, injector.getComponentById("componentId1", "ch.sourcepond.utils.bci.TestComponent", 0),
					injector.getComponentById("componentId2", "ch.sourcepond.utils.bci.TestComponent", 1),
					injector.getComponentByTypeName("ch.sourcepond.utils.bci.TestComponent", 2),
					injector.getComponentByTypeName("ch.sourcepond.utils.bci.TestComponent", 3),
					injector.getComponentByTypeName("ch.sourcepond.utils.bci.TestComponent", 4),
					injector.getComponentById("componentId3", "ch.sourcepond.utils.bci.TestComponent", 5),
					injector.getComponentById("componentId4", "ch.sourcepond.utils.bci.TestComponent", 6),
					injector.getComponentById("componentId5", "ch.sourcepond.utils.bci.TestComponent", 7),
					injector.getComponentByTypeName("ch.sourcepond.utils.bci.TestComponent", 8),
					injector.getComponentById("componentId6", "ch.sourcepond.utils.bci.TestComponent", 9));
		} catch (final Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
}