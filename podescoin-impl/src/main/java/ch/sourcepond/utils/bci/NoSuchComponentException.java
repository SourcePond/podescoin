package ch.sourcepond.utils.bci;

import static java.lang.String.format;

public class NoSuchComponentException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static String createMessage(final String pFieldName, final int pParameterIndex, final Class<?> pFieldType) {
		if (pFieldName != null) {
			return format("No component found which matches field '%s' with type '%s'", pFieldName,
					pFieldType.getName());
		}
		return format("No component found which matches parameter at index %d with type '%s'", pParameterIndex,
				pFieldType.getName());
	}

	public NoSuchComponentException(final String pFieldName, final int pParameterIndex, final Class<?> pFieldType) {
		super(createMessage(pFieldName, pParameterIndex, pFieldType));
	}
}
