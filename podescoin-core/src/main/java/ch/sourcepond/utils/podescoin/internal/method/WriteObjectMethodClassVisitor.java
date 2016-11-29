package ch.sourcepond.utils.podescoin.internal.method;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.slf4j.LoggerFactory.getLogger;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.slf4j.Logger;

import ch.sourcepond.utils.podescoin.internal.inspector.Inspector;

public class WriteObjectMethodClassVisitor extends MethodInjectionClassVisitor {
	private static final Logger LOG = getLogger(WriteObjectMethodClassVisitor.class);

	public WriteObjectMethodClassVisitor(final ClassVisitor pVisitor, final Inspector pInspector) {
		super(pVisitor, pInspector);
	}

	@Override
	protected MethodVisitor createInjectionMethodWriter() {
		LOG.debug("{} : create new writeObject method", getClassName());
		return cv.visitMethod(ACC_PRIVATE, WRITE_OBJECT_METHOD_NAME, WRITE_OBJECT_METHOD_DESC, null,
				WRITE_OBJECT_METHOD_EXCEPTIONS);
	}
}
