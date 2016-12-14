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
package ch.sourcepond.utils.podescoin.internal.util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Map.Entry;

import ch.sourcepond.utils.podescoin.Recipient;
import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class PodesCoinObjectOutputStream extends ObjectOutputStream {
	private static final Unsafe UNSAFE;
	static {
		try {
			final Field singleoneInstanceField = Unsafe.class.getDeclaredField("theUnsafe");
			singleoneInstanceField.setAccessible(true);
			UNSAFE = (Unsafe) singleoneInstanceField.get(null);
		} catch (final Exception e) {
			throw new Error(e.getMessage(), e);
		}
	}
	private final PodesCoinClassLoader loader;

	public PodesCoinObjectOutputStream(final PodesCoinClassLoader pLoader, final OutputStream out) throws IOException {
		super(out);
		loader = pLoader;
		enableReplaceObject(true);
	}

	private Field setAccessible(final Field pField) {
		pField.setAccessible(true);
		return pField;
	}

	private Object cloneObject(final Object obj) throws IOException {
		if (obj != null && obj.getClass().isAnnotationPresent(Recipient.class)) {
			try {
				final Class<?> targetType = loader.loadClass(obj.getClass().getName());
				final Object clone = UNSAFE.allocateInstance(targetType);

				for (final Entry<Field, Field> entry : new FieldCollector(obj.getClass(), targetType).entrySet()) {
					final Field sourceField = setAccessible(entry.getKey());
					final Field targetField = setAccessible(entry.getValue());
					targetField.set(clone, cloneObject(sourceField.get(obj)));
				}

				return clone;
			} catch (final ClassNotFoundException | NoSuchFieldException | SecurityException | InstantiationException
					| IllegalAccessException e) {
				throw new IOException(e.getMessage(), e);
			}
		}
		return obj;
	}

	@Override
	protected Object replaceObject(final Object obj) throws IOException {
		return cloneObject(obj);
	}
}
