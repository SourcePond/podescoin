package ch.sourcepond.utils.bci;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Named;

public class TestObject implements Serializable {

	@Inject
	@Named("anyComponent")
	private transient TestComponent component;
}
