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

import static org.objectweb.asm.Opcodes.ASM5;

import org.objectweb.asm.AnnotationVisitor;

final class NamedAnnotationOnFieldVisitor extends AnnotationVisitor {
	private final ComponentFieldVisitor fieldVisitor;

	NamedAnnotationOnFieldVisitor(final ComponentFieldVisitor pFieldVisitor, final AnnotationVisitor pDelegate) {
		super(ASM5, pDelegate);
		fieldVisitor = pFieldVisitor;
	}

	@Override
	public void visit(final String name, final Object value) {
		fieldVisitor.setComponentId((String) value);
		super.visit(name, value);
	}
}
