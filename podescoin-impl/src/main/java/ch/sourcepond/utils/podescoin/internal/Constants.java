package ch.sourcepond.utils.podescoin.internal;

import static org.objectweb.asm.Type.getInternalName;

import javax.inject.Inject;
import javax.inject.Named;

import ch.sourcepond.utils.podescoin.Injector;

final class Constants {
	static final String INJECT_ANNOTATION_NAME = Inject.class.getName();
	static final String NAMED_ANNOTATION_NAME = Named.class.getName();
	static final String INJECTOR_INTERNAL_NAME = getInternalName(Injector.class);
	static final String CONSTRUCTOR_NAME = "<init>";
}
