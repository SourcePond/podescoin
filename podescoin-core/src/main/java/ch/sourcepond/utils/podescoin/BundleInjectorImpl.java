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

import static ch.sourcepond.utils.podescoin.Injector.injectors;
import static java.lang.String.format;
import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isTransient;
import static org.osgi.framework.ServiceEvent.UNREGISTERING;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

import ch.sourcepond.utils.podescoin.api.Component;
import ch.sourcepond.utils.podescoin.internal.BundleInjector;

/**
 * @author rolandhauser
 *
 */
final class BundleInjectorImpl implements ServiceListener, Container, BundleInjector {
	private static final String OSGI_BLUEPRINT_CONTAINER_SYMBOLICNAME_FILTER = "(osgi.blueprint.container.symbolicname=%s)";

	// We need to keep a list of fields because there could be fields with the
	// same name on different levels in the class hierarchy.
	private final ConcurrentMap<String, Collection<Field>> fields = new ConcurrentHashMap<>();
	private final Bundle bundle;
	private final ServiceReference<BlueprintContainer> containerRef;
	private final BlueprintContainer container;

	BundleInjectorImpl(final Bundle pBundle) {
		bundle = pBundle;
		containerRef = getContainerRef();
		container = pBundle.getBundleContext().getService(containerRef);
	}

	private ServiceReference<BlueprintContainer> getContainerRef() {
		try {
			final BundleContext context = bundle.getBundleContext();
			final Collection<ServiceReference<BlueprintContainer>> refs = context.getServiceReferences(
					BlueprintContainer.class,
					format(OSGI_BLUEPRINT_CONTAINER_SYMBOLICNAME_FILTER, bundle.getSymbolicName()));
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

	private Map<String, Object> findCandidates(final Class<?> pTargetType) throws ClassNotFoundException {
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

				if (pTargetType.isAssignableFrom(type)) {
					candidates.put(componentId, container.getComponentInstance(componentId));
				}
			}
		}
		return candidates;
	}

	private Object getComponent(final String pFieldNameOrNull, final int pParameterIndex,
			final String pComponentIdOrNull, final Class<?> pTargetType) throws ClassNotFoundException {
		final Object component;
		if (isComponentIdEmpty(pComponentIdOrNull)) {
			final Map<String, Object> candidates = findCandidates(pTargetType);

			if (candidates.size() > 1) {
				throw new AmbiguousComponentException(pFieldNameOrNull, pParameterIndex, candidates);
			}
			if (candidates.isEmpty()) {
				throw new NoSuchComponentException(pFieldNameOrNull, pParameterIndex, pTargetType);
			}
			component = candidates.values().iterator().next();
		} else {
			component = container.getComponentInstance(pComponentIdOrNull);
			if (!pTargetType.isAssignableFrom(component.getClass())) {
				if (pFieldNameOrNull != null) {
					throw new ClassCastException(
							format("Field '%s' is of type '%s' which is not compatible to component with id '%s' and type '%s'",
									pFieldNameOrNull, pTargetType.getName(), pComponentIdOrNull,
									component.getClass().getName()));
				}
				throw new ClassCastException(format(
						"Parameter at index %d is of type '%s' which is not compatible to component with id '%s' and type '%s'",
						pParameterIndex, pTargetType.getName(), pComponentIdOrNull, component.getClass().getName()));
			}
		}
		return component;
	}

	private Collection<Field> findFields(final Class<?> pClass, final String pFieldName,
			final Collection<Field> pFields) {
		if (pClass != null) {
			for (final Field field : pClass.getDeclaredFields()) {
				if (pFieldName.equals(field.getName())) {
					pFields.add(field);
				}
			}
			findFields(pClass.getSuperclass(), pFieldName, pFields);
		}
		return pFields;
	}

	private boolean isComponentIdEmpty(final String pComponentIdOrNull) {
		return pComponentIdOrNull == null || pComponentIdOrNull.isEmpty();
	}

	private Collection<Field> getDeclaredFields(final Class<?> pClass, final String pFieldName,
			final String pComponentId) throws NoSuchFieldException {
		final String key = pClass + "_" + pFieldName + "_" + pComponentId;
		Collection<Field> fieldCollection = fields.get(key);
		if (fieldCollection == null) {
			fieldCollection = findFields(pClass, pFieldName, new LinkedList<>());
			for (final Iterator<Field> it = fieldCollection.iterator(); it.hasNext();) {
				final Component component = it.next().getAnnotation(Component.class);
				if (component != null) {
					final String expectedId = component.value();
					final String actualId = pComponentId == null ? "" : pComponentId;
					if (!expectedId.equals(actualId)) {
						it.remove();
					}
				} else {
					it.remove();
				}
			}
			if (fieldCollection.isEmpty()) {
				throw new NoSuchFieldException(String.format(
						"No field with name %s could be found in class hierarchy of %s", pFieldName, pClass.getName()));
			}
			fields.putIfAbsent(key, fieldCollection);
		}
		return fieldCollection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.sourcepond.utils.podescoin.IBundleInjector#initDeserializedObject(java
	 * .io. Serializable, java.lang.String[][])
	 */
	@Override
	public void initDeserializedObject(final Serializable pObj, final String[][] pComponentToFields)
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
				for (final Field field : getDeclaredFields(cl, componentToField[0], componentToField[1])) {
					final int modifiers = field.getModifiers();

					if (!isTransient(modifiers)) {
						throw new IllegalArgumentException(format("Field '%s' must be transient!", field.getName()));
					}

					if (isFinal(modifiers)) {
						throw new IllegalArgumentException(format("Field '%s' cannot be final!", field.getName()));
					}

					try {
						field.setAccessible(true);
						field.set(pObj, getComponent(field.getName(), 0, componentToField[1],
								bundle.loadClass(componentToField[2])));
					} finally {
						field.setAccessible(false);
					}
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

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getComponentById(final String pComponentId, final String pExpectedTypeName,
			final int pParameterIndex) {
		try {
			return (T) getComponent(null, pParameterIndex, pComponentId, bundle.loadClass(pExpectedTypeName));
		} catch (final ClassNotFoundException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getComponentByTypeName(final String pTypeName, final int pParameterIndex) {
		try {
			return (T) getComponent(null, pParameterIndex, null, bundle.loadClass(pTypeName));
		} catch (final ClassNotFoundException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
}
