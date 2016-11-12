package ch.sourcepond.utils.podescoin;

import java.io.Serializable;

import org.osgi.framework.Bundle;

/**
 *
 */
final class BundleDetectorImpl implements BundleDetector {

	@Override
	public Bundle getBundle(Class<? extends Serializable> pClass) {
		return getBundle(pClass);
	}

}
