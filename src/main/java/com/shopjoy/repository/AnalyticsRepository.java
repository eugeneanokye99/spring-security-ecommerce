package com.shopjoy.repository;

import com.shopjoy.dto.response.DashboardDataResponse;
import com.shopjoy.dto.response.UserAnalyticsResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class AnalyticsRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Double getTotalRevenue() {
        Query query = entityManager.createNativeQuery(
                "SELECT SUM(total_amount) FROM orders WHERE status NOT IN ('cancelled')");
        Object result = query.getSingleResult();
        return result != null ? ((Number) result).doubleValue() : 0.0;
    }

    @SuppressWarnings("unchecked")
    public List<DashboardDataResponse.SalesDataPoint> getSalesOverTime() {
        String sql = "SELECT TO_CHAR(order_date, 'YYYY-MM-DD') as date, SUM(total_amount) as revenue " +
                "FROM orders " +
                "WHERE status NOT IN ('cancelled') " +
                "GROUP BY TO_CHAR(order_date, 'YYYY-MM-DD') " +
                "ORDER BY date DESC LIMIT 7";

        List<Object[]> results = entityManager.createNativeQuery(sql).getResultList();
        return results.stream()
                .map(row -> DashboardDataResponse.SalesDataPoint.builder()
                        .date((String) row[0])
                        .revenue(row[1] != null ? ((Number) row[1]).doubleValue() : 0.0)
                        .build())
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public List<DashboardDataResponse.CategorySalesDataPoint> getCategoryDistribution() {
        String sql = "SELECT c.category_name, SUM(oi.subtotal) as revenue, COUNT(DISTINCT o.order_id) as order_count " +
                "FROM categories c " +
                "JOIN products p ON c.category_id = p.category_id " +
                "JOIN order_items oi ON p.product_id = oi.product_id " +
                "JOIN orders o ON oi.order_id = o.order_id " +
                "WHERE o.status NOT IN ('cancelled') " +
                "GROUP BY c.category_name " +
                "ORDER BY revenue DESC";

        List<Object[]> results = entityManager.createNativeQuery(sql).getResultList();
        return results.stream()
                .map(row -> DashboardDataResponse.CategorySalesDataPoint.builder()
                        .categoryName((String) row[0])
                        .revenue(row[1] != null ? ((Number) row[1]).doubleValue() : 0.0)
                        .orderCount(row[2] != null ? ((Number) row[2]).longValue() : 0L)
                        .build())
                .collect(Collectors.toList());
    }

    public Long getUserTotalOrders(Integer userId) {
        Query query = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM orders WHERE user_id = :userId");
        query.setParameter("userId", userId);
        Object result = query.getSingleResult();
        return result != null ? ((Number) result).longValue() : 0L;
    }

    public Double getUserTotalSpent(Integer userId) {
        Query query = entityManager.createNativeQuery(
                "SELECT SUM(total_amount) FROM orders WHERE user_id = :userId AND status NOT IN ('cancelled')");
        query.setParameter("userId", userId);
        Object result = query.getSingleResult();
        return result != null ? ((Number) result).doubleValue() : 0.0;
    }

    public Long getUserTotalItems(Integer userId) {
        Query query = entityManager.createNativeQuery(
                "SELECT SUM(quantity) FROM order_items oi JOIN orders o ON oi.order_id = o.order_id " +
                        "WHERE o.user_id = :userId AND o.status NOT IN ('cancelled')");
        query.setParameter("userId", userId);
        Object result = query.getSingleResult();
        return result != null ? ((Number) result).longValue() : 0L;
    }

    @SuppressWarnings("unchecked")
    public List<UserAnalyticsResponse.CategorySpending> getUserCategorySpending(Integer userId) {
        String sql = "SELECT c.category_name, SUM(oi.subtotal) as spent " +
                "FROM categories c " +
                "JOIN products p ON c.category_id = p.category_id " +
                "JOIN order_items oi ON p.product_id = oi.product_id " +
                "JOIN orders o ON oi.order_id = o.order_id " +
                "WHERE o.user_id = :userId AND o.status NOT IN ('cancelled') " +
                "GROUP BY c.category_name";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("userId", userId);
        List<Object[]> results = query.getResultList();
        return results.stream()
                .map(row -> UserAnalyticsResponse.CategorySpending.builder()
                        .categoryName((String) row[0])
                        .amountSpent(row[1] != null ? ((Number) row[1]).doubleValue() : 0.0)
                        .build())
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public List<UserAnalyticsResponse.RecentActivity> getUserRecentActivity(Integer userId) {
        String sql = "(SELECT 'Purchased ' || p.product_name as description, TO_CHAR(o.order_date, 'YYYY-MM-DD') as date, 'PURCHASE' as type "
                + "FROM orders o JOIN order_items oi ON o.order_id = oi.order_id JOIN products p ON oi.product_id = p.product_id "
                + "WHERE o.user_id = :userId) " +
                "UNION " +
                "(SELECT 'Reviewed ' || p.product_name as description, TO_CHAR(r.created_at, 'YYYY-MM-DD') as date, 'REVIEW' as type "
                + "FROM reviews r JOIN products p ON r.product_id = p.product_id " +
                "WHERE r.user_id = :userId) " +
                "ORDER BY date DESC LIMIT 5";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("userId", userId);
        List<Object[]> results = query.getResultList();
        return results.stream()
                .map(row -> UserAnalyticsResponse.RecentActivity.builder()
                        .description((String) row[0])
                        .date((String) row[1])
                        .type((String) row[2])
                        .build())
                .collect(Collectors.toList());
    }
}
