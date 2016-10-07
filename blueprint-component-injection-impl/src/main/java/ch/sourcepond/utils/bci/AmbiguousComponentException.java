package ch.sourcepond.utils.bci;

import java.util.Map;

public class AmbiguousComponentException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static String createMessage(final String pFieldName, final Map<String, Object> pAmbiguousComponents) {
		final StringBuilder builder = new StringBuilder("There are more than one component which match field ")
				.append(pFieldName).append(". Following components have been found:\n");
		for (final Map.Entry<String, Object> entry : pAmbiguousComponents.entrySet()) {
			builder.append("\t").append(entry.getKey()).append(" -> ").append(entry.getValue().getClass()).append("\n");
		}
		return builder.toString();
	}

	public AmbiguousComponentException(final String pFieldName, final Map<String, Object> pAmbiguousComponents) {
		super(createMessage(pFieldName, pAmbiguousComponents));
	}
}
