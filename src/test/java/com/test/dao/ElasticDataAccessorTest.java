package com.test.dao;

import org.junit.Before;
import org.junit.Test;

import com.test.beans.Product;

public class ElasticDataAccessorTest {
	private ElasticDataAccessor elasticDataAccessor;
	
	@Before
	public void setup() {
		elasticDataAccessor = new ElasticDataAccessor("search-products-mwde7hdv6okyi2t5xmfin2ukx4.us-east-1.es.amazonaws.com");
	}
	
	
}
