package ch.sourcepond.utils.bci;

import static java.lang.String.format;

public class NoSuchComponentException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NoSuchComponentException(final String pFieldName, final Class<?> pFieldType) {
		super(format("No component found which matches field '%s' with type '%s'", pFieldName, pFieldType.getName()));
	}
}
