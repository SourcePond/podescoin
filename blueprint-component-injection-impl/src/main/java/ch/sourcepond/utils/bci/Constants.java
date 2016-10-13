package ch.sourcepond.utils.bci;

import static org.objectweb.asm.Type.getInternalName;

import javax.inject.Inject;
import javax.inject.Named;

final class Constants {
	static final String INJECT_ANNOTATION_NAME = Inject.class.getName();
	static final String NAMED_ANNOTATION_NAME = Named.class.getName();
	static final String INJECTOR_INTERNAL_NAME = getInternalName(Injector.class);
}
