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
