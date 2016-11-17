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
package ch.sourcepond.utils.podescoin.testing;

import static ch.sourcepond.utils.podescoin.testing.CloneContextFactory.OSGI_BLUEPRINT_CONTAINER_SYMBOLICNAME_FILTER;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.blueprint.reflect.BeanMetadata;

import ch.sourcepond.utils.podescoin.internal.util.PodesCoinObjectInputStream;

/**
 * @author rolandhauser
 *
 */
public class CloneContext {
	private static final TestingClassLoader loader = new TestingClassLoader();
	private static final String TEST_BUNDLE_SYMBOLIC_NAME = "PodesCoinTestBundleInjector";
	private final Object detector = mock(CloneContextFactory.BUNDLE_DETECTOR_INTERFACE);
	private final Bundle bundle = mock(Bundle.class);
	private final BundleContext bundleContext = mock(BundleContext.class);
	private final BlueprintContainer blueprintContainer = mock(BlueprintContainer.class,
			withSettings().name("BlueprintContainer"));
	@SuppressWarnings("unchecked")
	private final ServiceReference<BlueprintContainer> blueprintContainerRef = mock(ServiceReference.class);
	private final Collection<ServiceReference<BlueprintContainer>> blueprintContainerRefs = asList(
			blueprintContainerRef);
	private final Set<String> componentIds = new HashSet<>();
	private final Cloner cloner = new Cloner(loader);

	/**
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * 
	 */
	protected CloneContext() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		CloneContextFactory.setDetector(detector);
		when(CloneContextFactory.GET_BUNDLE_METHOD.invoke(detector, (Class<?>) Mockito.any())).thenReturn(bundle);

		// when(detector.getBundle(pClass))
		when(bundle.getSymbolicName()).thenReturn(TEST_BUNDLE_SYMBOLIC_NAME);
		when(bundle.getBundleContext()).thenReturn(bundleContext);
		try {
			when(bundleContext.getServiceReferences(BlueprintContainer.class,
					format(OSGI_BLUEPRINT_CONTAINER_SYMBOLICNAME_FILTER, TEST_BUNDLE_SYMBOLIC_NAME)))
							.thenReturn(blueprintContainerRefs);
		} catch (final InvalidSyntaxException e) {
			// Will never happen
			e.printStackTrace();
		}
		when(bundleContext.getService(blueprintContainerRef))
				.thenReturn((BlueprintContainer) Proxy.newProxyInstance(getClass().getClassLoader(),
						new Class<?>[] { BlueprintContainer.class },
						new BlueprintContainerHandler(blueprintContainer)));
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
			// Will never happen
			e.printStackTrace();
		}
		when(blueprintContainer.getComponentMetadata(componentId)).thenReturn(meta);
		when(blueprintContainer.getComponentInstance(componentId)).thenReturn(pComponent);
		return this;
	}

	/**
	 * @param pComponent
	 * @param pType
	 * @return
	 */
	public <T> CloneContext addComponent(final T pComponent, final Class<T> pType) {
		return addComponentMetadata(pComponent, null, pType);
	}

	/**
	 * @param pComponent
	 * @param pComponentId
	 * @param pType
	 * @return
	 */
	public <T> CloneContext addComponent(final T pComponent, final String pComponentId, final Class<T> pType) {
		final String componentId = pComponentId == null || pComponentId.isEmpty() ? UUID.randomUUID().toString()
				: pComponentId;
		return addComponentMetadata(pComponent, componentId, pType);
	}

	/**
	 * @param obj
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public <T extends Serializable> T deepClone(final T obj) throws IOException, ClassNotFoundException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (final ObjectOutputStream oout = new ObjectOutputStream(out)) {
			oout.writeObject(obj);
			try (final ObjectInputStream in = new PodesCoinObjectInputStream(loader,
					new ByteArrayInputStream(out.toByteArray()))) {
				return (T) cloner.copyState(in.readObject());
			}
		} catch (final Exception e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	/**
	 * 
	 */
	public void tearDown() {
		CloneContextFactory.resetDetector();
	}
}
