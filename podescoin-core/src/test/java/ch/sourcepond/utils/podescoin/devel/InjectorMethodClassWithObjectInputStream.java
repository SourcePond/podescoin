/*Copyright (C) 2016 Roland Hauser, <sourcepond@gmail.com>
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package ch.sourcepond.utils.podescoin.devel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import ch.sourcepond.utils.podescoin.Container;
import ch.sourcepond.utils.podescoin.Injector;
import ch.sourcepond.utils.podescoin.TestComponent;

public class InjectorMethodClassWithObjectInputStream implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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