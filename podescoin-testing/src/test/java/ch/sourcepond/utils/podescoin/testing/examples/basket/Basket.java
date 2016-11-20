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
package ch.sourcepond.utils.podescoin.testing.examples.basket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ch.sourcepond.utils.podescoin.api.Recipient;

@Recipient
public class Basket implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1670149749787139007L;
	private transient Address address;
	private final List<Item> items = new ArrayList<>();
	private String orderNumber;

	public Basket(final Address pAddress) {
		address = pAddress;
	}

	public List<Item> getItems() {
		return items;
	}

	public void setOrderNumber(final String orderNumber) {
		this.orderNumber = orderNumber;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public Address getAddress() {
		return address;
	}

	private void writeObject(final ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeUTF(address.getAddressId());
	}

	@Inject
	void readObject(final ObjectInputStream in, final AddressService addressService)
			throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		final String oid = in.readUTF();
		address = addressService.load(oid);
	}
}
