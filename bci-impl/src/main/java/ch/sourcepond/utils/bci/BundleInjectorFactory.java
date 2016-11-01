package ch.sourcepond.utils.bci;

import org.osgi.framework.Bundle;

import ch.sourcepond.utils.bci.internal.BundleInjector;

/**
 * @author rolandhauser
 *
 */
class BundleInjectorFactory {

	BundleInjector newInjector(final Bundle pBundle) {
		return new BundleInjectorImpl(pBundle);
	}
}
