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

import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Type.getDescriptor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;

import ch.sourcepond.utils.podescoin.api.Component;

final class IsComponentFieldVisitor extends FieldVisitor {
	private final String COMPONENT_DESC = getDescriptor(Component.class);
	private final ReadObjectInspector inspector;

	public IsComponentFieldVisitor(final ReadObjectInspector pInspector, final FieldVisitor pVisitor) {
		super(ASM5, pVisitor);
		inspector = pInspector;
	}

	@Override
	public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
		if (COMPONENT_DESC.equals(desc)) {
			inspector.componentFieldFound();
		}
		return super.visitAnnotation(desc, visible);
	}
}
