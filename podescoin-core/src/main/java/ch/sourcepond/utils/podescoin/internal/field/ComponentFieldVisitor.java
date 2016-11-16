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

import static ch.sourcepond.utils.podescoin.internal.Constants.INJECT_ANNOTATION_NAME;
import static ch.sourcepond.utils.podescoin.internal.Constants.NAMED_ANNOTATION_NAME;
import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Type.getType;
import static org.slf4j.LoggerFactory.getLogger;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.slf4j.Logger;

import ch.sourcepond.utils.podescoin.internal.Access;

public final class ComponentFieldVisitor extends FieldVisitor {
	private static final Logger LOG = getLogger(ComponentFieldVisitor.class);
	private final FieldInjectionClassVisitor classVisitor;
	private final String fieldName;
	private final String fieldType;
	private final int access;
	private String componentIdOrNull;
	private boolean inject;

	ComponentFieldVisitor(final FieldInjectionClassVisitor pClassVisitor, final FieldVisitor pDelegate,
			final String pFieldName, final String pFieldType, final int pAccess) {
		super(ASM5, pDelegate);
		classVisitor = pClassVisitor;
		fieldName = pFieldName;
		fieldType = pFieldType;
		access = pAccess;
	}

	void setComponentId(final String pComponentId) {
		componentIdOrNull = pComponentId;
	}

	@Override
	public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
		if (visible) {
			if (INJECT_ANNOTATION_NAME.equals(getType(desc).getClassName())) {

				if (!Access.isTransient(access) || Access.isFinal(access)) {
					classVisitor.addIllegalField(fieldName, fieldType, access);
				}

				inject = true;
			}
			if (NAMED_ANNOTATION_NAME.equals(getType(desc).getClassName())) {
				return new NamedAnnotationOnFieldVisitor(this, fv.visitAnnotation(desc, visible));
			}
		}
		return super.visitAnnotation(desc, visible);
	}

	@Override
	public void visitEnd() {
		if (inject) {
			LOG.debug("{} : registering injection field {} with id {} and type {}", classVisitor.getClassName(),
					fieldName, componentIdOrNull, fieldType);
			classVisitor.addNamedComponent(fieldName, componentIdOrNull, fieldType);
		}
	}
}
