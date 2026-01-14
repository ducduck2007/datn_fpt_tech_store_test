package com.retailmanagement.service;


import com.retailmanagement.dto.request.ProductRequest;
import com.retailmanagement.dto.response.ProductResponse;
import com.retailmanagement.entity.*;
import com.retailmanagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ProductService {

    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ImageRepository imageRepository;
    @Autowired private ProductCategoryRepository productCategoryRepository;

    private final String UPLOAD_DIR = "uploads/";

    @Transactional
    public Product createProduct(ProductRequest request) throws IOException {
        Product product = new Product();
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setDescription(request.getDescription());
        product.setIsVisible(request.getIsVisible() != null ? request.getIsVisible() : true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));

            ProductCategory pc = new ProductCategory();

            ProductCategoryId pcId = new ProductCategoryId();
            pcId.setProductId(savedProduct.getId());
            pcId.setCategoryId(category.getId());
            pcId.setProductId(savedProduct.getId());
            pcId.setCategoryId(category.getId());

            pc.setId(pcId);
            pc.setCategory(category);
            pc.setIsPrimary(true);
            pc.setCreatedAt(Instant.now());

            productCategoryRepository.save(pc);
        }

        if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
            String fileName = saveFileToDisk(request.getImageFile());

            Image image = new Image();
            image.setProduct(savedProduct);
            image.setUrl("/uploads/" + fileName);
            image.setIsPrimary(true);
            image.setSortOrder(1);
            image.setCreatedAt(Instant.now());

            imageRepository.save(image);
        }

        return savedProduct;
    }

    public Page<ProductResponse> getProducts(int page, Integer categoryId) {
        Page<Product> productPage;

        if (categoryId != null) {
            Pageable pageable = PageRequest.of(page, 20, Sort.by("created_at").descending());
            productPage = productRepository.findByCategoryId(categoryId, pageable);
        } else {
            Pageable pageable = PageRequest.of(page, 20, Sort.by("createdAt").descending());
            productPage = productRepository.findAll(pageable);
        }

        return productPage.map(product -> {
            ProductResponse dto = new ProductResponse();
            dto.setId(product.getId());
            dto.setName(product.getName());
            dto.setSku(product.getSku());
            dto.setDescription(product.getDescription());
            dto.setIsVisible(product.getIsVisible());
            dto.setCreatedAt(product.getCreatedAt());

            imageRepository.findFirstByProductIdAndIsPrimaryTrue(product.getId())
                    .ifPresent(img -> dto.setImageUrl(img.getUrl()));

            return dto;
        });
    }

    private String saveFileToDisk(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }
}