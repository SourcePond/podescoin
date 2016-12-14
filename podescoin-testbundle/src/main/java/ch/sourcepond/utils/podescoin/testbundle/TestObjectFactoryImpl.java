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
package ch.sourcepond.utils.podescoin.testbundle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class TestObjectFactoryImpl implements TestObjectFactory {

	@SuppressWarnings("unchecked")
	private <T> T serializeDeserialize(final T pObj) throws Exception {
		final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		try (final ObjectOutputStream out = new ObjectOutputStream(byteOut)) {
			out.writeObject(pObj);
		}

		try (final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(byteOut.toByteArray()))) {
			return (T) in.readObject();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.sourcepond.utils.podescoin.testbundle.TestObjectFactory#
	 * getFieldInjectionObject()
	 */
	@Override
	public FieldInjectionObject getFieldInjectionObject() throws Exception {
		return serializeDeserialize(new FieldInjectionObject());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.sourcepond.utils.podescoin.testbundle.TestObjectFactory#
	 * getFieldInjectionObjectWithComponentId()
	 */
	@Override
	public FieldInjectionObjectWithComponentId getFieldInjectionObjectWithComponentId() throws Exception {
		return serializeDeserialize(new FieldInjectionObjectWithComponentId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.sourcepond.utils.podescoin.testbundle.TestObjectFactory#
	 * getInjectorMethodObject()
	 */
	@Override
	public InjectorMethodObject getInjectorMethodObject() throws Exception {
		return serializeDeserialize(new InjectorMethodObject());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.sourcepond.utils.podescoin.testbundle.TestObjectFactory#
	 * getInjectorMethodObjectWithComponentId()
	 */
	@Override
	public InjectorMethodObjectWithComponentId getInjectorMethodObjectWithComponentId() throws Exception {
		return serializeDeserialize(new InjectorMethodObjectWithComponentId());
	}

	@Override
	public UnserializableObject getUnserializableObject() throws Exception {
		// Loading class UnserializableObject should not cause an exception,
		// but, the fact that the class is not serializable should be logged by
		// the framework.
		return new UnserializableObject();
	}
}
