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

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.slf4j.LoggerFactory.getLogger;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.slf4j.Logger;

import ch.sourcepond.utils.podescoin.internal.Enhancer;
import ch.sourcepond.utils.podescoin.internal.inspector.DefaultStreamCallGenerator;
import ch.sourcepond.utils.podescoin.internal.inspector.WriteObjectInspector;

public class WriteObjectMethodClassVisitor extends InjectorMethodClassVisitor {
	private static final Logger LOG = getLogger(WriteObjectMethodClassVisitor.class);

	public WriteObjectMethodClassVisitor(final ClassVisitor pVisitor, final WriteObjectInspector pInspector) {
		super(pInspector, pVisitor);
	}

	@Override
	protected MethodVisitor createInjectionMethodWriter() {
		LOG.debug("{} : create new writeObject method", getClassName());
		return cv.visitMethod(ACC_PRIVATE, WRITE_OBJECT_METHOD_NAME, WRITE_OBJECT_METHOD_DESC, null,
				WRITE_OBJECT_METHOD_EXCEPTIONS);
	}

	@Override
	protected Enhancer createInjectionMethodVisitor(final MethodVisitor pWriter, final boolean pEnhanceMode,
			final DefaultStreamCallGenerator pDefaultStreamCallGenerator) {
		return new WriteObjectEnhancer(inspector, pWriter, pEnhanceMode, pDefaultStreamCallGenerator);
	}

	@Override
	protected boolean isInjectorMethod(final int access, final String name, final String desc,
			final String[] exceptions) {
		return isWriteObjectMethod(access, name, desc, exceptions);
	}
}
