package ch.sourcepond.utils.podescoin.testing.examples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import ch.sourcepond.utils.podescoin.CloneContext;
import ch.sourcepond.utils.podescoin.Cloner;
import ch.sourcepond.utils.podescoin.Component;

public class ExampleTest2 {

	@Component("anyId")
	private final TestService1 srv1 = mock(TestService1.class);

	@Component("anySecondId")
	private final TestService2 srv2 = mock(TestService2.class);

	@Component
	private final TestService3 srv3 = mock(TestService3.class);

	private final SomeComponent component = mock(SomeComponent.class);

	@Test
	public void verifyObject() throws Exception {
		when(srv1.load("1234")).thenReturn(component);

		final CloneContext ctx = Cloner.newContext(this);

		final TestSerializable obj = new TestSerializable(component);
		obj.setName("Roland");

		final TestSerializable clone = ctx.deepClone(obj);

		assertEquals("Roland", clone.getName());
		assertSame(component, clone.getSomeComponent());
	}
}
