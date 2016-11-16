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
package ch.sourcepond.utils.podescoin;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Named;

public class CloneContextFactory {

	public static CloneContext newContext() {
		return new CloneContext();
	}

	private static List<Field> collectFields(final Class<?> pClassOrNull, final List<Field> pFields) {
		if (pClassOrNull != null) {
			for (final Field f : pClassOrNull.getDeclaredFields()) {
				if (f.isAnnotationPresent(Named.class)) {
					f.setAccessible(true);
					pFields.add(f);
				}
			}
		}
		return pFields;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static CloneContext newContext(final Object pTest) {
		final CloneContext context = new CloneContext();
		for (final Field f : collectFields(pTest.getClass(), new LinkedList<>())) {
			final Named component = f.getAnnotation(Named.class);
			try {
				context.addComponent(f.get(pTest), component.value(), (Class) f.getType());
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		return context;
	}
}
