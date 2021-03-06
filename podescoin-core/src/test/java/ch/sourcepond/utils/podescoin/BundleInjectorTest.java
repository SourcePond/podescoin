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
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.osgi.framework.ServiceEvent.MODIFIED;
import static org.osgi.framework.ServiceEvent.UNREGISTERING;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.blueprint.reflect.BeanMetadata;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.osgi.service.blueprint.reflect.ServiceReferenceMetadata;

import ch.sourcepond.utils.podescoin.api.Component;

public class BundleInjectorTest {
	static final String COMPONENT_ID = "testComponentId";
	static final String FIELD_NAME = "testComponent";
	static final String ANY_BUNDLE_SYMBOLIC_NAME = "anyBundleSymbolicName";
	private final TestComponent testComponent = new TestComponent();
	@Mock
	private Bundle bundle;

	@Mock
	private BundleContext context;

	@Mock
	private ServiceReference<BlueprintContainer> containerRef;

	@Mock
	private BlueprintContainer container;
	private Collection<ServiceReference<BlueprintContainer>> containerRefs;
	private BundleInjectorImpl injector;

	@Before
	public void setup() throws Exception {
		initMocks(this);
		when(bundle.getSymbolicName()).thenReturn(ANY_BUNDLE_SYMBOLIC_NAME);
		when(bundle.getBundleContext()).thenReturn(context);
		containerRefs = asList(containerRef);
		when(context.getServiceReferences(BlueprintContainer.class,
				"(osgi.blueprint.container.symbolicname=anyBundleSymbolicName)")).thenReturn(containerRefs);
		when(context.getService(containerRef)).thenReturn(container);
		injector = new BundleInjectorImpl(bundle);
	}

	@After
	public void tearDown() {
		Injector.injectors.clear();
	}

	@Test
	public void serviceChanged() {
		injectors.put(bundle, injector);
		final ServiceEvent event = new ServiceEvent(UNREGISTERING, containerRef);
		injector.serviceChanged(event);
		assertTrue(injectors.isEmpty());
	}

	@Test
	public void serviceChanged_EventTypeNotInteresting() {
		injectors.put(bundle, injector);
		final ServiceEvent event = new ServiceEvent(MODIFIED, containerRef);
		injector.serviceChanged(event);
		assertFalse(injectors.isEmpty());
	}

	@Test
	public void serviceChanged_ServiceReferenceNotInteresting() {
		injectors.put(bundle, injector);
		final ServiceEvent event = new ServiceEvent(UNREGISTERING, mock(ServiceReference.class));
		injector.serviceChanged(event);
		assertFalse(injectors.isEmpty());
	}

	@Test
	public void getContainerRef_NoBlueprintContainerAvailable() throws Exception {
		when(context.getServiceReferences(BlueprintContainer.class,
				"(osgi.blueprint.container.symbolicname=anyBundleSymbolicName)")).thenReturn(Collections.emptyList());
		try {
			new BundleInjectorImpl(bundle);
			fail("Exception expected");
		} catch (final IllegalStateException expected) {
			assertEquals("No blueprint-container found with id 'anyBundleSymbolicName'", expected.getMessage());
		}
	}

	@Test
	public void getContainerRef_InvalidSyntaxException() throws Exception {
		final InvalidSyntaxException expected = new InvalidSyntaxException("", "");
		Mockito.doThrow(expected).when(context).getServiceReferences(BlueprintContainer.class,
				"(osgi.blueprint.container.symbolicname=anyBundleSymbolicName)");
		try {
			new BundleInjectorImpl(bundle);
			fail("Exception expected");
		} catch (final IllegalStateException e) {
			assertSame(expected, e.getCause());
		}
	}

