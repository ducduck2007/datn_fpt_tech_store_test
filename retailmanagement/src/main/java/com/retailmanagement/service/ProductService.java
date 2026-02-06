package com.retailmanagement.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductVariantRepository productVariantRepository;
    private final PromotionService promotionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String UPLOAD_DIR = "uploads/";

    @Transactional
    public void createProduct(ProductRequest request) throws IOException {
        Product product = new Product();

        product.setName(request.getName().trim());
        product.setSku(request.getSku().trim().toUpperCase());
        product.setIsVisible(request.getIsVisible() != null ? request.getIsVisible() : true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        String finalDescription = request.getDescription();

        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            try {
                List<AttributeRequest> attrList = objectMapper.readValue(
                        request.getAttributes(),
                        new TypeReference<List<AttributeRequest>>(){}
                );

                StringBuilder attrStr = new StringBuilder();
                attrStr.append("\n\n--- THÔNG SỐ KỸ THUẬT ---\n");
                for (AttributeRequest attr : attrList) {
                    attrStr.append("- ").append(attr.getName())
                            .append(": ").append(attr.getValue()).append("\n");
                }
                finalDescription += attrStr.toString();

            } catch (Exception e) {
                System.err.println("Lỗi parse attributes JSON: " + e.getMessage());
            }
        }
        product.setDescription(finalDescription);

        product.setAttributesJson(request.getAttributes());

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

        String finalDescription = request.getDescription();

        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            try {
                List<AttributeRequest> attrList = objectMapper.readValue(
                        request.getAttributes(),
                        new TypeReference<List<AttributeRequest>>(){}
                );

                StringBuilder attrStr = new StringBuilder();
                attrStr.append("\n--- THÔNG SỐ KỸ THUẬT ---\n");
                for (AttributeRequest attr : attrList) {
                    attrStr.append("- ").append(attr.getName())
                            .append(": ").append(attr.getValue()).append("\n");
                }
                finalDescription += attrStr.toString();

            } catch (Exception e) {
                System.err.println("Lỗi parse attributes JSON: " + e.getMessage());
            }
        }
        product.setDescription(finalDescription);
        product.setAttributesJson(request.getAttributes());

        if (request.getIdsToDelete() != null && !request.getIdsToDelete().isEmpty()) {
            imageRepository.deleteAllById(request.getIdsToDelete());
            imageRepository.flush();
        }

        boolean hasPrimary = imageRepository.existsByProductIdAndIsPrimaryTrue(product.getId());
        if (request.getGalleryImages() != null) {
            for (MultipartFile file : request.getGalleryImages()) {
                if (!file.isEmpty()) {
                    String fileName = saveFileToDisk(file);
                    Image image = new Image();
                    image.setProduct(product);
                    image.setUrl("/uploads/" + fileName);
                    if (!hasPrimary) {
                        image.setIsPrimary(true);
                        hasPrimary = true;
                    } else {
                        image.setIsPrimary(false);
                    }
                    image.setIsPrimary(false);
                    image.setSortOrder(1);
                    image.setCreatedAt(Instant.now());
                    imageRepository.save(image);
                }
            }
        }
        productRepository.save(product);
    }

    public Page<ProductResponse> getProducts(int page, List<Integer> categoryIds, String keyword, String sortBy) {
        String sortColumn = "created_at";
        Sort.Direction direction = Sort.Direction.DESC;
        if (sortBy != null) {
            if (sortBy.contains("name")) sortColumn = "name";
            if (sortBy.contains("asc")) direction = Sort.Direction.ASC;
            if ("oldest".equals(sortBy)) {
                sortColumn = "created_at";
                direction = Sort.Direction.ASC;
            }
        }
        Pageable pageable = PageRequest.of(page, 20, Sort.by(direction, sortColumn));
        String searchKey = (keyword != null && !keyword.trim().isEmpty())
                ? "%" + keyword.trim() + "%"
                : null;
        boolean hasCategory = (categoryIds != null && !categoryIds.isEmpty());
        List<Integer> filterIds = hasCategory ? categoryIds : List.of(-1);
        return productRepository.searchProducts(
                searchKey,
                filterIds,
                hasCategory,
                true,
                pageable
        ).map(this::mapToResponse);
    }

    public ProductResponse getProductById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
        return mapToResponse(product);
    }

    @Transactional
    public void softDeleteProduct(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        product.setIsVisible(false);
        productRepository.save(product);
    }

    /**
     * Map product to response with promotion pricing
     */
    private ProductResponse mapToResponse(Product product) {
        ProductResponse dto = new ProductResponse();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSku(product.getSku());
        dto.setDescription(product.getDescription());
        dto.setIsVisible(product.getIsVisible());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setAttributes(product.getAttributesJson());

        imageRepository.findFirstByProductIdAndIsPrimaryTrue(product.getId())
                .ifPresent(img -> dto.setImageUrl(img.getUrl()));

        // Find cheapest variant with promotion price calculation
        LocalDateTime now = LocalDateTime.now();
        productVariantRepository.findByProduct_Id(product.getId()).stream()
                .filter(v -> v.getPrice() != null)
                .min((v1, v2) -> {
                    // Compare final prices after promotions
                    BigDecimal final1 = calculateFinalPrice(v1, now);
                    BigDecimal final2 = calculateFinalPrice(v2, now);
                    return final1.compareTo(final2);
                })
                .ifPresent(v -> {
                    dto.setMinPrice(v.getPrice());
                    dto.setCurrencyCode(v.getCurrencyCode());
                    dto.setVariantId(v.getId());
                    
                    // Add promotion price if available
                    BigDecimal finalPrice = calculateFinalPrice(v, now);
                    if (finalPrice.compareTo(v.getPrice()) < 0) {
                        // There's a promotion active
                        dto.setPromotionPrice(finalPrice);
                        
                        // Find the promotion to show details
                        Promotion promo = promotionService.findBestPromotionForVariant(v, now);
                        if (promo != null) {
                            dto.setPromotionName(promo.getName());
                            dto.setPromotionCode(promo.getCode());
                        }
                    }
                });

        productCategoryRepository
                .findFirstById_ProductIdAndIsPrimaryTrue(product.getId())
                .ifPresent(pc -> dto.setCategoryId(pc.getCategory().getId()));

        return dto;
    }

    /**
     * Calculate final price after all promotions
     */
    private BigDecimal calculateFinalPrice(ProductVariant variant, LocalDateTime at) {
        BigDecimal basePrice = variant.getPrice();
        if (basePrice == null) return BigDecimal.ZERO;

        Promotion best = promotionService.findBestPromotionForVariant(variant, at);
        if (best != null) {
            return promotionService.computeEffectiveUnitPrice(basePrice, best);
        }
        return basePrice;
    }

    private String saveFileToDisk(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }
}
