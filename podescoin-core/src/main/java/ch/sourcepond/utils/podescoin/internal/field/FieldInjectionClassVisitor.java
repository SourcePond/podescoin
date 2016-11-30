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
package ch.sourcepond.utils.podescoin.internal.field;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Type.getType;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.slf4j.Logger;

import ch.sourcepond.utils.podescoin.IllegalFieldDeclarationException;
import ch.sourcepond.utils.podescoin.internal.Access;
import ch.sourcepond.utils.podescoin.internal.Enhancer;
import ch.sourcepond.utils.podescoin.internal.SerializableClassVisitor;
import ch.sourcepond.utils.podescoin.internal.inspector.DefaultStreamCallGenerator;
import ch.sourcepond.utils.podescoin.internal.inspector.Inspector;

public final class FieldInjectionClassVisitor extends SerializableClassVisitor {
	private static final Logger LOG = getLogger(FieldInjectionClassVisitor.class);
	private List<String> illegalFields;
	private List<String[]> namedComponents;

	public FieldInjectionClassVisitor(final Inspector pInspector, final ClassVisitor pWriter) {
		super(pInspector, pWriter);
	}

	public void addIllegalField(final String fieldName, final String fieldType, final int access) {
		if (illegalFields == null) {
			illegalFields = new LinkedList<>();
		}

		final StringBuilder builder = new StringBuilder();
		if (Access.isPrivate(access)) {
			builder.append("private ");
		} else if (Access.isProtected(access)) {
			builder.append("protected ");
		} else if (Access.isPublic(access)) {
			builder.append("public ");
		}

		if (Access.isTransient(access)) {
			builder.append("transient ");
		}
		if (Access.isVolatile(access)) {
			builder.append("volatile ");
		}
		if (Access.isFinal(access)) {
			builder.append("final ");
		}

		illegalFields.add(builder.append(fieldType).append(" ").append(fieldName).toString());
	}

	@Override
	public FieldVisitor visitField(final int access, final String name, final String desc, final String signature,
			final Object value) {
		return new ComponentFieldVisitor(this, cv.visitField(access, name, desc, signature, value), name,
				getType(desc).getClassName(), access);
	}

	@Override
	protected boolean isEnhancementNecessary() {
		return namedComponents != null;
	}

	public void addNamedComponent(final String pFieldName, final String pComponentIdOrNull, final String pTypeName) {
		if (namedComponents == null) {
			namedComponents = new LinkedList<>();
		}
		namedComponents.add(new String[] { pFieldName, pComponentIdOrNull, pTypeName });
	}

	@Override
	protected Enhancer createInjectionMethodVisitor(final MethodVisitor pWriter, final boolean pEnhanceMode,
			final DefaultStreamCallGenerator pDefaultReadGenerator) {
		if (illegalFields != null) {
			final StringBuilder errorMessage = new StringBuilder("Failed to enhance ").append(inspector.getClassName())
					.append("\n").append("Injectable fields must be transient and non-final! Illegal declarations:\n");
			for (final String illegalField : illegalFields) {
				errorMessage.append("\t").append(illegalField).append("\n");
			}

			throw new IllegalFieldDeclarationException(errorMessage.toString());
		}

		final FieldInjectionEnhancer visitor = new FieldInjectionEnhancer(pEnhanceMode, pDefaultReadGenerator, pWriter);
		visitor.setNamedComponents(namedComponents);
		return visitor;
	}

	@Override
	protected MethodVisitor createInjectionMethodWriter() {
		LOG.debug("{} : create new readObject method", getClassName());
		return cv.visitMethod(ACC_PRIVATE, READ_OBJECT_METHOD_NAME, READ_OBJECT_METHOD_DESC, null,
				READ_OBJECT_METHOD_EXCEPTIONS);
	}

	@Override
	protected boolean isInjectorMethod(final int access, final String name, final String desc,
			final String[] exceptions) {
		return isReadObjectMethod(access, name, desc, exceptions);
	}
}
