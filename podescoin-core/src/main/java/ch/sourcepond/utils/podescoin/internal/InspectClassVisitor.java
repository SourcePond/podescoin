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

import static ch.sourcepond.utils.podescoin.internal.Constants.CONSTRUCTOR_NAME;
import static ch.sourcepond.utils.podescoin.internal.Constants.INJECT_ANNOTATION_NAME;
import static java.lang.String.format;
import static java.lang.System.arraycopy;
import static org.objectweb.asm.Type.getArgumentTypes;
import static org.objectweb.asm.Type.getType;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import ch.sourcepond.utils.podescoin.Recipient;
import ch.sourcepond.utils.podescoin.internal.method.InjectorMethodVisitor;

public final class InspectClassVisitor extends NamedClassVisitor {
	private static final String[][] EMPTY = new String[0][0];
	private boolean injectionAware;
	private String[][] namedComponents;
	private String injectorMethodName;
	private String injectorMethodDesc;
	private boolean hasObjectInputStream;

	public InspectClassVisitor() {
		super(null);
	}

	public boolean isInjectionAware() {
		return injectionAware;
	}

	@Override
	public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
		if (!injectionAware) {
			injectionAware = Recipient.class.getName().equals(getType(desc).getClassName());
		}
		return super.visitAnnotation(desc, visible);
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
			final String[] exceptions) {
		MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
		if (injectionAware) {
			if (!CONSTRUCTOR_NAME.equals(name)) {
				visitor = new InjectorMethodVisitor(this, visitor, classInternalName, superClassInternalNameOrNull,
						name, desc);
			}
		}
		return visitor;
	}

	public void addNamedComponent(final String pComponentId, final int pParameterIndex) {
		namedComponents[hasObjectInputStream ? pParameterIndex - 1 : pParameterIndex][0] = pComponentId;
	}

	public boolean isArgumentTypesInitialized() {
		return namedComponents != null;
	}

	public String[][] getComponents() {
		return namedComponents;
	}

	public boolean hasObjectInputStream() {
		return hasObjectInputStream;
	}

	public void initArgumentTypes(final boolean pHasObjectInputStream, final String pInjectorMethodName,
			final String pInjectorMethodDesc) {
		if (isArgumentTypesInitialized()) {
			throw new AmbiguousInjectorMethodsException(
					format("More than one method detected which is annotated with %s", INJECT_ANNOTATION_NAME));
		}

		hasObjectInputStream = pHasObjectInputStream;
		injectorMethodName = pInjectorMethodName;
		injectorMethodDesc = pInjectorMethodDesc;

		Type[] argumentTypes = getArgumentTypes(pInjectorMethodDesc);
		if (hasObjectInputStream) {
			final Type[] reducedArgumentTypes = new Type[argumentTypes.length - 1];
			arraycopy(argumentTypes, 1, reducedArgumentTypes, 0, reducedArgumentTypes.length);
			argumentTypes = reducedArgumentTypes;
		}

		if (argumentTypes.length > 0) {
			namedComponents = new String[argumentTypes.length][2];
			for (int i = 0; i < argumentTypes.length; i++) {
				namedComponents[i][1] = argumentTypes[i].getClassName();
			}
		} else {
			namedComponents = EMPTY;
		}
	}

	public String getInjectorMethodName() {
		return injectorMethodName;
	}

	public String getInjectorMethodDesc() {
		return injectorMethodDesc;
	}
}
