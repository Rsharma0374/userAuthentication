package com.guardianservices.userAuthentication.web.controller;

import com.guardianservices.userAuthentication.domain.model.Product;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for product-related operations
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Management", description = "Endpoints for managing products")
@SecurityRequirement(name = "bearer-jwt")
public class ProductController {

    /**
     * Retrieves all available products
     *
     * @return list of product names
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @Operation(summary = "Get all products", description = "Retrieves all available products from the system")
    public ResponseEntity<List<String>> getAllProducts() {
        log.debug("Retrieving all products");

        List<String> products = Arrays.stream(Product.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        return ResponseEntity.ok(products);
    }
}
