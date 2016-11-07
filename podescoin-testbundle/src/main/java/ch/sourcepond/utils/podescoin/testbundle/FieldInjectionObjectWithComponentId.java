package ch.sourcepond.utils.podescoin.testbundle;

import javax.inject.Inject;
import javax.inject.Named;

import ch.sourcepond.utils.podescoin.testservice.AmbiguousDateService;
import ch.sourcepond.utils.podescoin.testservice.AmbiguousNameService;
import ch.sourcepond.utils.podescoin.testservice.TestService;

/**
 *
 */
public class FieldInjectionObjectWithComponentId implements Injected {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Inject
	@Named("testservice.ambiguousName1")
	private transient AmbiguousNameService nameService;

	@Inject
	@Named("testservice.ambiguousDate2")
	private transient AmbiguousDateService dateService;
	
	@Override
	public TestService getDateService() {
		return dateService;
	}

	@Override
	public TestService getNameService() {
		return nameService;
	}
}
