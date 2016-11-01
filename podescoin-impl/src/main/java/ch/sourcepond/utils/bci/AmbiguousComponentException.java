package ch.sourcepond.utils.bci;

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
