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

import java.util.Map;

public class AmbiguousComponentException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static String createMessage(final String pFieldNameOrNull, final int pParameterIndex,
			final Map<String, Object> pAmbiguousComponents) {
		final StringBuilder message = new StringBuilder();
		if (pFieldNameOrNull != null) {
			message.append("There is more than one component which matches field ").append(pFieldNameOrNull);
		} else {
			message.append("There is more than one component which matches parameter at index ")
					.append(pParameterIndex);
		}
		message.append(". Following components have been found:\n");
		for (final Map.Entry<String, Object> entry : pAmbiguousComponents.entrySet()) {
			message.append("\t").append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n");
		}
		return message.toString();
	}

	public AmbiguousComponentException(final String pFieldNameOrNull, final int pParameterIndex,
			final Map<String, Object> pAmbiguousComponents) {
		super(createMessage(pFieldNameOrNull, pParameterIndex, pAmbiguousComponents));
	}
}
