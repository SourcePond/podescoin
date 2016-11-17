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
package ch.sourcepond.utils.podescoin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javax.inject.Named;

import org.osgi.service.blueprint.container.BlueprintContainer;

final class BlueprintContainerHandler implements InvocationHandler {
	private final BlueprintContainer delegate;

	public BlueprintContainerHandler(final BlueprintContainer pDelegate) {
		delegate = pDelegate;
	}

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		final Object result = method.invoke(delegate, args);
		if (result == null && args.length == 1) {
			throw new AssertionError(String.format(
					"\nInvocation of %s on %s with arguments %s returned null!\nYou may add following annotation to the appropriate mock:\n%s(\"%s\")",
					method.getName(), delegate, args[0], Named.class.getName(), args[0]));
		}
		return result;
	}

}