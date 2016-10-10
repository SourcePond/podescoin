package ch.sourcepond.utils.bci;

import static org.junit.Assert.assertTrue;

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
		assertTrue(ex.getMessage().startsWith(
				"There are more than one component which match field anyField. Following components have been found:\n"));
		assertTrue(ex.getMessage().contains("\tcomponentId1 -> java.lang.Object@"));
		assertTrue(ex.getMessage().contains("\tcomponentId2 -> java.lang.Object@"));
	}
}
