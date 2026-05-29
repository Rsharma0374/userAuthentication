package com.guardianservices.userAuthentication.domain.port.out;

import com.guardianservices.userAuthentication.domain.model.Product;
import com.guardianservices.userAuthentication.domain.model.Role;

import java.util.List;

/**
 * Repository interface for Product entity
 * Defines persistence operations for products
 */
public interface ProductRepository {


    /**
     * Saves a product to the database
     *
     * @param product product to save
     * @return saved product
     */
    Product save(Product product);

    /**
     * Retrieves all roles
     *
     * @return list of all roles
     */
    List<Product> findAll();
}
