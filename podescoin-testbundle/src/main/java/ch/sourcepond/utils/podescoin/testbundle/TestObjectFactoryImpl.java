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
	
	/* (non-Javadoc)
	 * @see ch.sourcepond.utils.podescoin.testbundle.TestObjectFactory#getFieldInjectionObject()
	 */
	@Override
	public FieldInjectionObject getFieldInjectionObject() throws Exception {
		return serializeDeserialize(new FieldInjectionObject());
	}
	
	/* (non-Javadoc)
	 * @see ch.sourcepond.utils.podescoin.testbundle.TestObjectFactory#getFieldInjectionObjectWithComponentId()
	 */
	@Override
	public FieldInjectionObjectWithComponentId getFieldInjectionObjectWithComponentId() throws Exception {
		return serializeDeserialize(new FieldInjectionObjectWithComponentId());
	}

	/* (non-Javadoc)
	 * @see ch.sourcepond.utils.podescoin.testbundle.TestObjectFactory#getInjectorMethodObject()
	 */
	@Override
	public InjectorMethodObject getInjectorMethodObject() throws Exception {
		return serializeDeserialize(new InjectorMethodObject());
	}

	/* (non-Javadoc)
	 * @see ch.sourcepond.utils.podescoin.testbundle.TestObjectFactory#getInjectorMethodObjectWithComponentId()
	 */
	@Override
	public InjectorMethodObjectWithComponentId getInjectorMethodObjectWithComponentId() throws Exception {
		return serializeDeserialize(new InjectorMethodObjectWithComponentId());
	}
}
