package ch.sourcepond.utils.podescoin.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import ch.sourcepond.utils.podescoin.TestClassLoader;;

public class EnhancedClassAwareObjectInputStream extends ObjectInputStream {
	private final TestClassLoader loader;

	public EnhancedClassAwareObjectInputStream(final TestClassLoader pLoader, final  InputStream in) throws IOException {
		super(in);
		loader = pLoader;
	}
	

	@Override
	protected Class<?> resolveClass(final ObjectStreamClass desc) throws IOException, ClassNotFoundException {
		if (loader.isEnhanced(desc.getName())) {
			return loader.loadClass(desc.getName());
		}
		return super.resolveClass(desc);
	}

}
