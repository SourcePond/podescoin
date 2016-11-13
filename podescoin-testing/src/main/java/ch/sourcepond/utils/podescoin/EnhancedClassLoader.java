package ch.sourcepond.utils.podescoin;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.inject.Inject;

import ch.sourcepond.utils.podescoin.internal.Activator;

final class EnhancedClassLoader extends ClassLoader {
	private final Map<Class<?>, Class<?>> enhancedClasses = new HashMap<>();
	private final Map<Class<?>, Class<?>> originalClasses = new HashMap<>();

	EnhancedClassLoader(final ClassLoader pParent) {
		super(pParent);
	}

	@Override
	protected Class<?> findClass(final String name) throws ClassNotFoundException {
		Class<?> cl = enhancedClasses.get(name);
		if (cl == null) {
			cl = super.findClass(name);
		} else {
			System.out.println(cl);
		}
		return cl;
	}

	private <T extends AccessibleObject> boolean hasAnnotation(final Class<?> pClass,
			final Function<Class<?>, T[]> pFunc, final Predicate<AnnotatedElement> pTester) {
		if (pClass != null) {
			for (final T member : pFunc.apply(pClass)) {
				if (pTester.test(member)) {
					return true;
				}
			}
			return hasAnnotation(pClass.getSuperclass(), pFunc, pTester);
		}
		return false;
	}

	private Class<?>[] determineTypeParameterClass(final Type pGenericType) throws ClassNotFoundException {
		Class<?>[] result = null;
		if (pGenericType != null && pGenericType instanceof ParameterizedType) {
			final Type[] actualTypeArguments = ((ParameterizedType) pGenericType).getActualTypeArguments();
			if (actualTypeArguments.length > 0) {
				result = new Class<?>[actualTypeArguments.length];
				for (int i = 0; i < actualTypeArguments.length; i++) {
					try {
						result[i] = getClass().getClassLoader().loadClass(actualTypeArguments[i].getTypeName());
					} catch (final ClassNotFoundException e) {
						result[i] = Object.class;
					}
				}
			}
		}

		if (result == null) {
			result = new Class<?>[0];
		}

		return result;
	}

	private Class<?> enhanceClass(final Class<?> pClass) throws ClassNotFoundException {
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
		return defineClass(pClass.getName(), enhancedClassData, 0, enhancedClassData.length,
				getClass().getProtectionDomain());
	}

	private void register(final Class<?> pClass, final Field pField, final Set<Class<?>> pVisitedClasses)
			throws ClassNotFoundException {
		if (pClass != null && !pVisitedClasses.contains(pClass) && !enhancedClasses.containsKey(pClass)) {
			pVisitedClasses.add(pClass);
			if (pClass.isArray()) {
				register(pClass.getComponentType(), null, pVisitedClasses);
			} else if (Collection.class.isAssignableFrom(pClass) || Map.class.isAssignableFrom(pClass)) {
				for (final Class<?> actualType : determineTypeParameterClass(pField.getGenericType())) {
					register(actualType, null, pVisitedClasses);
				}
			} else {
				final Predicate<AnnotatedElement> tester = e -> e.isAnnotationPresent(Inject.class);
				if (hasAnnotation(pClass, cl -> cl.getDeclaredFields(), tester)
						|| hasAnnotation(pClass, cl -> cl.getDeclaredMethods(), tester)) {
					final Class<?> enhancedClass = enhanceClass(pClass);
					enhancedClasses.put(pClass, enhancedClass);
					originalClasses.put(enhancedClass, pClass);
				}
			}

			for (final Field f : pClass.getDeclaredFields()) {
				register(f.getType(), f, pVisitedClasses);
			}
			register(pClass.getSuperclass(), pField, pVisitedClasses);
		}
	}

	public Class<?> getOriginalClass(final Class<?> pEnhancedClass) throws ClassNotFoundException {
		return originalClasses.get(pEnhancedClass);
	}

	public boolean isRegistered(final Class<?> pClass) throws ClassNotFoundException {
		return originalClasses.containsKey(pClass);
	}

	public void register(final Class<?> pClass) throws ClassNotFoundException {
		register(pClass, null, new HashSet<>());
	}
}
