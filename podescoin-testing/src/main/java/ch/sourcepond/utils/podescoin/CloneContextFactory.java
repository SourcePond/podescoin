package ch.sourcepond.utils.podescoin;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public class CloneContextFactory {

	public static CloneContext newContext() {
		return new CloneContext();
	}

	private static List<Field> collectFields(final Class<?> pClassOrNull, final List<Field> pFields) {
		if (pClassOrNull != null) {
			for (final Field f : pClassOrNull.getDeclaredFields()) {
				if (f.isAnnotationPresent(Component.class)) {
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
			final Component component = f.getAnnotation(Component.class);
			try {
				context.addComponent(f.get(pTest), component.value(), (Class) f.getType());
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		return context;
	}
}
