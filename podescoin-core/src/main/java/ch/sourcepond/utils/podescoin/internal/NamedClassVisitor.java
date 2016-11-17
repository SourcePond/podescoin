/*Copyright (C) 2016 Roland Hauser, <sourcepond@gmail.com>
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package ch.sourcepond.utils.podescoin.internal;

import static org.objectweb.asm.Opcodes.ASM5;

import org.objectweb.asm.ClassVisitor;

public abstract class NamedClassVisitor extends ClassVisitor {
	private String className;
	protected String classInternalName;
	protected String superClassInternalNameOrNull;

	public NamedClassVisitor(final ClassVisitor cv) {
		super(ASM5, cv);
	}

	public final String getClassName() {
		return className;
	}

	public final String getInternalClassName() {
		return classInternalName;
	}

	@Override
	public void visit(final int version, final int access, final String name, final String signature,
			final String superName, final String[] interfaces) {
		classInternalName = name;
		className = toClassName(name);
		superClassInternalNameOrNull = superName;
		super.visit(version, access, name, signature, superName, interfaces);
	}

	public static String toClassName(final String pInternalClassName) {
		return pInternalClassName.replace('/', '.');
	}
}
