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

import static ch.sourcepond.testing.OptionsHelper.blueprintBundles;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;

import ch.sourcepond.utils.podescoin.testbundle.FieldInjectionObject;
import ch.sourcepond.utils.podescoin.testbundle.FieldInjectionObjectWithComponentId;
import ch.sourcepond.utils.podescoin.testbundle.Injected;
import ch.sourcepond.utils.podescoin.testbundle.InjectorMethodObject;
import ch.sourcepond.utils.podescoin.testbundle.InjectorMethodObjectWithComponentId;
import ch.sourcepond.utils.podescoin.testbundle.TestObjectFactory;

@RunWith(PaxExam.class)
public class WeavingHookTest {

	@Inject
	private TestObjectFactory factory;

	@Configuration
	public Option[] configure() {
		return options(
				mavenBundle("ch.sourcepond.utils", "podescoin-core").versionAsInProject(),
				mavenBundle("ch.sourcepond.utils", "podescoin-testbundle").versionAsInProject(),
				mavenBundle("ch.sourcepond.utils", "podescoin-testservice").versionAsInProject(), blueprintBundles(),
				junitBundles());
	}

	private void verifyService(final Injected pInjected, final String pDateServiceId, final String pNameServiceId) {
		assertNotNull(pInjected.getDateService());
		assertNotNull(pInjected.getNameService());
		assertEquals(pDateServiceId, pInjected.getDateService().getId());
		assertEquals(pNameServiceId, pInjected.getNameService().getId());
	}
	
	@Test
	public void verifyFieldInjection() throws Exception {
		final FieldInjectionObject obj = factory.getFieldInjectionObject();
		verifyService(obj, "testservice.date", "testservice.name");
	}
	
	@Test
	public void verifyFieldInjectionWithComponentId() throws Exception {
		final FieldInjectionObjectWithComponentId obj = factory.getFieldInjectionObjectWithComponentId();
		verifyService(obj, "testservice.ambiguousDate2", "testservice.ambiguousName1");
	}
	
	@Test
	public void verifyInjectorMethod() throws Exception {
		final InjectorMethodObject obj = factory.getInjectorMethodObject();
		verifyService(obj, "testservice.date", "testservice.name");
	}
	
	@Test
	public void verifyInjectorMethodWithComponentId() throws Exception {
		final InjectorMethodObjectWithComponentId obj = factory.getInjectorMethodObjectWithComponentId();
		verifyService(obj, "testservice.ambiguousDate2", "testservice.ambiguousName1");
	}
	
}
