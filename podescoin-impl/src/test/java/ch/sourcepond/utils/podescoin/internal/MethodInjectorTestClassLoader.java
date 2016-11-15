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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.osgi.framework.Bundle;

import ch.sourcepond.utils.podescoin.TestClassLoader;
import ch.sourcepond.utils.podescoin.internal.InspectForInjectorMethodClassVisitor;
import ch.sourcepond.utils.podescoin.internal.MethodInjectionClassVisitor;

public class MethodInjectorTestClassLoader extends TestClassLoader {

	public MethodInjectorTestClassLoader(final ClassVisitor pVisitor, final ClassWriter pWriter,
			final Class<?> pTestSerializableClass, final Bundle pBundle) {
		super(new InspectForInjectorMethodClassVisitor(pVisitor), pWriter, pTestSerializableClass, pBundle);
	}

	@Override
	protected byte[] secondPass(final byte[] pClassData) {
		final ClassReader reader = new ClassReader(writer.toByteArray());
		ClassWriter writer = new ClassWriter(reader, 0);
		MethodInjectionClassVisitor visitor = new MethodInjectionClassVisitor(writer, (InspectForInjectorMethodClassVisitor) this.visitor);
		reader.accept(visitor, 0);
		return writer.toByteArray();
	}

}