	public static class InitDeserializedObject implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Component
		public transient TestComponent testComponent;
	}

	public static class InitDeserializedObjectWithId implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Component("testComponentId")
		public transient TestComponent testComponent;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void initDeserializedObject() throws Exception {
		when(container.getComponentInstance(COMPONENT_ID)).thenReturn(testComponent);
		when(bundle.loadClass(TestComponent.class.getName())).thenReturn((Class) TestComponent.class);
		final InitDeserializedObjectWithId obj = new InitDeserializedObjectWithId();
		injector.initDeserializedObject(obj,
				new String[][] { { FIELD_NAME, COMPONENT_ID, TestComponent.class.getName() } });
		assertSame(testComponent, obj.testComponent);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void initDeserializedObject_ComponentIsNotCompatible() throws Exception {
		when(container.getComponentInstance(COMPONENT_ID)).thenReturn(new Object());
		when(bundle.loadClass(TestComponent.class.getName())).thenReturn((Class) TestComponent.class);
		final InitDeserializedObjectWithId obj = new InitDeserializedObjectWithId();

		try {
			injector.initDeserializedObject(obj,
					new String[][] { { FIELD_NAME, COMPONENT_ID, TestComponent.class.getName() } });
			fail("Exception expected!");
		} catch (final ClassCastException expected) {
			assertEquals(
					"Field 'testComponent' is of type 'ch.sourcepond.utils.podescoin.TestComponent' which is not compatible to component with id 'testComponentId' and type 'java.lang.Object'",
					expected.getMessage());
		}
	}

	@Test
	public void initDeserializedObject_IllegalArraySize() throws Exception {
		when(container.getComponentInstance(COMPONENT_ID)).thenReturn(testComponent);
		final InitDeserializedObject obj = new InitDeserializedObject();

		try {
			injector.initDeserializedObject(obj,
					new String[][] { { FIELD_NAME, COMPONENT_ID, "Illegal argument", TestComponent.class.getName() } });
			fail("Exception expected!");
		} catch (final IllegalArgumentException expected) {
			assertEquals("Component-to-field mapping array must be of size 3! Illegal size 4", expected.getMessage());
		}
	}

	public static class InitDeserializedObjectFieldNotTransient implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Component("testComponentId")
		public TestComponent testComponent;
	}

	@Test
	public void initDeserializedObject_FieldNotTransient() throws Exception {
		when(container.getComponentInstance(COMPONENT_ID)).thenReturn(testComponent);
		final InitDeserializedObjectFieldNotTransient obj = new InitDeserializedObjectFieldNotTransient();

		try {
			injector.initDeserializedObject(obj,
					new String[][] { { FIELD_NAME, COMPONENT_ID, TestComponent.class.getName() } });
			fail("Exception expected!");
		} catch (final IllegalArgumentException expected) {
			assertEquals("Field 'testComponent' must be transient!", expected.getMessage());
		}
	}

	public static class InitDeserializedObjectFieldIsFinal implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Component("testComponentId")
		public transient final TestComponent testComponent = null;
	}

	@Test
	public void initDeserializedObject_FieldIsFinal() throws Exception {
		when(container.getComponentInstance(COMPONENT_ID)).thenReturn(testComponent);
		final InitDeserializedObjectFieldIsFinal obj = new InitDeserializedObjectFieldIsFinal();

		try {
			injector.initDeserializedObject(obj,
					new String[][] { { FIELD_NAME, COMPONENT_ID, TestComponent.class.getName() } });
			fail("Exception expected!");
		} catch (final IllegalArgumentException expected) {
			assertEquals("Field 'testComponent' cannot be final!", expected.getMessage());
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void initDeserializedObjectTypeOnly_UnsupportedMetadataType() throws Exception {
		when(container.getComponentInstance(COMPONENT_ID)).thenReturn(testComponent);
		when(container.getComponentIds()).thenReturn(new HashSet<>(asList(COMPONENT_ID)));
		final ComponentMetadata metadata = mock(ComponentMetadata.class);
		when(container.getComponentMetadata(COMPONENT_ID)).thenReturn(metadata);
		when(bundle.loadClass(TestComponent.class.getName())).thenReturn((Class) TestComponent.class);
		final InitDeserializedObject obj = new InitDeserializedObject();

		try {
			injector.initDeserializedObject(obj,
					new String[][] { { FIELD_NAME, null, TestComponent.class.getName() } });
			fail("Exception expected!");
		} catch (final NoSuchComponentException expected) {
			// noop
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void initDeserializedObjectTypeOnly_AmbiguousComponents() throws Exception {
		final String secondComponentId = "secondComponentId";

		when(container.getComponentInstance(COMPONENT_ID)).thenReturn(testComponent);
		when(container.getComponentIds()).thenReturn(new HashSet<>(asList(COMPONENT_ID, secondComponentId)));
		final BeanMetadata metadata = mock(BeanMetadata.class);
		when(container.getComponentMetadata(COMPONENT_ID)).thenReturn(metadata);
		when(container.getComponentMetadata(secondComponentId)).thenReturn(metadata);
		when(metadata.getClassName()).thenReturn(TestComponent.class.getName());
		when(bundle.loadClass(TestComponent.class.getName())).thenReturn((Class) TestComponent.class);

		final InitDeserializedObject obj = new InitDeserializedObject();

		try {
			injector.initDeserializedObject(obj,
					new String[][] { { FIELD_NAME, null, TestComponent.class.getName() } });
			fail("Exception expected!");
		} catch (final AmbiguousComponentException expected) {
			// noop
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void initDeserializedObjectTypeOnly_TypeNotAssignable() throws Exception {
		when(container.getComponentInstance(COMPONENT_ID)).thenReturn(testComponent);
		when(container.getComponentIds()).thenReturn(new HashSet<>(asList(COMPONENT_ID)));
		final BeanMetadata metadata = mock(BeanMetadata.class);
		when(container.getComponentMetadata(COMPONENT_ID)).thenReturn(metadata);
		when(metadata.getClassName()).thenReturn(Object.class.getName());
		when(bundle.loadClass(TestComponent.class.getName())).thenReturn((Class) TestComponent.class);
		when(bundle.loadClass(Object.class.getName())).thenReturn((Class) Object.class);

		final InitDeserializedObject obj = new InitDeserializedObject();

		try {
			injector.initDeserializedObject(obj,
					new String[][] { { FIELD_NAME, null, TestComponent.class.getName() } });
			fail("Exception expected!");
		} catch (final NoSuchComponentException expected) {
			// noop
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void initDeserializedObjectTypeOnly_BeanMetatdata_EmptyComponentId() throws Exception {
		when(container.getComponentInstance(COMPONENT_ID)).thenReturn(testComponent);
		when(container.getComponentIds()).thenReturn(new HashSet<>(asList(COMPONENT_ID)));
		final BeanMetadata metadata = mock(BeanMetadata.class);
		when(container.getComponentMetadata(COMPONENT_ID)).thenReturn(metadata);
		when(metadata.getClassName()).thenReturn(TestComponent.class.getName());
		when(bundle.loadClass(TestComponent.class.getName())).thenReturn((Class) TestComponent.class);

		final InitDeserializedObject obj = new InitDeserializedObject();
		injector.initDeserializedObject(obj, new String[][] { { FIELD_NAME, "", TestComponent.class.getName() } });
		assertSame(testComponent, obj.testComponent);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void initDeserializedObjectTypeOnly_BeanMetatdata() throws Exception {
		when(container.getComponentInstance(COMPONENT_ID)).thenReturn(testComponent);
		when(container.getComponentIds()).thenReturn(new HashSet<>(asList(COMPONENT_ID)));
		final BeanMetadata metadata = mock(BeanMetadata.class);
		when(container.getComponentMetadata(COMPONENT_ID)).thenReturn(metadata);
		when(metadata.getClassName()).thenReturn(TestComponent.class.getName());
		when(bundle.loadClass(TestComponent.class.getName())).thenReturn((Class) TestComponent.class);

		final InitDeserializedObject obj = new InitDeserializedObject();
		injector.initDeserializedObject(obj, new String[][] { { FIELD_NAME, null, TestComponent.class.getName() } });
		assertSame(testComponent, obj.testComponent);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void initDeserializedObjectTypeOnly_ServiceReferenceMetatdata() throws Exception {
		when(container.getComponentInstance(COMPONENT_ID)).thenReturn(testComponent);
		when(container.getComponentIds()).thenReturn(new HashSet<>(asList(COMPONENT_ID)));
		final ServiceReferenceMetadata metadata = mock(ServiceReferenceMetadata.class);
		when(container.getComponentMetadata(COMPONENT_ID)).thenReturn(metadata);
		when(metadata.getInterface()).thenReturn(TestComponent.class.getName());
		when(bundle.loadClass(TestComponent.class.getName())).thenReturn((Class) TestComponent.class);

		final InitDeserializedObject obj = new InitDeserializedObject();
		injector.initDeserializedObject(obj, new String[][] { { FIELD_NAME, null, TestComponent.class.getName() } });
		assertSame(testComponent, obj.testComponent);
	}
}
