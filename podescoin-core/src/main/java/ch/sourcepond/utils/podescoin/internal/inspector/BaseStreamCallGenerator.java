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

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import org.objectweb.asm.MethodVisitor;

abstract class BaseStreamCallGenerator implements DefaultStreamCallGenerator {
	private boolean insertDefaultStreamCall = true;

	protected abstract String getStreamInternalName();

	protected abstract String getMethodName();

	protected abstract String getMethodDesc();

	@Override
	public final void visitDefaultStreamCall(final MethodVisitor pVisitor) {
		if (insertDefaultStreamCall) {
			pVisitor.visitVarInsn(ALOAD, 1);
			pVisitor.visitMethodInsn(INVOKEVIRTUAL, getStreamInternalName(), getMethodName(), getMethodDesc(), false);
			insertDefaultStreamCall = false;
		}
	}
}
