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
package ch.sourcepond.utils.podescoin.internal.inspector;

import static ch.sourcepond.utils.podescoin.internal.Constants.NAMED_ANNOTATION_NAME;
import static ch.sourcepond.utils.podescoin.internal.NamedClassVisitor.toClassName;
import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Type.getArgumentTypes;
import static org.objectweb.asm.Type.getType;
import static org.slf4j.LoggerFactory.getLogger;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;

import ch.sourcepond.utils.podescoin.internal.method.SuperMethodInvokationException;

/**
 * @author rolandhauser
 *
 */
final class InjectorMethodInspector extends MethodVisitor {
	private static final Logger LOG = getLogger(InjectorMethodInspector.class);
	private InjectorMethodArgumentTypesInitializer initializer;
	private NamedComponentRegistration namedComponentRegistration;
	private Class<?> objectStreamClass;
	private String classInternalName;
	private String superClassInternalNameOrNull;
	private String injectorMethodName;
	private String injectorMethodDesc;
	private String injectorMethodAnnotationName;
	private boolean injectorMethodDetected;

	public InjectorMethodInspector(final MethodVisitor mv) {
		super(ASM5, mv);
	}

	public void setInitializer(final InjectorMethodArgumentTypesInitializer initializer) {
		this.initializer = initializer;
	}

	public void setNamedComponentRegistration(final NamedComponentRegistration namedComponentRegistration) {
		this.namedComponentRegistration = namedComponentRegistration;
	}

	public void setObjectStreamClass(final Class<?> objectStreamClass) {
		this.objectStreamClass = objectStreamClass;
	}

	public void setClassInternalName(final String classInternalName) {
		this.classInternalName = classInternalName;
	}

	public void setSuperClassInternalNameOrNull(final String superClassInternalNameOrNull) {
		this.superClassInternalNameOrNull = superClassInternalNameOrNull;
	}

	public void setInjectorMethodName(final String injectorMethodName) {
		this.injectorMethodName = injectorMethodName;
	}

	public void setInjectorMethodDesc(final String injectorMethodDesc) {
		this.injectorMethodDesc = injectorMethodDesc;
	}

	public void setInjectorMethodAnnotationName(final String injectorMethodAnnotationName) {
		this.injectorMethodAnnotationName = injectorMethodAnnotationName;
	}

	public void setInjectorMethodDetected(final boolean injectorMethodDetected) {
		this.injectorMethodDetected = injectorMethodDetected;
	}

	@Override
	public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
		if (!injectorMethodDetected) {
			injectorMethodDetected = injectorMethodAnnotationName.equals(getType(desc).getClassName());
		}

		if (visible && injectorMethodDetected) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("{} : {} : added with descriptor {}", classInternalName.replace('/', '.'), injectorMethodName,
						injectorMethodDesc);
			}
			initializer.initArgumentTypes(includeObjectStream(), injectorMethodName, injectorMethodDesc);
		}
		return super.visitAnnotation(desc, visible);
	}

	@Override
	public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
		if (injectorMethodDetected && NAMED_ANNOTATION_NAME.equals(getType(desc).getClassName())) {
			return new NamedAnnotationOnParameterVisitor(this, super.visitParameterAnnotation(parameter, desc, visible),
					parameter);
		}
		return super.visitParameterAnnotation(parameter, desc, visible);
	}

	@Override
	public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc,
			final boolean itf) {
		if (Opcodes.INVOKESPECIAL == opcode && injectorMethodDetected && owner.equals(superClassInternalNameOrNull)
				&& name.equals(injectorMethodName) && desc.equals(injectorMethodDesc)) {
			final StringBuilder errorMessage = new StringBuilder("Failed to enhance ")
					.append(toClassName(classInternalName)).append("\n")
					.append(String.format("Injector method '%s' is not allowed to call 'super.%s'", name, name))
					.append("\nMethod descriptor: ").append(desc).append("\n");
			throw new SuperMethodInvokationException(errorMessage.toString());
		}
		super.visitMethodInsn(opcode, owner, name, desc, false);
	}

	private boolean includeObjectStream() {
		boolean includeObjectStream = false;
		final Type[] argumentTypes = getArgumentTypes(injectorMethodDesc);
		if (argumentTypes.length > 0) {
			includeObjectStream = objectStreamClass.getName().equals(argumentTypes[0].getClassName());
		}
		return includeObjectStream;
	}

	void setComponentId(final String pComponentId, final int pParameterIndex) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("{} : {} : use component-id {} for parameter index {}", classInternalName.replace('/', '.'),
					injectorMethodName, pComponentId, pParameterIndex);
		}
		namedComponentRegistration.registerNamedComponent(pComponentId, pParameterIndex);
	}
}
