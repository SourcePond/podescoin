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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.inject.Named;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.blueprint.reflect.BeanMetadata;

import ch.sourcepond.utils.podescoin.Injector;
import ch.sourcepond.utils.podescoin.internal.util.PodesCoinObjectInputStream;

/**
 * @author rolandhauser
 *
 */
public class PodesCoinTestingContext implements TestRule {
	private static final TestingClassLoader loader = new TestingClassLoader();
	private static final String TEST_BUNDLE_SYMBOLIC_NAME = "PodesCoinTestBundleInjector";
	private static final Object ORIGINAL_DETECTOR;
	private static final Class<?> BUNDLE_DETECTOR_INTERFACE;
	private static final Method GET_BUNDLE_METHOD;
	private static final String OSGI_BLUEPRINT_CONTAINER_SYMBOLICNAME_FILTER;
	private static final Field DETECTOR_FIELD;

	static {
		try {
			final ClassLoader ldr = PodesCoinTestingContext.class.getClassLoader();
			BUNDLE_DETECTOR_INTERFACE = ldr.loadClass("ch.sourcepond.utils.podescoin.BundleDetector");
			GET_BUNDLE_METHOD = BUNDLE_DETECTOR_INTERFACE.getMethod("getBundle", Class.class);
			GET_BUNDLE_METHOD.setAccessible(true);

			final Class<?> bundleInjectorImpl = ldr.loadClass("ch.sourcepond.utils.podescoin.BundleInjectorImpl");
			final Field osgiBlueprintContainerSymbolicnameFilter = bundleInjectorImpl
					.getDeclaredField("OSGI_BLUEPRINT_CONTAINER_SYMBOLICNAME_FILTER");
			osgiBlueprintContainerSymbolicnameFilter.setAccessible(true);
			OSGI_BLUEPRINT_CONTAINER_SYMBOLICNAME_FILTER = (String) osgiBlueprintContainerSymbolicnameFilter
					.get(bundleInjectorImpl);
			DETECTOR_FIELD = Injector.class.getDeclaredField("detector");
			DETECTOR_FIELD.setAccessible(true);
			ORIGINAL_DETECTOR = DETECTOR_FIELD.get(Injector.class);
		} catch (final ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException
				| IllegalAccessException | NoSuchMethodException e) {
			throw new AssertionError(
					String.format("Class %s could not be initialized", PodesCoinTestingContext.class.getName()), e);
		}
	}

	private final Object detector = mock(BUNDLE_DETECTOR_INTERFACE);
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
	protected PodesCoinTestingContext()
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		setDetector(detector);
		when(GET_BUNDLE_METHOD.invoke(detector, (Class<?>) Mockito.any())).thenReturn(bundle);

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

	private static void setDetector(final Object detector) {
		try {
			DETECTOR_FIELD.set(Injector.class, detector);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new AssertionError(String.format("Field '%s' could not be set on %s", DETECTOR_FIELD.getName(),
					Injector.class.getName()), e);
		}
	}

	private static String toComponentId(final Named pNamed) {
		String componentId = null;
		if (pNamed != null) {
			componentId = pNamed.value();
		}
		if (componentId == null || componentId.isEmpty()) {
			componentId = UUID.randomUUID().toString();
		}
		return componentId;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String setupMetadata(final String pComponentId, final Class<?> pComponentType) {
		componentIds.add(pComponentId);
		final BeanMetadata meta = mock(BeanMetadata.class);
		when(meta.getClassName()).thenReturn(pComponentType.getName());
		try {
			when(bundle.loadClass(pComponentType.getName())).thenReturn((Class) pComponentType);
		} catch (final ClassNotFoundException e) {
			// Will never happen
			e.printStackTrace();
		}
		when(blueprintContainer.getComponentMetadata(pComponentId)).thenReturn(meta);
		return pComponentId;
	}

	private PodesCoinTestingContext addComponentMetadata(final Named pNamed, final Object pComponent,
			final Class<?> pComponentType) {
		when(blueprintContainer.getComponentInstance(setupMetadata(toComponentId(pNamed), pComponentType)))
				.thenReturn(pComponent);
		return this;
	}

	/**
	 * @param pComponent
	 * @param pType
	 * @return
	 */
	public <T> PodesCoinTestingContext addComponent(final T pComponent, final Class<T> pComponentType) {
		return addComponentMetadata(null, pComponent, pComponentType);
	}

	/**
	 * @param pComponent
	 * @param pComponentId
	 * @param pType
	 * @return
	 */
	public <T> PodesCoinTestingContext addComponent(final String pComponentId, final T pComponent,
			final Class<T> pComponentType) {
		when(blueprintContainer.getComponentInstance(setupMetadata(pComponentId, pComponentType)))
				.thenReturn(pComponent);
		return this;
	}

	void addComponent(final Object pTest, final Named pNamed, final Field pField) {
		when(blueprintContainer.getComponentInstance(setupMetadata(toComponentId(pNamed), pField.getType())))
				.thenAnswer(new Answer<Object>() {

					@Override
					public Object answer(final InvocationOnMock invocation) throws Throwable {
						return pField.get(pTest);
					}
				});
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
		setDetector(ORIGINAL_DETECTOR);
	}

	@Override
	public Statement apply(final Statement base, final Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				try {
					base.evaluate();
				} finally {
					tearDown();
				}
			}
		};
	}

	private static List<Field> collectFields(final Class<?> pClassOrNull, final List<Field> pFields) {
		if (pClassOrNull != null) {
			for (final Field f : pClassOrNull.getDeclaredFields()) {
				if (f.isAnnotationPresent(Named.class)) {
					f.setAccessible(true);
					pFields.add(f);
				}
			}
			return collectFields(pClassOrNull.getSuperclass(), pFields);
		}
		return pFields;
	}

	public static PodesCoinTestingContext newContext(final Object pTest) {
		try {
			final PodesCoinTestingContext context = new PodesCoinTestingContext();
			for (final Field f : collectFields(pTest.getClass(), new LinkedList<>())) {
				final Named component = f.getAnnotation(Named.class);
				try {
					context.addComponent(pTest, component, f);
				} catch (final IllegalArgumentException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
			return context;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new AssertionError(
					String.format("Instance of %s could not be created!", PodesCoinTestingContext.class.getName()), e);
		}
	}
}
