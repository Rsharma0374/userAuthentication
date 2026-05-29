package com.guardianservices.userAuthentication.application.service.impl;

import com.guardianservices.userAuthentication.application.service.ProductService;
import com.guardianservices.userAuthentication.domain.model.Product;
import com.guardianservices.userAuthentication.domain.port.out.ProductRepository;
import com.guardianservices.userAuthentication.infrastructure.persistence.jpa.repository.ProductJpaRepository;
import com.guardianservices.userAuthentication.web.dto.request.CreateProductRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public boolean checkProductExistence(String product) {
        return productJpaRepository.existsByName(product);
    }

    /**
     * Retrieves all roles from Keycloak
     *
     * @return list of role names
     */
    @Transactional(readOnly = true)
    @Override
    public List<Product> getAllProducts() {
        log.debug("Retrieving all products");
        // In a real implementation, fetch from Keycloak or cache
        return productRepository.findAll();
    }

    @Override
    @Transactional
    public void createProduct(CreateProductRequest request) {
        log.debug("Creating new Product: {}", request.getName());

        // Check if role already exists
        if (productJpaRepository.existsByName(request.getName())) {
            throw new RuntimeException("Product already exists: " + request.getName());
        }

        // Save product to local database
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        productRepository.save(product);
        log.info("Product created successfully: {}", request.getName());
    }
}
