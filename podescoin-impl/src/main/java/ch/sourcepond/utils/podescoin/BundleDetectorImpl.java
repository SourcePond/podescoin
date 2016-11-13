package ch.sourcepond.utils.podescoin;

import java.io.Serializable;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 *
 */
final class BundleDetectorImpl implements BundleDetector {

	@Override
	public Bundle getBundle(final Class<? extends Serializable> pClass) {
		return FrameworkUtil.getBundle(pClass);
	}

}
