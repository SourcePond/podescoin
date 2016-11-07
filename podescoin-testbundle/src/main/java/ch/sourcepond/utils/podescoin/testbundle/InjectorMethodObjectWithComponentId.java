package ch.sourcepond.utils.podescoin.testbundle;

import javax.inject.Inject;
import javax.inject.Named;

import ch.sourcepond.utils.podescoin.testservice.AmbiguousDateService;
import ch.sourcepond.utils.podescoin.testservice.AmbiguousNameService;
import ch.sourcepond.utils.podescoin.testservice.TestService;

public class InjectorMethodObjectWithComponentId implements Injected {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public transient AmbiguousNameService nameService;

	public transient AmbiguousDateService dateService;

	@Inject
	public void inject(@Named("testservice.ambiguousName1") final AmbiguousNameService pNameService,
			@Named("testservice.ambiguousDate2") final AmbiguousDateService pDateService) {
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
