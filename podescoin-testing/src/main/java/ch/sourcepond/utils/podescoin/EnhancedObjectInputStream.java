package ch.sourcepond.utils.podescoin;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

public class EnhancedObjectInputStream extends ObjectInputStream {
	private final EnhancedClassLoader loader;

	public EnhancedObjectInputStream(final EnhancedClassLoader pLoader, final InputStream in) throws IOException {
		super(in);
		loader = pLoader;
	}

	@Override
	protected Class<?> resolveClass(final ObjectStreamClass desc) throws IOException, ClassNotFoundException {
		return loader.loadClass(desc.getName());
	}

}
