package ch.sourcepond.utils.podescoin.devel;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import ch.sourcepond.utils.podescoin.Injector;

public class WriteInjectFieldsClass implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private void writeObject(final ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		Injector.injectComponents(this, new String[][] { { "test1", "test2", "test3" }, { "test1", "test2", "test3" },
				{ "test1", "test2", "test3" }, { "test1", "test2", "test3" } });

	}
}
