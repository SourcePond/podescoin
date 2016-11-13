package ch.sourcepond.utils.podescoin;

import static ch.sourcepond.utils.podescoin.BundleInjectorImpl.OSGI_BLUEPRINT_CONTAINER_SYMBOLICNAME;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.blueprint.reflect.BeanMetadata;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class CloneContext {
	private static final EnhancedClassLoader loader = new EnhancedClassLoader(CloneContext.class.getClassLoader());;

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

	private static final String TEST_BUNDLE_SYMBOLIC_NAME = "PodesCoinTestBundleInjector";
	private final BundleDetector detector = mock(BundleDetector.class);
	private final Bundle bundle = mock(Bundle.class);
	private final BundleContext bundleContext = mock(BundleContext.class);
	private final BlueprintContainer blueprintContainer = mock(BlueprintContainer.class);
	@SuppressWarnings("unchecked")
	private final ServiceReference<BlueprintContainer> blueprintContainerRef = mock(ServiceReference.class);
	private final Collection<ServiceReference<BlueprintContainer>> blueprintContainerRefs = asList(
			blueprintContainerRef);
	private final Set<String> componentIds = new HashSet<>();

	protected CloneContext() {
		Injector.detector = detector;
		when(detector.getBundle(Mockito.any())).thenReturn(bundle);

		// when(detector.getBundle(pClass))
		when(bundle.getSymbolicName()).thenReturn(TEST_BUNDLE_SYMBOLIC_NAME);
		when(bundle.getBundleContext()).thenReturn(bundleContext);
		try {
			when(bundleContext.getServiceReferences(BlueprintContainer.class,
					format(OSGI_BLUEPRINT_CONTAINER_SYMBOLICNAME, TEST_BUNDLE_SYMBOLIC_NAME)))
							.thenReturn(blueprintContainerRefs);
		} catch (final InvalidSyntaxException e) {
			// Will never happen
			e.printStackTrace();
		}
		when(bundleContext.getService(blueprintContainerRef)).thenReturn(blueprintContainer);
		when(blueprintContainer.getComponentIds()).thenReturn(componentIds);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private CloneContext addComponentMetadata(final Object pComponent, final String componentId, final Class<?> pType) {
		componentIds.add(componentId);
		final BeanMetadata meta = mock(BeanMetadata.class);
		when(meta.getClassName()).thenReturn(pType.getName());
		try {
			when(bundle.loadClass(pType.getName())).thenReturn((Class) pType);
		} catch (final ClassNotFoundException e) {
			// Should never happen
			e.printStackTrace();
		}
		when(blueprintContainer.getComponentMetadata(componentId)).thenReturn(meta);
		when(blueprintContainer.getComponentInstance(componentId)).thenReturn(pComponent);
		return this;
	}

	public <T> CloneContext addComponent(final T pComponent, final Class<T> pType) {
		final String componentId = UUID.randomUUID().toString();
		return addComponentMetadata(pComponent, componentId, pType);
	}

	public <T> CloneContext addComponent(final T pComponent, final String pComponentId, final Class<T> pType) {
		return addComponentMetadata(pComponent, pComponentId, pType);
	}

	private boolean hasRegisteredType(final Collection<Object> pCollection) throws ClassNotFoundException {
		for (final Object o : pCollection) {
			if (o != null && loader.isRegistered(o.getClass())) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private Object copyState(final Class<?> pOriginalClass, final Class<?> pEnhancedClass, final Object pOriginal,
			final Object pEnhancedClone) throws NoSuchFieldException, SecurityException, IllegalArgumentException,
			IllegalAccessException, InstantiationException, ClassNotFoundException {
		if (pOriginalClass != null) {
			for (final Field originalField : pOriginalClass.getDeclaredFields()) {
				if (!Modifier.isStatic(originalField.getModifiers())) {
					final Field enhancedField = pEnhancedClass.getDeclaredField(originalField.getName());
					originalField.setAccessible(true);
					enhancedField.setAccessible(true);

					final Object valueFromEnhancedObject = enhancedField.get(pEnhancedClone);
					if (valueFromEnhancedObject instanceof Collection) {
						final Collection<Object> original = (Collection<Object>) valueFromEnhancedObject;
						if (!original.isEmpty() && hasRegisteredType(original)) {
							final Collection<?> clonedCollection = new ArrayList<>(
									(Collection<?>) valueFromEnhancedObject);
							original.clear();
							for (final Object o : clonedCollection) {
								if (o != null) {
									final Class<?> originalClass = loader.getOriginalClass(o.getClass());
									original.add(copyState(originalClass, o.getClass(),
											UNSAFE.allocateInstance(originalClass), o));
								}
							}
						}
					}

					if (valueFromEnhancedObject instanceof Map) {
						final Map<Object, Object> original = (Map<Object, Object>) valueFromEnhancedObject;
						if (!original.isEmpty()
								&& (hasRegisteredType(original.keySet()) || hasRegisteredType(original.values()))) {
							final Map<Object, Object> clone = new HashMap<>(original);
							original.clear();

							for (final Map.Entry<Object, Object> o : clone.entrySet()) {
								Object key = o.getKey();
								if (loader.isRegistered(key.getClass())) {
									final Class<?> originalKeyClass = loader.getOriginalClass(key.getClass());
									key = copyState(originalKeyClass, key.getClass(),
											UNSAFE.allocateInstance(originalKeyClass), key);
								}

								final Object value = o.getValue();
								if (loader.isRegistered(value.getClass())) {
									final Class<?> originalValueClass = loader.getOriginalClass(value.getClass());
									key = copyState(originalValueClass, key.getClass(),
											UNSAFE.allocateInstance(originalValueClass), key);
								}

								original.put(key, value);
							}
						}
					}

					if (loader.isRegistered(originalField.getType())) {
						originalField.set(pOriginal, copyState(originalField.getType(), enhancedField.getType(),
								UNSAFE.allocateInstance(originalField.getType()), valueFromEnhancedObject));
					} else {
						originalField.set(pOriginal, valueFromEnhancedObject);
					}
				}
			}
			return copyState(pOriginalClass.getSuperclass(), pEnhancedClass.getSuperclass(), pOriginal, pEnhancedClone);
		}
		return pOriginal;
	}

	@SuppressWarnings("unchecked")
	public <T extends Serializable> T deepClone(final T obj) throws IOException, ClassNotFoundException {
		loader.register(obj.getClass());

		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (final ObjectOutputStream oout = new ObjectOutputStream(out)) {
			oout.writeObject(obj);
		}

		try (final ObjectInputStream in = new EnhancedObjectInputStream(loader,
				new ByteArrayInputStream(out.toByteArray()))) {
			final Object deserialized = in.readObject();
			return (T) copyState(obj.getClass(), deserialized.getClass(), UNSAFE.allocateInstance(obj.getClass()),
					deserialized);
		} catch (final Exception e) {
			throw new IOException(e.getMessage(), e);
		}
	}
}
