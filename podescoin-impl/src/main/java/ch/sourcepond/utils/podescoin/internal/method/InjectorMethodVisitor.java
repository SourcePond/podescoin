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

import static ch.sourcepond.utils.podescoin.internal.Constants.INJECT_ANNOTATION_NAME;
import static ch.sourcepond.utils.podescoin.internal.Constants.NAMED_ANNOTATION_NAME;
import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Type.getArgumentTypes;
import static org.objectweb.asm.Type.getType;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.ObjectInputStream;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.slf4j.Logger;

final class InjectorMethodVisitor extends MethodVisitor {
	private static final Logger LOG = getLogger(InjectorMethodVisitor.class);
	private final InspectForInjectorMethodClassVisitor classVisitor;
	private final String injectorMethodName;
	private final String injectorMethodDesc;

	public InjectorMethodVisitor(final InspectForInjectorMethodClassVisitor pClassVisitor, final MethodVisitor mv,
			final String pInjectorMethodName, final String pInjectorMethodDesc) {
		super(ASM5, mv);
		classVisitor = pClassVisitor;
		injectorMethodName = pInjectorMethodName;
		injectorMethodDesc = pInjectorMethodDesc;
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
		if (visible && INJECT_ANNOTATION_NAME.equals(getType(desc).getClassName())) {
			LOG.debug("{} : {} : added with descriptor {}", classVisitor.getClassName(), injectorMethodName, injectorMethodDesc);
			classVisitor.initArgumentTypes(includeObjectInputStream(), injectorMethodName, injectorMethodDesc);
		}
		return super.visitAnnotation(desc, visible);
	}

	@Override
	public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
		if (classVisitor.isArgumentTypesInitialized()) {
			if (NAMED_ANNOTATION_NAME.equals(getType(desc).getClassName())) {
				return new NamedAnnotationOnParameterVisitor(this,
						super.visitParameterAnnotation(parameter, desc, visible), parameter);
			}
		}
		return super.visitParameterAnnotation(parameter, desc, visible);
	}

	private boolean includeObjectInputStream() {
		boolean includeObjectInputStream = false;
		final Type[] argumentTypes = getArgumentTypes(injectorMethodDesc);
		if (argumentTypes.length > 0) {
			includeObjectInputStream = ObjectInputStream.class.getName().equals(argumentTypes[0].getClassName());
		}
		return includeObjectInputStream;
	}
	
	void setComponentId(final String pComponentId, final int pParameterIndex) {
		LOG.debug("{} : {} : use component-id {} for parameter index {}", classVisitor.getClassName(),
				injectorMethodName, pComponentId, pParameterIndex);
		classVisitor.addNamedComponent(pComponentId, pParameterIndex);
	}
}
