package ch.sourcepond.utils.podescoin.testbundle;

import javax.inject.Inject;

import ch.sourcepond.utils.podescoin.testservice.DateService;
import ch.sourcepond.utils.podescoin.testservice.NameService;
import ch.sourcepond.utils.podescoin.testservice.TestService;

public class InjectorMethodObject implements Injected {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public transient NameService nameService;
	
	public transient DateService dateService;

	@Inject
	public void inject(final NameService pNameService, final DateService pDateService) {
		nameService = pNameService;
		dateService = pDateService;
	}

	@Override
	public TestService getDateService() {
		return dateService;
	}

	@Override
	public TestService getNameService() {
		return nameService;
	}
}
