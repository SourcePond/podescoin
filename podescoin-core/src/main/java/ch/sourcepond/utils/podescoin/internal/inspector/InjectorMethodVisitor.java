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
package ch.sourcepond.utils.podescoin.internal.method;

import static ch.sourcepond.utils.podescoin.internal.Constants.NAMED_ANNOTATION_NAME;
import static ch.sourcepond.utils.podescoin.internal.Constants.READ_OBJECT_ANNOTATION_NAME;
import static ch.sourcepond.utils.podescoin.internal.NamedClassVisitor.toClassName;
import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Type.getArgumentTypes;
import static org.objectweb.asm.Type.getType;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.ObjectInputStream;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;

import ch.sourcepond.utils.podescoin.internal.Inspector;

public final class InjectorMethodVisitor extends MethodVisitor {
	private static final Logger LOG = getLogger(InjectorMethodVisitor.class);
	private final Inspector classVisitor;
	private final String classInternalName;
	private final String superClassInternalNameOrNull;
	private final String readInjectorMethodName;
	private final String readInjectorMethodDesc;
	private boolean isReadInjectorMethod;

	public InjectorMethodVisitor(final Inspector pClassVisitor, final MethodVisitor mv, final String pClassName,
			final String pSuperClassNameOrNull, final String pInjectorMethodName, final String pInjectorMethodDesc) {
		super(ASM5, mv);
		classVisitor = pClassVisitor;
		classInternalName = pClassName;
		superClassInternalNameOrNull = pSuperClassNameOrNull;
		readInjectorMethodName = pInjectorMethodName;
		readInjectorMethodDesc = pInjectorMethodDesc;
	}

	@Override
	public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
		if (!isReadInjectorMethod) {
			isReadInjectorMethod = READ_OBJECT_ANNOTATION_NAME.equals(getType(desc).getClassName());
		}

		if (visible && isReadInjectorMethod) {
			LOG.debug("{} : {} : added with descriptor {}", classVisitor.getClassName(), readInjectorMethodName,
					readInjectorMethodDesc);
			classVisitor.initReadObjectArgumentTypes(includeObjectInputStream(), readInjectorMethodName,
					readInjectorMethodDesc);
		}
		return super.visitAnnotation(desc, visible);
	}

	@Override
	public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
		if (classVisitor.isReadArgumentTypesInitialized()) {
			if (NAMED_ANNOTATION_NAME.equals(getType(desc).getClassName())) {
				return new NamedAnnotationOnParameterVisitor(this,
						super.visitParameterAnnotation(parameter, desc, visible), parameter);
			}
		}
		return super.visitParameterAnnotation(parameter, desc, visible);
	}

	@Override
	public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc,
			final boolean itf) {
		if (Opcodes.INVOKESPECIAL == opcode && isReadInjectorMethod && owner.equals(superClassInternalNameOrNull)
				&& name.equals(readInjectorMethodName) && desc.equals(readInjectorMethodDesc)) {
			final StringBuilder errorMessage = new StringBuilder("Failed to enhance ")
					.append(toClassName(classInternalName)).append("\n")
					.append(String.format("Injector method '%s' is not allowed to call 'super.%s'", name, name))
					.append("\nMethod descriptor: ").append(desc).append("\n");
			throw new SuperMethodInvokationException(errorMessage.toString());
		}
		super.visitMethodInsn(opcode, owner, name, desc, false);
	}

	private boolean includeObjectInputStream() {
		boolean includeObjectInputStream = false;
		final Type[] argumentTypes = getArgumentTypes(readInjectorMethodDesc);
		if (argumentTypes.length > 0) {
			includeObjectInputStream = ObjectInputStream.class.getName().equals(argumentTypes[0].getClassName());
		}
		return includeObjectInputStream;
	}

	void setComponentId(final String pComponentId, final int pParameterIndex) {
		LOG.debug("{} : {} : use component-id {} for parameter index {}", classVisitor.getClassName(),
				readInjectorMethodName, pComponentId, pParameterIndex);
		classVisitor.addReadNamedComponent(pComponentId, pParameterIndex);
	}
}
