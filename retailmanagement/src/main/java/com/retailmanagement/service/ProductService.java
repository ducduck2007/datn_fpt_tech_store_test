package com.retailmanagement.service;

import com.retailmanagement.dto.request.ProductRequest;
import com.retailmanagement.dto.response.ProductResponse;
import com.retailmanagement.entity.Category;
import com.retailmanagement.entity.Image;
import com.retailmanagement.entity.Product;
import com.retailmanagement.entity.ProductCategory;
import com.retailmanagement.entity.ProductCategoryId;
import com.retailmanagement.entity.ProductVariant;
import com.retailmanagement.repository.CategoryRepository;
import com.retailmanagement.repository.ImageRepository;
import com.retailmanagement.repository.ProductCategoryRepository;
import com.retailmanagement.repository.ProductRepository;
import com.retailmanagement.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;
    private final ProductCategoryRepository productCategoryRepository;

    // ✅ Added for computing min price in product list
    private final ProductVariantRepository productVariantRepository;

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
            pc.setId(new ProductCategoryId(savedProduct.getId(), category.getId()));
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
        Page<Product> productPage;

        // ✅ Keep fixed 20/page as required
        if (categoryId != null) {
            Pageable pageable = PageRequest.of(page, 20); // ordering handled by native query ORDER BY
            productPage = productRepository.findByCategoryId(categoryId, pageable);
        } else {
            Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
            productPage = productRepository.findAll(pageable);
        }

        return productPage.map(this::toProductResponse);
    }

    private ProductResponse toProductResponse(Product product) {
        ProductResponse dto = new ProductResponse();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSku(product.getSku());
        dto.setDescription(product.getDescription());
        dto.setIsVisible(product.getIsVisible());
        dto.setCreatedAt(product.getCreatedAt());

        imageRepository.findFirstByProductIdAndIsPrimaryTrue(product.getId())
                .ifPresent(img -> dto.setImageUrl(img.getUrl()));

        // ✅ "Display current price in product list"
        // Choose min current price among variants (if you want "primary variant" instead, we can switch logic)
        List<ProductVariant> variants = productVariantRepository.findByProduct_Id(product.getId());
        Optional<ProductVariant> minVariantOpt = variants.stream()
                .filter(v -> v.getPrice() != null)
                .min(Comparator.comparing(ProductVariant::getPrice));

        if (minVariantOpt.isPresent()) {
            ProductVariant v = minVariantOpt.get();
            dto.setMinPrice(v.getPrice());
            dto.setCurrencyCode(v.getCurrencyCode());
        } else {
            dto.setMinPrice(null);
            dto.setCurrencyCode(null);
        }

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
