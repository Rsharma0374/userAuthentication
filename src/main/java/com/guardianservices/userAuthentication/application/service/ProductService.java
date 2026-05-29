package com.guardianservices.userAuthentication.application.service;

import com.guardianservices.userAuthentication.domain.model.Product;
import com.guardianservices.userAuthentication.web.dto.request.CreateProductRequest;
import jakarta.validation.Valid;

import java.util.List;

public interface ProductService {
    boolean checkProductExistence(String product);

    List<Product> getAllProducts();

    void createProduct(@Valid CreateProductRequest request);
}
