package com.test.dao;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.google.common.base.Supplier;
import com.test.beans.Product;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.indices.Flush;
import io.searchbox.indices.Refresh;
import vc.inreach.aws.request.AWSSigner;
import vc.inreach.aws.request.AWSSigningRequestInterceptor;

public class ElasticDataAccessor {
	private final JestClient client;

	public ElasticDataAccessor(String esHost) {
		this(esHost, DefaultAWSCredentialsProviderChain.getInstance());
	}

	public ElasticDataAccessor(String esHost, AWSCredentialsProvider awsCredentialsProvider) {
		final Supplier<LocalDateTime> clock = () -> LocalDateTime.now(ZoneOffset.UTC);
		final AWSSigner awsSigner = new AWSSigner(awsCredentialsProvider, "us-east-1", "es", clock);
		final AWSSigningRequestInterceptor requestInterceptor = new AWSSigningRequestInterceptor(
		        awsSigner);
		final JestClientFactory factory = new JestClientFactory() {
			@Override
			protected HttpClientBuilder configureHttpClient(HttpClientBuilder builder) {
				builder.addInterceptorLast(requestInterceptor);
				return builder;
			}

			@Override
			protected HttpAsyncClientBuilder configureHttpClient(HttpAsyncClientBuilder builder) {
				builder.addInterceptorLast(requestInterceptor);
				return builder;
			}
		};
		factory.setHttpClientConfig(
		        new HttpClientConfig.Builder("https://" + esHost).multiThreaded(true).build());
		client = factory.getObject();
	}

	public JestClient getClient() {
		return client;
	}

	public int save(Product product) {
		Index index = new Index.Builder(product).index("product").type("product-index-type")
		        .setParameter("op_type", "create").id(product.getId()).build();
		JestResult result;
		try {
			result = client.execute(index);
			Flush flush = new Flush.Builder().addIndex("product").build();
			client.execute(flush);
			Refresh refresh = new Refresh.Builder().addIndex("product-index-type").build();
			client.execute(refresh);
			if (!(result.getResponseCode() == 201
			        || result.getResponseCode() == HttpStatus.SC_CONFLICT)) {
				throw new RuntimeException("Failed to insert new message "
				        + result.getErrorMessage() + "  " + result.getResponseCode());
			}
			return result.getResponseCode();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<Product> getProducts(String searchTerm, int from, int size) {
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.matchPhraseQuery("name", searchTerm));
		searchSourceBuilder.size(size);
		searchSourceBuilder.from(from);
		Search search = new Search.Builder(searchSourceBuilder.toString())
		        // multiple index or types can be added.
		        .addIndex("product").addType("product-index-type").build();
		try {
			SearchResult result = client.execute(search);
			List<Product> products = result.getSourceAsObjectList(Product.class);
			return products;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
