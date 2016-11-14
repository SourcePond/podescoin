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
