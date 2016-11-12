package ch.sourcepond.utils.podescoin;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import ch.sourcepond.utils.podescoin.internal.Activator;

final class EnhancedClassLoader extends ClassLoader {
	private final Map<String, Class<?>> enhancedClasses = new HashMap<>();

	EnhancedClassLoader(final ClassLoader pParent) {
		super(pParent);
	}

	@Override
	protected Class<?> findClass(final String name) throws ClassNotFoundException {
		Class<?> cl = enhancedClasses.get(name);
		if (cl == null) {
			cl = super.findClass(name);
		}
		return cl;
	}

	public synchronized void register(final Class<? extends Serializable> pClass) throws ClassNotFoundException {
		if (!enhancedClasses.containsKey(pClass.getName())) {
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			try (final InputStream in = new BufferedInputStream(
					getClass().getResourceAsStream("/" + pClass.getName().replace('.', '/') + ".class"))) {
				final byte[] buffer = new byte[1024];
				int read = in.read(buffer);
				while (read != -1) {
					out.write(buffer, 0, read);
					read = in.read(buffer);
				}
			} catch (final IOException e) {
				throw new ClassNotFoundException(e.getMessage(), e);
			}
			final byte[] enhancedClassData = Activator.transform(out.toByteArray());
			final Class<?> cl = defineClass(pClass.getName(), enhancedClassData, 0, enhancedClassData.length,
					getClass().getProtectionDomain());
			enhancedClasses.put(pClass.getName(), cl);
		}
	}
}
