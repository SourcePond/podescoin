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
