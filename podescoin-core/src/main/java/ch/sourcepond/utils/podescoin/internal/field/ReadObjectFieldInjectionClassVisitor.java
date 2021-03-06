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

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.sourcepond.utils.podescoin.internal.inspector.Inspector;

public class ReadObjectFieldInjectionClassVisitor extends FieldInjectionClassVisitor {
	private static final Logger LOG = LoggerFactory.getLogger(ReadObjectFieldInjectionClassVisitor.class);

	public ReadObjectFieldInjectionClassVisitor(Inspector pInspector, ClassVisitor pWriter) {
		super(pInspector, pWriter);
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
