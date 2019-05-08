package com.test.functions;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.gson.Gson;
import com.test.beans.Product;
import com.test.beans.Products;
import com.test.beans.Search;
import com.test.dao.ElasticDataAccessor;

public class SearchProductDetails implements RequestHandler<Search, String>{
	private final ElasticDataAccessor esDataAccessor;
	private static final String host="search-products-mwde7hdv6okyi2t5xmfin2ukx4.us-east-1.es.amazonaws.com";

	public SearchProductDetails() {
		esDataAccessor = new ElasticDataAccessor(host);
	}

	/**
	 * Handles the search lambda function requests
	 */
	@Override
	public String handleRequest(Search input, Context context) {
		List<Product> products = esDataAccessor.getProducts(input.getTerm(), input.getFrom(), input.getSize());
		Products products2 = new Products();
		products2.setProducts(products);
		Gson gson = new Gson();
		Client client = ClientBuilder.newClient().register(JacksonJsonProvider.class);
		WebTarget webTarget = client.target(input.getCallback());
		Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
		Response response = invocationBuilder.post(Entity.entity(gson.toJson(products2), MediaType.APPLICATION_JSON));
		return "success";
	}
}
