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
package ch.sourcepond.utils.podescoin.testing.examples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ch.sourcepond.utils.podescoin.CloneContext;
import ch.sourcepond.utils.podescoin.CloneContextFactory;
import ch.sourcepond.utils.podescoin.Component;
import ch.sourcepond.utils.podescoin.testing.examples.basket.Address;
import ch.sourcepond.utils.podescoin.testing.examples.basket.AddressService;
import ch.sourcepond.utils.podescoin.testing.examples.basket.Basket;
import ch.sourcepond.utils.podescoin.testing.examples.basket.Item;
import ch.sourcepond.utils.podescoin.testing.examples.basket.Product;
import ch.sourcepond.utils.podescoin.testing.examples.basket.ProductService;
import ch.sourcepond.utils.podescoin.testing.examples.basket.StockService;

public class BasketExample2Test {
	private static final String ADDRESS_ID = "1234";
	private static final String PRODUCT_ID = "5678";
	private static final String ORDER_NUMBER = "9123";
	private static final int QUANTITY = 2;
	private static final int STOCK = 10;

	@Component
	@Mock
	private AddressService addressService;

	@Component("product.service")
	@Mock
	private ProductService productService;

	@Component("stock.service")
	@Mock
	private StockService stockService;

	@Mock
	private Address address;

	@Mock
	private Product product;

	private CloneContext ctx;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		ctx = CloneContextFactory.newContext(this);
	}

	@Test
	public void verifyBasket() throws Exception {
		when(addressService.load(ADDRESS_ID)).thenReturn(address);
		when(productService.load(PRODUCT_ID)).thenReturn(product);
		when(address.getAddressId()).thenReturn(ADDRESS_ID);
		when(product.getProductId()).thenReturn(PRODUCT_ID);
		when(stockService.getStock(PRODUCT_ID)).thenReturn(STOCK);

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
		assertEquals(STOCK, clonedItem.getStock());
	}
}
