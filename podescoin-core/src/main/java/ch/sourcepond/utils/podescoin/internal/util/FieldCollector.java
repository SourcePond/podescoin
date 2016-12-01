package ch.sourcepond.utils.podescoin.internal.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.function.Predicate;

public class FieldCollector extends HashMap<Field, Field> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Predicate<Field> predicate;

	public FieldCollector(final Predicate<Field> pPredicate, final Class<?> pSourceType, final Class<?> pTargetType)
			throws NoSuchFieldException, SecurityException {
		predicate = pPredicate;
		collectDeclaredFields(pSourceType, pTargetType);
	}

	public FieldCollector(final Class<?> pSourceType, final Class<?> pTargetType)
			throws NoSuchFieldException, SecurityException {
		this(f -> true, pSourceType, pTargetType);
	}

	private void collectDeclaredFields(final Class<?> pSourceType, final Class<?> pTargetType)
			throws NoSuchFieldException, SecurityException {
		if (pSourceType != null) {
			for (final Field f : pSourceType.getDeclaredFields()) {
				if (!Modifier.isStatic(f.getModifiers()) && predicate.test(f)) {
					put(f, pTargetType.getDeclaredField(f.getName()));
				}
			}
			collectDeclaredFields(pSourceType.getSuperclass(), pTargetType.getSuperclass());
		}
	}
}
