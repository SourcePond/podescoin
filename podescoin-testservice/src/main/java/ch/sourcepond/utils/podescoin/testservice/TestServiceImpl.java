package ch.sourcepond.utils.podescoin.testservice;

public class TestServiceImpl implements TestService {
	private final String id;
	
	public TestServiceImpl(final String pId) {
		id = pId;
	}

	@Override
	public String getId() {
		return id;
	}
}
