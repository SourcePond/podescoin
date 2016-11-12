package ch.sourcepond.utils.podescoin.testing.examples;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.inject.Inject;

public class TestSerializable implements Serializable {
	private transient SomeComponent component;
	private String name;

	public TestSerializable(final SomeComponent pComponent) {
		component = pComponent;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public SomeComponent getSomeComponent() {
		return component;
	}

	private void writeObject(final ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeUTF(component.getOid());
	}

	@Inject
	void readObject(final ObjectInputStream in, final TestService1 srv1) throws ClassNotFoundException, IOException {
		in.defaultReadObject();

		System.out.println(srv1);

		final String oid = in.readUTF();
		component = srv1.load(oid);
	}

	public String getName() {
		return name;
	}
}
