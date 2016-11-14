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

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import ch.sourcepond.utils.podescoin.BundleInjectorFactory;
import ch.sourcepond.utils.podescoin.Injector;
import ch.sourcepond.utils.podescoin.internal.BundleInjector;

public abstract class ClassVisitorTest {
	protected final ClassWriter writer = new ClassWriter(0);
	@Mock
	protected BundleInjector injector;

	@Mock
	protected BundleInjectorFactory factory;

	@Mock
	protected Bundle bundle;

	@Mock
	protected BundleContext context;

	protected TestClassLoader loader;
	protected ClassVisitor visitor;

	@Before
	public void setup() {
		initMocks(this);
		when(factory.newInjector(bundle)).thenReturn(injector);
		when(bundle.getBundleContext()).thenReturn(context);
		Injector.factory = factory;
		visitor = newVisitor();
	}

	protected abstract ClassVisitor newVisitor();

	@After
	public void tearDown() {
		Injector.injectors.clear();
	}

	protected Method getMethod(final Object pObj, final String pName, final Class<?>... pArgumentTypes)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		final Method method = pObj.getClass().getDeclaredMethod(pName, pArgumentTypes);
		method.setAccessible(true);
		return method;
	}
}
