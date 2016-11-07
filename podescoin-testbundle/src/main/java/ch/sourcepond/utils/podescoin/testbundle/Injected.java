package ch.sourcepond.utils.podescoin.testbundle;

import java.io.Serializable;

import ch.sourcepond.utils.podescoin.testservice.TestService;

public interface Injected extends Serializable {

	TestService getDateService();
	
	TestService getNameService();
}
