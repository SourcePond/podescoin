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

import static ch.sourcepond.utils.podescoin.internal.Constants.CONSTRUCTOR_NAME;
import static ch.sourcepond.utils.podescoin.internal.Constants.INJECT_ANNOTATION_NAME;
import static java.lang.String.format;
import static java.lang.System.arraycopy;
import static org.objectweb.asm.Type.getArgumentTypes;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import ch.sourcepond.utils.podescoin.internal.NamedClassVisitor;

public final class InspectForInjectorMethodClassVisitor extends NamedClassVisitor {
	private static final String[][] EMPTY = new String[0][0];
	private String[][] namedComponents;
	private String injectorMethodName;
	private String injectorMethodDesc;
	private boolean hasObjectInputStream;

	public InspectForInjectorMethodClassVisitor() {
		super(null);
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
			final String[] exceptions) {
		if (!CONSTRUCTOR_NAME.equals(name)) {
			return new InjectorMethodVisitor(this, super.visitMethod(access, name, desc, signature, exceptions), name,
					desc);
		}
		return super.visitMethod(access, name, desc, signature, exceptions);
	}

	void addNamedComponent(final String pComponentId, final int pParameterIndex) {
		namedComponents[hasObjectInputStream ? pParameterIndex - 1 : pParameterIndex][0] = pComponentId;
	}

	boolean isArgumentTypesInitialized() {
		return namedComponents != null;
	}

	String[][] getComponents() {
		return namedComponents;
	}

	boolean hasObjectInputStream() {
		return hasObjectInputStream;
	}

	void initArgumentTypes(final boolean pHasObjectInputStream, final String pInjectorMethodName,
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
