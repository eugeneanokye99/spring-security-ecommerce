package com.shopjoy.specification;

import com.shopjoy.dto.filter.OrderFilter;
import com.shopjoy.entity.Order;
import com.shopjoy.entity.User;
import com.shopjoy.entity.OrderStatus;
import com.shopjoy.entity.PaymentStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class OrderSpecification {

    public static Specification<Order> withFilters(Integer userId, OrderFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }

            if (filter != null) {
                if (StringUtils.hasText(filter.getStatus())) {
                    try {
                        predicates.add(cb.equal(root.get("status"), OrderStatus.valueOf(filter.getStatus().toUpperCase())));
                    } catch (IllegalArgumentException ignored) {}
                }

                if (StringUtils.hasText(filter.getPaymentStatus())) {
                    try {
                        predicates.add(cb.equal(root.get("paymentStatus"), PaymentStatus.valueOf(filter.getPaymentStatus().toUpperCase())));
                    } catch (IllegalArgumentException ignored) {}
                }

                if (filter.getStartDate() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("orderDate"), filter.getStartDate()));
                }

                if (filter.getEndDate() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("orderDate"), filter.getEndDate()));
                }

                if (filter.getMinAmount() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("totalAmount"), filter.getMinAmount()));
                }

                if (filter.getMaxAmount() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("totalAmount"), filter.getMaxAmount()));
                }

                if (StringUtils.hasText(filter.getSearchTerm())) {
                    String searchTerm = filter.getSearchTerm().toLowerCase();
                    String pattern = "%" + searchTerm + "%";
                    
                    List<Predicate> searchPredicates = new ArrayList<>();
                    
                    // Search in order details
                    searchPredicates.add(cb.like(cb.lower(root.get("shippingAddress")), pattern));
                    searchPredicates.add(cb.like(cb.lower(root.get("notes")), pattern));
                    
                    // Search in customer details (requires join)
                    Join<Order, User> userJoin = root.join("user", JoinType.LEFT);
                    searchPredicates.add(cb.like(cb.lower(userJoin.get("firstName")), pattern));
                    searchPredicates.add(cb.like(cb.lower(userJoin.get("lastName")), pattern));
                    searchPredicates.add(cb.like(cb.lower(userJoin.get("email")), pattern));
                    
                    // Search in order ID safely
                    if (searchTerm.matches("\\d+")) {
                        searchPredicates.add(cb.equal(root.get("orderId"), Integer.parseInt(searchTerm)));
                    }
                    
                    // Partial ID search - using CONCAT function for database portability (Postgres integer vs text)
                    // This avoids the "operator does not exist: integer ~~ text" error in PostgreSQL
                    searchPredicates.add(cb.like(cb.function("CONCAT", String.class, root.get("orderId"), cb.literal("")), pattern));
                    
                    predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
