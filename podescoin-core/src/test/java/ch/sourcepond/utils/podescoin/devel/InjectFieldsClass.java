package ch.sourcepond.utils.podescoin.devel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import ch.sourcepond.utils.podescoin.Injector;

public class InjectFieldsClass implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		Injector.injectComponents(this, new String[][] { { "test1", "test2", "test3" }, { "test1", "test2", "test3" },
				{ "test1", "test2", "test3" }, { "test1", "test2", "test3" } });

	}
}
