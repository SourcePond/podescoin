package ch.sourcepond.utils.podescoin.internal;

import static org.objectweb.asm.Opcodes.ASM5;

import org.objectweb.asm.ClassVisitor;

abstract class NamedClassVisitor extends ClassVisitor {
	private String className;
	private String classInternalName;
	
	public NamedClassVisitor(ClassVisitor cv) {
		super(ASM5, cv);
	}
	
	final String getClassName() {
		return className;
	}
	
	final String getInternalClassName() {
		return classInternalName;
	}
	
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		classInternalName = name;
		className = name.replace('/', '.');
		super.visit(version, access, name, signature, superName, interfaces);
	}
}
