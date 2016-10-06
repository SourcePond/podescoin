package ch.sourcepond.utils.bci.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import ch.sourcepond.utils.bci.Injector;

public class TestClass implements Serializable {

	public void injectBlueprintComponents() {

		Injector.injectComponents(this,
				new String[][] { { "field1", "componentId1", "com.some.Type1" },
						{ "field2", "componentId2", "com.some.Type2" }, { "field3", "componentId3", "com.some.Type3" },
						{ "field4", "componentId4", "com.some.Type4" }, { "field5", "componentId5", "com.some.Type5" },
						{ "field6", "componentId6", "com.some.Type6" }, { "field7", "componentId7", "com.some.Type7" },
						{ "field8", "componentId8", "com.some.Type8" }, { "field9", "componentId9", "com.some.Type9" },
						{ "field10", "componentId10", "com.some.Type10" },
						{ "field11", "componentId11", "com.some.Type11" }, { "field12", null, "com.some.Type12" } });
	}

	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
		injectBlueprintComponents();
	}
}
