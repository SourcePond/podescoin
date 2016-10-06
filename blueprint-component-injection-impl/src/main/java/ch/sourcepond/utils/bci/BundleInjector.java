package ch.sourcepond.utils.bci;

import static ch.sourcepond.utils.bci.Injector.injectors;
import static java.lang.String.format;
import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isTransient;
import static org.osgi.framework.ServiceEvent.UNREGISTERING;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.blueprint.reflect.BeanMetadata;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.osgi.service.blueprint.reflect.ServiceReferenceMetadata;

/**
 * @author rolandhauser
 *
 */
class BundleInjector implements ServiceListener {
	private final Bundle bundle;
	private final ServiceReference<BlueprintContainer> containerRef;
	private final BlueprintContainer container;

	BundleInjector(final Bundle pBundle) {
		bundle = pBundle;
		containerRef = getContainerRef();
		container = pBundle.getBundleContext().getService(containerRef);
	}

	private ServiceReference<BlueprintContainer> getContainerRef() {
		try {
			final BundleContext context = bundle.getBundleContext();
			final Collection<ServiceReference<BlueprintContainer>> refs = context.getServiceReferences(
					BlueprintContainer.class,
					format("(osgi.blueprint.container.symbolicname=%s)", bundle.getSymbolicName()));
			final Iterator<ServiceReference<BlueprintContainer>> it = refs.iterator();
			if (!it.hasNext()) {
				throw new IllegalStateException(
						format("No blueprint-container found with id '%s'", bundle.getSymbolicName()));
			}
			return it.next();
		} catch (final InvalidSyntaxException e) {
			// Should never happen
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	private Object getComponent(final Field pField, final String pComponentIdOrNull, final Class<?> pFieldType)
			throws ClassNotFoundException {
		if (pComponentIdOrNull == null || pComponentIdOrNull.isEmpty()) {
			final Map<String, Object> candidates = new HashMap<>();
			for (final String componentId : container.getComponentIds()) {
				String typeName = null;
				final ComponentMetadata metadata = container.getComponentMetadata(componentId);
				if (metadata instanceof BeanMetadata) {
					typeName = ((BeanMetadata) metadata).getClassName();
				} else if (metadata instanceof ServiceReferenceMetadata) {
					typeName = ((ServiceReferenceMetadata) metadata).getInterface();
				}

				if (typeName != null) {
					final Class<?> type = bundle.loadClass(typeName);

					if (pFieldType.isAssignableFrom(type)) {
						candidates.put(componentId, container.getComponentInstance(componentId));
					}
				}
			}

			if (candidates.size() > 1) {
				throw new AmbiguousComponentException(pField.getName(), candidates);
			}
			if (candidates.isEmpty()) {
				throw new NoSuchComponentException(pField.getName(), pFieldType);
			}
			return candidates.values().iterator().next();
		}
		return container.getComponentInstance(pComponentIdOrNull);
	}

	void initDeserializedObject(final Serializable pObj, final String[][] pComponentToFields)
			throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
		for (int i = 0; i < pComponentToFields.length; i++) {
			final String[] componentToField = pComponentToFields[i];

			if (componentToField.length != 3) {
				throw new IllegalArgumentException(
						format("Component-to-field mapping array must be of size 3! Illegal size %d",
								componentToField.length));
			}

			final Class<? extends Serializable> cl = pObj.getClass();
			for (int z = 0; z < componentToField.length; z++) {
				final Field field = cl.getDeclaredField(componentToField[0]);
				final int modifiers = field.getModifiers();

				if (!isTransient(modifiers)) {
					throw new IllegalArgumentException(format("Field '%s' must be transient!", field.getName()));
				}

				if (isFinal(modifiers)) {
					throw new IllegalArgumentException(format("Field '%s' cannot be final!", field.getName()));
				}

				try {
					field.setAccessible(true);
					final String componentIdOrNull = componentToField[1];
					final Class<?> fieldType = bundle.loadClass(componentToField[2]);
					final Object component = getComponent(field, componentIdOrNull, fieldType);

					if (!field.getType().isAssignableFrom(component.getClass())) {
						throw new ClassCastException(
								format("Field '%s' is of type '%s' which is not compatible to component with id '%s' and type '%s'",
										field.getName(), field.getType().getName(), componentIdOrNull,
										component.getClass().getName()));
					}

					field.set(pObj, component);
				} finally {
					field.setAccessible(false);
				}
			}
		}
	}

	@Override
	public void serviceChanged(final ServiceEvent event) {
		if (UNREGISTERING == event.getType() && containerRef.equals(event.getServiceReference())) {
			injectors.remove(bundle);
		}
	}
}
