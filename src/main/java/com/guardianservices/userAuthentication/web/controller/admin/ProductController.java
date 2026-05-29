package com.guardianservices.userAuthentication.web.controller.admin;

import com.guardianservices.userAuthentication.application.service.ProductService;
import com.guardianservices.userAuthentication.domain.model.Product;
import com.guardianservices.userAuthentication.web.dto.request.CreateProductRequest;
import com.guardianservices.userAuthentication.web.dto.response.ProductResponse;
import com.guardianservices.userAuthentication.web.dto.response.RoleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for product-related operations
 */
@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Management", description = "Endpoints for managing products")
@SecurityRequirement(name = "bearer-jwt")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * Retrieves all available products
     *
     * @return list of product names
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get all products", description = "Retrieves all available products from the system")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        log.debug("Retrieving all products");

        List<Product> products = productService.getAllProducts();

        List<ProductResponse> responses = products.stream()
                .map(product -> ProductResponse.builder()
                        .name(product.getName())
                        .description(product.getDescription())
                        .createdAt(product.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create Product", description = "Creates a new Product in the system")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        log.debug("Creating product: {}", request.getName());


        productService.createProduct(request);

        ProductResponse response = ProductResponse.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
