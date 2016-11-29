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

import org.objectweb.asm.AnnotationVisitor;

public class NamedAnnotationOnParameterVisitor extends AnnotationVisitor {
	private final InjectorMethodInspector methodVisitor;
	private final int parameterIndex;

	NamedAnnotationOnParameterVisitor(final InjectorMethodInspector pMethodVisitor,
			final AnnotationVisitor pDelegate, final int pParameterIndex) {
		super(ASM5, pDelegate);
		methodVisitor = pMethodVisitor;
		parameterIndex = pParameterIndex;
	}

	@Override
	public void visit(final String name, final Object value) {
		methodVisitor.setComponentId((String) value, parameterIndex);
		super.visit(name, value);
	}
}
