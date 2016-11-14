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

import javax.inject.Inject;
import javax.inject.Named;

public class Item implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private transient Product product;
	private int quantity;

	public Item(final Product pProduct) {
		product = pProduct;
	}

	public Product getProduct() {
		return product;
	}

	@Inject
	void initProduct(final ObjectInputStream in, @Named("product.service") final ProductService pProductService)
			throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		product = pProductService.load(in.readUTF());
	}

	private void writeObject(final ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeUTF(product.getProductId());
	}

	public void setQuantity(final int pQuantity) {
		quantity = pQuantity;
	}

	public int getQuantity() {
		return quantity;
	}
}
