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

import org.objectweb.asm.ClassVisitor;

import ch.sourcepond.utils.podescoin.internal.SerializableClassVisitor;
import ch.sourcepond.utils.podescoin.internal.inspector.Inspector;

public abstract class InjectorMethodClassVisitor extends SerializableClassVisitor {

	protected InjectorMethodClassVisitor(final Inspector pInspector, final ClassVisitor pWriter) {
		super(pInspector, pWriter);
	}

	@Override
	protected final boolean isEnhancementNecessary() {
		final String[][] components = inspector.getComponents();
		return components != null && components.length > 0;
	}

}
