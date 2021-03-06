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

import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getType;

final class DefaultReadObjectVisitor extends BaseStreamCallGenerator {
	private static final String DEFAULT_READ_OBJECT = "defaultReadObject";
	private static final String DEFAULT_READ_OBJECT_DESC = getMethodDescriptor(getType(void.class));

	@Override
	protected String getStreamInternalName() {
		return OBJECT_INPUT_STREAM_INTERNAL_NAME;
	}

	@Override
	protected String getMethodName() {
		return DEFAULT_READ_OBJECT;
	}

	@Override
	protected String getMethodDesc() {
		return DEFAULT_READ_OBJECT_DESC;
	}

}
