package com.test.functions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.test.beans.Product;
import com.test.dao.ElasticDataAccessor;

public class StoreProductDetails implements RequestHandler<Product, Product> {
	private final ElasticDataAccessor esDataAccessor;
	private static final String host="search-products-mwde7hdv6okyi2t5xmfin2ukx4.us-east-1.es.amazonaws.com";

	public StoreProductDetails() {
		esDataAccessor = new ElasticDataAccessor(host);
	}

	/**
	 * Stores the product objects
	 */
	@Override
	public Product handleRequest(Product product, Context context) {
		esDataAccessor.save(product);
		return product;
	}
}
