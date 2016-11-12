package ch.sourcepond.utils.podescoin;

import java.io.Serializable;

import org.osgi.framework.Bundle;

/**
 * @author rolandhauser
 *
 */
interface BundleDetector {

	Bundle getBundle(Class<? extends Serializable> pClass);
}
