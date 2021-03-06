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

import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;

import ch.sourcepond.utils.podescoin.api.WriteObject;
import ch.sourcepond.utils.podescoin.internal.SerializableClassVisitor;

/**
 * @author rolandhauser
 *
 */
public final class WriteObjectInspector extends Inspector {

	@Override
	protected DefaultStreamCallGenerator createDefaultStreamCallGenerator() {
		return new DefaultWriteObjectVisitor();
	}

	@Override
	protected boolean isInjectorMethod(final int access, final String name, final String desc,
			final String[] exceptions) {
		return SerializableClassVisitor.isWriteObjectMethod(access, name, desc, exceptions);
	}

	@Override
	protected Class<?> getObjectStreamClass() {
		return ObjectOutputStream.class;
	}

	@Override
	protected Class<? extends Annotation> getInjectorMethodAnnotation() {
		return WriteObject.class;
	}
}