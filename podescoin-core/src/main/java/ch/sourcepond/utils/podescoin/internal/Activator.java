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
package ch.sourcepond.utils.podescoin.internal;

import static ch.sourcepond.utils.podescoin.internal.Transformer.transform;
import static org.osgi.framework.hooks.weaving.WovenClass.DEFINED;
import static org.osgi.framework.hooks.weaving.WovenClass.TRANSFORMING;

import java.io.Serializable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.hooks.weaving.WeavingException;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;
import org.osgi.framework.hooks.weaving.WovenClassListener;

import ch.sourcepond.utils.podescoin.Injector;
import ch.sourcepond.utils.podescoin.Recipient;

public final class Activator implements BundleActivator, WeavingHook, WovenClassListener {
	private BundleContext context;

	@Override
	public void start(final BundleContext context) throws Exception {
		this.context = context;
		context.registerService(new String[] { WeavingHook.class.getName(), WovenClassListener.class.getName() }, this,
				null);
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		// noop; service un-registration is done automatically
	}

	private boolean isAllowed(final WovenClass wovenClass) {
		return !context.getBundle().equals(wovenClass.getBundleWiring().getBundle());
	}

	@Override
	public void weave(final WovenClass wovenClass) {
		if (wovenClass.getState() == TRANSFORMING && isAllowed(wovenClass)) {
			try {
				wovenClass.setBytes(transform(wovenClass.getBytes()));
				wovenClass.getDynamicImports().add(Injector.class.getPackage().getName());
			} catch (final Throwable e) {
				throw new WeavingException(String.format("Enhancement of %s failed!", wovenClass.getClassName()), e);
			}
		}
	}

	@Override
	public void modified(final WovenClass wovenClass) {
		if (wovenClass.getState() == DEFINED) {
			final Class<?> cl = wovenClass.getDefinedClass();
			if (cl.isAnnotationPresent(Recipient.class) && !Serializable.class.isAssignableFrom(cl)) {
				throw new UnserializableClassWarning(cl);
			}
		}
	}
}
