package ch.sourcepond.utils.podescoin;

import org.objectweb.asm.ClassVisitor;

public interface TestClassVisitorFactory {

	ClassVisitor newClassVisitor();
}
