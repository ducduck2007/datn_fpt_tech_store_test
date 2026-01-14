package com.retailmanagement.service;

import com.retailmanagement.dto.request.ProductRequest;
import com.retailmanagement.dto.response.ProductResponse;
import com.retailmanagement.entity.Category;
import com.retailmanagement.entity.Image;
import com.retailmanagement.entity.Product;
import com.retailmanagement.entity.ProductCategory;
import com.retailmanagement.entity.ProductCategoryId;
import com.retailmanagement.repository.CategoryRepository;
import com.retailmanagement.repository.ImageRepository;
import com.retailmanagement.repository.ProductCategoryRepository;
import com.retailmanagement.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;
    private final ProductCategoryRepository productCategoryRepository;

    private static final String UPLOAD_DIR = "uploads";

    @Transactional
    public Product createProduct(ProductRequest request) throws IOException {
        Product product = new Product();
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setDescription(request.getDescription());
        product.setIsVisible(request.getIsVisible() != null ? request.getIsVisible() : true);

        Product savedProduct = productRepository.save(product);

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));

            ProductCategory pc = new ProductCategory();
            pc.setId(new ProductCategoryId(savedProduct.getId(), category.getId())); // ✅ bỏ trùng set
            pc.setCategory(category);
            pc.setIsPrimary(true);
            pc.setCreatedAt(Instant.now());

            productCategoryRepository.save(pc);
        }

        MultipartFile imgFile = request.getImageFile();
        if (imgFile != null && !imgFile.isEmpty()) {
            String fileName = saveFileToDisk(imgFile);

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
        Page<Product> productPage = (categoryId != null)
                ? productRepository.findByCategoryId(categoryId, pageRequestForNative(page))
                : productRepository.findAll(pageRequestForJpa(page));

        return productPage.map(this::toResponse);
    }

    private Pageable pageRequestForNative(int page) {
        // Native query -> sort theo tên cột DB
        return PageRequest.of(page, 20, Sort.by("created_at").descending());
    }

    private Pageable pageRequestForJpa(int page) {
        // JPA -> sort theo field entity
        return PageRequest.of(page, 20, Sort.by("createdAt").descending());
    }

    private ProductResponse toResponse(Product product) {
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
    }

    private String saveFileToDisk(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String original = file.getOriginalFilename();
        String safeOriginal = (original == null) ? "file" : original.replaceAll("[\\\\/]+", "_");
        String fileName = UUID.randomUUID() + "_" + safeOriginal;

        Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }
}
