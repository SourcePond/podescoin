package ch.sourcepond.utils.podescoin;

import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import ch.sourcepond.utils.podescoin.AmbiguousComponentException;

public class AmbiguousComponentExceptionTest {

	@Test
	public void verifyMessage() {
		final Map<String, Object> ambiguousComponents = new LinkedHashMap<>();
		ambiguousComponents.put("componentId1", new Object());
		ambiguousComponents.put("componentId2", new Object());
		final AmbiguousComponentException ex = new AmbiguousComponentException("anyField", 0, ambiguousComponents);
		assertTrue(ex.getMessage().startsWith(
				"There is more than one component which matches field anyField. Following components have been found:\n"));
		assertTrue(ex.getMessage().contains("\tcomponentId1 -> java.lang.Object@"));
		assertTrue(ex.getMessage().contains("\tcomponentId2 -> java.lang.Object@"));
	}
}
