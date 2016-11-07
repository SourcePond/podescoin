package ch.sourcepond.utils.podescoin;

import static ch.sourcepond.testing.OptionsHelper.blueprintBundles;
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
import ch.sourcepond.utils.podescoin.testbundle.TestObjectFactory;

@RunWith(PaxExam.class)
public class WeavingHookTest {

	@Inject
	private TestObjectFactory factory;

	@Configuration
	public Option[] configure() {
		return options(
				mavenBundle("ch.sourcepond.utils", "podescoin-impl").versionAsInProject(),
				mavenBundle("ch.sourcepond.utils", "podescoin-testbundle").versionAsInProject(),
				mavenBundle("ch.sourcepond.utils", "podescoin-testservice").versionAsInProject(), blueprintBundles(),
				junitBundles());
	}

	@Test
	public void verifyStuff() throws Exception {
		final FieldInjectionObject obj = factory.getFieldInjectionObject();
		assertNotNull(obj.dateService);
		assertNotNull(obj.nameService);
	}
}
