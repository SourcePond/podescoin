package ch.sourcepond.utils.bci.testservice;

import java.util.Date;

public class DateServiceImpl implements DateService {

	@Override
	public Date getDate() {
		return new Date();
	}

}
