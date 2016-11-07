package ch.sourcepond.utils.podescoin;

import org.osgi.framework.Bundle;

import ch.sourcepond.utils.podescoin.internal.BundleInjector;

/**
 * @author rolandhauser
 *
 */
class BundleInjectorFactory {

	BundleInjector newInjector(final Bundle pBundle) {
		return new BundleInjectorImpl(pBundle);
	}
}
