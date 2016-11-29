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
import static ch.sourcepond.utils.podescoin.internal.Constants.READ_OBJECT_ANNOTATION_NAME;
import static ch.sourcepond.utils.podescoin.internal.SerializableClassVisitor.isReadObjectMethod;
import static java.lang.String.format;
import static java.lang.System.arraycopy;
import static org.objectweb.asm.Type.getArgumentTypes;
import static org.objectweb.asm.Type.getType;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import ch.sourcepond.utils.podescoin.api.Recipient;
import ch.sourcepond.utils.podescoin.internal.method.InjectorMethodVisitor;

public final class Inspector extends NamedClassVisitor {
	private static final String[][] EMPTY = new String[0][0];
	private boolean injectionAware;
	private String[][] readNamedComponents;
	private String readInjectorMethodName;
	private String readInjectorMethodDesc;
	private boolean hasObjectInputStream;
	private DefaultReadObjectGenerator defaultReadGenerator;

	public Inspector() {
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
			if (defaultReadGenerator == null && isReadObjectMethod(access, name, desc, exceptions)) {
				defaultReadGenerator = new NoopDefaultReadObjectGenerator();
			}
		}
		return visitor;
	}

	public void addReadNamedComponent(final String pComponentId, final int pParameterIndex) {
		readNamedComponents[hasObjectInputStream ? pParameterIndex - 1 : pParameterIndex][0] = pComponentId;
	}

	public boolean isReadArgumentTypesInitialized() {
		return readNamedComponents != null;
	}

	public String[][] getReadComponents() {
		return readNamedComponents;
	}

	public boolean hasObjectInputStream() {
		return hasObjectInputStream;
	}

	public void initReadObjectArgumentTypes(final boolean pHasObjectInputStream, final String pInjectorMethodName,
			final String pInjectorMethodDesc) {
		if (isReadArgumentTypesInitialized()) {
			throw new AmbiguousInjectorMethodsException(
					format("More than one method detected which is annotated with %s", READ_OBJECT_ANNOTATION_NAME));
		}

		hasObjectInputStream = pHasObjectInputStream;
		readInjectorMethodName = pInjectorMethodName;
		readInjectorMethodDesc = pInjectorMethodDesc;

		Type[] argumentTypes = getArgumentTypes(pInjectorMethodDesc);
		if (hasObjectInputStream) {
			final Type[] reducedArgumentTypes = new Type[argumentTypes.length - 1];
			arraycopy(argumentTypes, 1, reducedArgumentTypes, 0, reducedArgumentTypes.length);
			argumentTypes = reducedArgumentTypes;
		}

		if (argumentTypes.length > 0) {
			readNamedComponents = new String[argumentTypes.length][2];
			for (int i = 0; i < argumentTypes.length; i++) {
				readNamedComponents[i][1] = argumentTypes[i].getClassName();
			}
		} else {
			readNamedComponents = EMPTY;
		}
	}

	public DefaultReadObjectGenerator getDefaultReadGenerator() {
		if (defaultReadGenerator == null) {
			defaultReadGenerator = new DefaultReadObjectVisitor();
		}
		return defaultReadGenerator;
	}

	public String getReadInjectorMethodName() {
		return readInjectorMethodName;
	}

	public String getReadInjectorMethodDesc() {
		return readInjectorMethodDesc;
	}
}
