package ch.sourcepond.utils.bci;

import org.osgi.framework.Bundle;

/**
 * @author rolandhauser
 *
 */
class BundleInjectorFactory {

	BundleInjector newInjector(final Bundle pBundle) {
		return new BundleInjector(pBundle);
	}
}
