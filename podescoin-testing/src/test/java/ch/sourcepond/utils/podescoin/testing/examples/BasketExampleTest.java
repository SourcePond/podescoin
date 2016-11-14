package ch.sourcepond.utils.podescoin.testing.examples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;

import ch.sourcepond.utils.podescoin.CloneContext;
import ch.sourcepond.utils.podescoin.CloneContextFactory;
import ch.sourcepond.utils.podescoin.testing.examples.basket.Address;
import ch.sourcepond.utils.podescoin.testing.examples.basket.AddressService;
import ch.sourcepond.utils.podescoin.testing.examples.basket.Basket;
import ch.sourcepond.utils.podescoin.testing.examples.basket.Item;
import ch.sourcepond.utils.podescoin.testing.examples.basket.Product;
import ch.sourcepond.utils.podescoin.testing.examples.basket.ProductService;

public class BasketExampleTest {
	private static final String ADDRESS_ID = "1234";
	private static final String PRODUCT_ID = "5678";
	private static final String ORDER_NUMBER = "9123";
	private static final int QUANTITY = 2;
	private final AddressService addressService = mock(AddressService.class);
	private final ProductService productService = mock(ProductService.class);
	private final Address address = mock(Address.class);
	private final Product product = mock(Product.class);

	@Test
	public void verifyBasket() throws Exception {
		when(addressService.load(ADDRESS_ID)).thenReturn(address);
		when(productService.load(PRODUCT_ID)).thenReturn(product);
		when(address.getAddressId()).thenReturn(ADDRESS_ID);
		when(product.getProductId()).thenReturn(PRODUCT_ID);

		final CloneContext ctx = CloneContextFactory.newContext().addComponent(addressService, AddressService.class)
				.addComponent(productService, "product.service", ProductService.class);

		final Basket basket = new Basket(address);
		basket.setOrderNumber(ORDER_NUMBER);

		final Item item = new Item(product);
		item.setQuantity(QUANTITY);
		basket.getItems().add(item);

		final Basket cloneBasket = ctx.deepClone(basket);
		assertEquals(ORDER_NUMBER, cloneBasket.getOrderNumber());
		assertSame(basket.getAddress(), cloneBasket.getAddress());

		final List<Item> items = cloneBasket.getItems();
		assertEquals(1, items.size());

		final Item clonedItem = items.get(0);
		assertEquals(QUANTITY, clonedItem.getQuantity());
		assertSame(product, clonedItem.getProduct());
	}
}
