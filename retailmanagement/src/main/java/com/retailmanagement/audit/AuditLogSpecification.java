package com.retailmanagement.audit;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AuditLogSpecification {

    //Helper để ignore rỗng
    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    private static boolean hasList(List<?> list) {
        return list != null && !list.isEmpty();
    }

    // Filter logic
    public static Specification<AuditLog> filter(AuditLogFilterRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates =  new ArrayList<>();

            // ===== USER =====
            if (request.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), request.getUserId()));
            }

            // ===== MODULE =====
            if (hasText(request.getModule())) {
                predicates.add(criteriaBuilder.equal(root.get("module"), request.getModule()));
            }

            if (hasList(request.getModules())) {
                List<String> modules = request.getModules().stream()
                        .filter(m -> m != null && !m.isBlank())
                        .toList();

                if (!modules.isEmpty()) {
                    predicates.add(root.get("module").in(modules));
                }
            }

            // ===== ACTION =====
            if (hasText(request.getAction())) {
                predicates.add(criteriaBuilder.equal(root.get("action"), request.getAction()));
            }

            if (hasList(request.getActions())) {
                List<String> actions = request.getActions().stream()
                        .filter(a -> a != null && !a.isBlank())
                        .toList();

                if (!actions.isEmpty()) {
                    predicates.add(root.get("action").in(actions));
                }
            }

            // ===== TARGET =====
            if (hasText(request.getTargetType())) {
                predicates.add(
                        criteriaBuilder.equal(root.get("targetType"), request.getTargetType().trim())
                );
            }

            if (request.getTargetId() != null) {
                predicates.add(
                        criteriaBuilder.equal(root.get("targetId"), request.getTargetId())
                );
            }

            // ===== IP =====
            if (hasText(request.getIpAddress())) {
                predicates.add(
                        criteriaBuilder.equal(root.get("ipAddress"), request.getIpAddress().trim())
                );
            }


            // ===== KEYWORD =====
            if (hasText(request.getKeyword())) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("detailsJson")),
                                "%" + request.getKeyword().toLowerCase().trim() + "%"
                        )
                );
            }

            // ===== DATE =====
            if (request.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        request.getStartDate()
                ));
            }

            if (request.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"),
                        request.getEndDate()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
