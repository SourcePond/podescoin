/*Copyright (C) 2016 Roland Hauser, <sourcepond@gmail.com>
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package ch.sourcepond.utils.podescoin.testbundle;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import ch.sourcepond.utils.podescoin.api.ReadObject;
import ch.sourcepond.utils.podescoin.api.Recipient;
import ch.sourcepond.utils.podescoin.api.WriteObject;
import ch.sourcepond.utils.podescoin.testservice.DateService;
import ch.sourcepond.utils.podescoin.testservice.NameService;
import ch.sourcepond.utils.podescoin.testservice.TestService;

@Recipient
public class InjectorMethodObject implements DataTransferInclInjection {
	public static final String TEST_PREFIX = "test_";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public transient NameService nameService;

	public transient DateService dateService;

	private transient String transferredNameServiceId;
	private transient String transferredDateServiceId;

	@ReadObject
	public void readObject(final ObjectInputStream in, final NameService pNameService, final DateService pDateService)
			throws IOException {
		nameService = pNameService;
		dateService = pDateService;

		transferredNameServiceId = in.readUTF();
		transferredDateServiceId = in.readUTF();
	}

	@WriteObject
	public void writeObject(final ObjectOutputStream out, final NameService pNameService,
			final DateService pDateService) throws IOException {
		out.writeUTF(TEST_PREFIX + pNameService.getId());
		out.writeUTF(TEST_PREFIX + pDateService.getId());
	}

	@Override
	public TestService getDateService() {
		return dateService;
	}

	@Override
	public TestService getNameService() {
		return nameService;
	}

	@Override
	public String getTransferredNameServiceId() {
		return transferredNameServiceId;
	}

	@Override
	public String getTransferredDateServiceId() {
		return transferredDateServiceId;
	}
}
