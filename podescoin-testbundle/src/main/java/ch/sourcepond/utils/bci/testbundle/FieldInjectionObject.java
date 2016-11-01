package ch.sourcepond.utils.bci.testbundle;

import java.io.Serializable;

import javax.inject.Inject;

import ch.sourcepond.utils.bci.testservice.DateService;
import ch.sourcepond.utils.bci.testservice.NameService;

public class FieldInjectionObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Inject
	public transient NameService nameService;
	
	@Inject
	public transient DateService dateService;
}
