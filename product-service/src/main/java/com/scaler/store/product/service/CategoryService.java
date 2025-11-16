package com.scaler.store.product.service;

import com.scaler.store.product.dto.CategoryDto;
import com.scaler.store.product.entity.Category;
import com.scaler.store.product.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryDto(c.getId(), c.getName(), c.getDescription(), c.getSlug()))
                .collect(Collectors.toList());
    }

    public CategoryDto createCategory(CategoryDto dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Category name already exists: " + dto.getName());
        }
        Category c = new Category(dto.getName(), dto.getDescription(), dto.getSlug());
        Category saved = categoryRepository.save(c);
        return new CategoryDto(saved.getId(), saved.getName(), saved.getDescription(), saved.getSlug());
    }
}
