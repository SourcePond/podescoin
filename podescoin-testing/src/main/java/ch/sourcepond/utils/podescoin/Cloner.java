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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
final class Cloner {
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

	private final Set<Object> visited = new HashSet<>();
	private final Function<Class<?>, Class<?>> classDetermination;
	private final Object source;
	private final EnhancedClassLoader loader;

	public Cloner(final EnhancedClassLoader pLoader, final Function<Class<?>, Class<?>> pClassDetermination,
			final Object pSource) {
		loader = pLoader;
		classDetermination = pClassDetermination;
		source = pSource;
	}

	private Map<Field, Field> getDeclaredFields(final Class<?> pSourceType, final Class<?> pTargetType, Map<Field, Field> pCollectedFields) throws NoSuchFieldException, SecurityException {
		if (pSourceType != null) {
			for (final Field f : pSourceType.getDeclaredFields()) {
				if (!Modifier.isStatic(f.getModifiers())) {
					pCollectedFields.put(f, pTargetType.getDeclaredField(f.getName()));
				}
			}
			return getDeclaredFields(pSourceType.getSuperclass(), pTargetType.getSuperclass(), pCollectedFields);
		}
		return pCollectedFields;
	}
	
	private Map<Field, Field> getDeclaredFields(final Class<?> pSourceType, final Class<?> pTargetType) throws NoSuchFieldException, SecurityException {
		return getDeclaredFields(pSourceType, pTargetType, new HashMap<>());
	}

	private Object copyState(final Function<Class<?>, Class<?>> pClassDetermination, final Class<?> pSourceType,
			final Class<?> pTargetType, final Object pSource, final Object pTarget)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException,
			InstantiationException, ClassNotFoundException {
		for (final Map.Entry<Field, Field> fieldEntry : getDeclaredFields(pSourceType, pTargetType).entrySet()) {
			final Field sourceField = fieldEntry.getKey();
			sourceField.setAccessible(true);
			final Object sourceValue = sourceField.get(pSource);
			Object targetValue = null;

			if (!sourceField.getType().isPrimitive()) {
				// Only do something if there is something to copy
				if (sourceValue != null) {
					final Class<?> sourceType = sourceValue.getClass();
					if (sourceType.isArray()) {
						final int size = Array.getLength(sourceValue);
						final Class<?> sourceComponentType = sourceType.getComponentType();
						if (size > 0 && !sourceComponentType.isPrimitive()) {
							Class<?> targetComponentType = pClassDetermination.apply(sourceComponentType);

							if (targetComponentType == null) {
								targetComponentType = sourceComponentType;
							}

							targetValue = Array.newInstance(targetComponentType, size);
							for (int i = 0; i < size; i++) {
								final Object sourceElementValue = Array.get(sourceValue, i);
								if (sourceElementValue != null) {
									targetComponentType = pClassDetermination.apply(sourceElementValue.getClass());

									final Object targetElementValue;
									if (targetComponentType == null) {
										targetElementValue = sourceElementValue;
									} else {
										targetElementValue = copyState(pClassDetermination,
												loader.swap(targetComponentType), targetComponentType,
												sourceElementValue, UNSAFE.allocateInstance(targetComponentType));
									}

									Array.set(targetValue, i, targetElementValue);
								}
							}
						}
					} else {
						Class<?> targetType = pClassDetermination.apply(sourceType);

						if (targetType != null) {
							targetValue = copyState(pClassDetermination, sourceType, targetType, sourceValue,
									UNSAFE.allocateInstance(targetType));
						} else if (!visited.contains(sourceValue)) {
							visited.add(sourceValue);
							targetValue = copyState(pClassDetermination, sourceType, sourceType, sourceValue,
									sourceValue);
						}
					}
				} else {
					targetValue = sourceValue;
				}
			}

			if (targetValue == null) {
				targetValue = sourceValue;
			}

			final Field targetField = fieldEntry.getValue();
			targetField.setAccessible(true);
			targetField.set(pTarget, targetValue);
		}
		return pTarget;
	}

	public Object copyState() throws InstantiationException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException, ClassNotFoundException {
		final Class<?> targetType = classDetermination.apply(source.getClass());
		return copyState(classDetermination, source.getClass(), targetType, source,
				UNSAFE.allocateInstance(targetType));
	}
}
