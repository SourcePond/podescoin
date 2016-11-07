package ch.sourcepond.utils.podescoin.testbundle;

import javax.inject.Inject;

import ch.sourcepond.utils.podescoin.testservice.DateService;
import ch.sourcepond.utils.podescoin.testservice.NameService;
import ch.sourcepond.utils.podescoin.testservice.TestService;

public class FieldInjectionObject implements Injected {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Inject
	private transient NameService nameService;
	
	@Inject
	private transient DateService dateService;

	@Override
	public TestService getDateService() {
		return dateService;
	}

	@Override
	public TestService getNameService() {
		return nameService;
	}

}
