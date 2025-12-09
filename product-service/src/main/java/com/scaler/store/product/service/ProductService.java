package com.scaler.store.product.service;

import com.scaler.store.product.document.ProductDocument;
import com.scaler.store.product.dto.CreateProductRequest;
import com.scaler.store.product.dto.ProductDto;
import com.scaler.store.product.entity.Category;
import com.scaler.store.product.entity.Product;
import com.scaler.store.product.repository.CategoryRepository;
import com.scaler.store.product.repository.ProductRepository;
import com.scaler.store.product.repository.ProductSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Autowired(required = false)
    private ProductSearchRepository searchRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public ProductDto createProduct(CreateProductRequest req) {
        if (productRepository.existsBySku(req.getSku())) {
            throw new IllegalArgumentException("Product with SKU already exists: " + req.getSku());
        }

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + req.getCategoryId()));

        Product product = new Product();
        product.setSku(req.getSku());
        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setStockQuantity(req.getStockQuantity());
        product.setImageUrl(req.getImageUrl());
        product.setCategory(category);

        Product saved = productRepository.save(product);

        // Index in Elasticsearch
        indexProductInElasticsearch(saved);

        return mapToDto(saved);
    }

    @Cacheable(value = "products", key = "#id")
    public ProductDto getProductById(Long id) {
        log.info("Fetching product ID {} from MySQL Database (Cache Miss)", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));
        return mapToDto(product);
    }

    public Page<ProductDto> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::mapToDto);
    }

    public Page<ProductDto> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable).map(this::mapToDto);
    }

    public Page<ProductDto> searchProducts(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // Try Elasticsearch first for full-text search
        if (searchRepository != null && keyword != null && !keyword.isBlank()) {
            try {
                log.info("Executing full-text search in Elasticsearch for keyword: '{}'", keyword);
                Page<ProductDocument> searchDocs = searchRepository.findByNameContaining(keyword, pageable);
                if (searchDocs.hasContent()) {
                    return searchDocs.map(doc -> {
                        ProductDto dto = new ProductDto();
                        dto.setId(Long.parseLong(doc.getId()));
                        dto.setName(doc.getName());
                        dto.setDescription(doc.getDescription());
                        dto.setPrice(doc.getPrice());
                        dto.setStockQuantity(doc.getStockQuantity());
                        dto.setSku(doc.getSku());
                        dto.setCategoryName(doc.getCategoryName());
                        return dto;
                    });
                }
            } catch (Exception e) {
                log.warn("Elasticsearch query failed or cluster unreachable: {}. Falling back to MySQL full-text search.", e.getMessage());
            }
        }

        // Fallback to MySQL query
        log.info("Executing search query in MySQL with keyword: '{}', categoryId: {}", keyword, categoryId);
        return productRepository.searchProducts(keyword, categoryId, minPrice, maxPrice, pageable)
                .map(this::mapToDto);
    }

    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public ProductDto updateProduct(Long id, CreateProductRequest req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + req.getCategoryId()));

        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setStockQuantity(req.getStockQuantity());
        product.setImageUrl(req.getImageUrl());
        product.setCategory(category);

        Product updated = productRepository.save(product);

        // Update Elasticsearch index
        indexProductInElasticsearch(updated);

        return mapToDto(updated);
    }

    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found with ID: " + id);
        }
        productRepository.deleteById(id);

        if (searchRepository != null) {
            try {
                searchRepository.deleteById(String.valueOf(id));
            } catch (Exception e) {
                log.warn("Failed to delete product from Elasticsearch index: {}", e.getMessage());
            }
        }
    }

    private void indexProductInElasticsearch(Product p) {
        if (searchRepository != null) {
            try {
                ProductDocument doc = new ProductDocument(
                        String.valueOf(p.getId()),
                        p.getName(),
                        p.getDescription(),
                        p.getCategory() != null ? p.getCategory().getName() : null,
                        p.getPrice(),
                        p.getStockQuantity(),
                        p.getSku()
                );
                searchRepository.save(doc);
                log.info("Product ID {} successfully indexed in Elasticsearch", p.getId());
            } catch (Exception e) {
                log.warn("Failed to index product in Elasticsearch: {}", e.getMessage());
            }
        }
    }

    private ProductDto mapToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setSku(product.getSku());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setImageUrl(product.getImageUrl());
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        return dto;
    }
}
