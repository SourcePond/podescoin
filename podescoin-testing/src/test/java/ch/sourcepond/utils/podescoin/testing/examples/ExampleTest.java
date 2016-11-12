package ch.sourcepond.utils.podescoin.testing.examples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import ch.sourcepond.utils.podescoin.CloneContext;
import ch.sourcepond.utils.podescoin.Cloner;

public class ExampleTest {
	private static final String ANY_OID = "1234";
	private final TestService1 srv1 = mock(TestService1.class);
	private final TestService2 srv2 = mock(TestService2.class);
	private final TestService3 srv3 = mock(TestService3.class);
	private final SomeComponent component = mock(SomeComponent.class);

	@Test
	public void verifyObject() throws Exception {
		when(srv1.load(ANY_OID)).thenReturn(component);
		when(component.getOid()).thenReturn(ANY_OID);

		final CloneContext ctx = Cloner.newContext().addComponent(srv1, "anyId", TestService1.class)
				.addComponent(srv2, "anySecondId", TestService2.class).addComponent(srv3, TestService3.class);

		final TestSerializable obj = new TestSerializable(component);
		obj.setName("Roland");

		final TestSerializable clone = ctx.deepClone(obj);
		assertEquals("Roland", clone.getName());
		assertSame(component, clone.getSomeComponent());
	}
}
