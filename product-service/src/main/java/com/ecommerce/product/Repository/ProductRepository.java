package com.ecommerce.product.Repository;

import com.ecommerce.product.Model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface ProductRepository extends MongoRepository<Product,String> {

}
