package ch.sourcepond.utils.bci;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

public class AmbiguousComponentExceptionTest {

	@Test
	public void verifyMessage() {
		final Map<String, Object> ambiguousComponents = new LinkedHashMap<>();
		ambiguousComponents.put("componentId1", new Object());
		ambiguousComponents.put("componentId2", new Object());
		final AmbiguousComponentException ex = new AmbiguousComponentException("anyField", ambiguousComponents);
		assertEquals(
				"There are more than one component which match field anyField. Following components have been found:\n"
						+ "\tcomponentId1 -> class java.lang.Object\n" + "\tcomponentId2 -> class java.lang.Object\n",
				ex.getMessage());
	}
}
