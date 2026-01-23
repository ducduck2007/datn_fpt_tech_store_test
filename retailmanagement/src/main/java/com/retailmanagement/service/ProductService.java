package com.retailmanagement.service;

import com.retailmanagement.dto.request.AttributeRequest;
import com.retailmanagement.dto.request.ProductRequest;
import com.retailmanagement.dto.response.ProductResponse;
import com.retailmanagement.entity.*;
import com.retailmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;
    private final ProductCategoryRepository productCategoryRepository;

    private final String UPLOAD_DIR = "uploads/";

    @Transactional
    public void createProduct(ProductRequest request) throws IOException {
        Product product = new Product();

        // 1. Xử lý thông tin cơ bản
        product.setName(request.getName().trim());
        product.setSku(request.getSku().trim().toUpperCase());
        product.setIsVisible(request.getIsVisible() != null ? request.getIsVisible() : true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        // 2. Xử lý thuộc tính: Gộp vào Description
        String finalDescription = request.getDescription();
        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            StringBuilder attrStr = new StringBuilder();
            attrStr.append("\n\n--- THÔNG SỐ KỸ THUẬT ---\n");
            for (AttributeRequest attr : request.getAttributes()) {
                attrStr.append("- ").append(attr.getName())
                        .append(": ").append(attr.getValue()).append("\n");
            }
            finalDescription += attrStr.toString();
        }
        product.setDescription(finalDescription);

        Product savedProduct = productRepository.save(product);

        if (request.getCategoryIds() != null) {
            for (Integer catId : request.getCategoryIds()) {
                ProductCategory pc = new ProductCategory();
                ProductCategoryId pcId = new ProductCategoryId(savedProduct.getId(), catId);
                pc.setId(pcId);
                pc.setCategory(categoryRepository.getReferenceById(catId));
                pc.setIsPrimary(false);
                pc.setCreatedAt(Instant.now());
                productCategoryRepository.save(pc);
            }
        }

        if (request.getGalleryImages() != null) {
            int sortOrder = 0;
            for (MultipartFile file : request.getGalleryImages()) {
                if (!file.isEmpty()) {
                    String fileName = saveFileToDisk(file);
                    Image image = new Image();
                    image.setProduct(savedProduct);
                    image.setUrl("/uploads/" + fileName);
                    image.setIsPrimary(sortOrder == 0);
                    image.setSortOrder(sortOrder++);
                    image.setCreatedAt(Instant.now());
                    imageRepository.save(image);
                }
            }
        }
    }

    @Transactional
    public void updateProduct(Integer id, ProductRequest request) throws IOException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setUpdatedAt(LocalDateTime.now());

        // Update Description kèm thuộc tính mới
        String finalDescription = request.getDescription();
        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            StringBuilder attrStr = new StringBuilder();
            attrStr.append("\n\n--- THÔNG SỐ KỸ THUẬT (Cập nhật) ---\n");
            for (AttributeRequest attr : request.getAttributes()) {
                attrStr.append("- ").append(attr.getName())
                        .append(": ").append(attr.getValue()).append("\n");
            }
            finalDescription += attrStr.toString();
        }
        product.setDescription(finalDescription);

        if (request.getIdsToDelete() != null && !request.getIdsToDelete().isEmpty()) {
            imageRepository.deleteAllById(request.getIdsToDelete());
        }

        if (request.getGalleryImages() != null) {
            for (MultipartFile file : request.getGalleryImages()) {
                if (!file.isEmpty()) {
                    String fileName = saveFileToDisk(file);
                    Image image = new Image();
                    image.setProduct(product);
                    image.setUrl("/uploads/" + fileName);
                    image.setIsPrimary(false);
                    image.setSortOrder(1);
                    image.setCreatedAt(Instant.now());
                    imageRepository.save(image);
                }
            }
        }
        productRepository.save(product);
    }

    public Page<ProductResponse> getProducts(int page, Integer categoryId, String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            Pageable pageable = PageRequest.of(page, 20, Sort.by("created_at").descending());
            return productRepository.searchProducts(keyword, true, pageable).map(this::mapToResponse);
        }

        if (categoryId != null) {
            Pageable pageable = PageRequest.of(page, 20, Sort.by("created_at").descending());
            return productRepository.findByCategoryId(categoryId, pageable).map(this::mapToResponse);
        }

        Pageable pageable = PageRequest.of(page, 20, Sort.by("createdAt").descending());
        return productRepository.findAll(pageable).map(this::mapToResponse);
    }

    private ProductResponse mapToResponse(Product product) {
        ProductResponse dto = new ProductResponse();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSku(product.getSku());
        dto.setDescription(product.getDescription()); // Description này đã chứa cả thuộc tính
        dto.setIsVisible(product.getIsVisible());
        dto.setCreatedAt(product.getCreatedAt());

        imageRepository.findFirstByProductIdAndIsPrimaryTrue(product.getId())
                .ifPresent(img -> dto.setImageUrl(img.getUrl()));

        return dto;
    }

    private String saveFileToDisk(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }
}