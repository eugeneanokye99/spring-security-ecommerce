package com.shopjoy.service.impl;

import com.shopjoy.aspect.PerformanceMetricsCollector;
import com.shopjoy.dto.response.DashboardDataResponse;
import com.shopjoy.dto.response.UserAnalyticsResponse;
import com.shopjoy.repository.AnalyticsRepository;
import com.shopjoy.repository.OrderRepository;
import com.shopjoy.repository.ProductRepository;
import com.shopjoy.repository.UserRepository;
import com.shopjoy.service.AnalyticsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final AnalyticsRepository analyticsRepository;
    private final PerformanceMetricsCollector metricsCollector;

    @Override
    public DashboardDataResponse getDashboardData() {
        Double totalRevenue = analyticsRepository.getTotalRevenue();

        DashboardDataResponse.OverallStats stats = DashboardDataResponse.OverallStats.builder()
                .totalProducts(productRepository.count())
                .totalUsers(userRepository.count())
                .totalOrders(orderRepository.count())
                .totalRevenue(totalRevenue != null ? totalRevenue : 0.0)
                .build();

        return DashboardDataResponse.builder()
                .overallStats(stats)
                .salesOverTime(analyticsRepository.getSalesOverTime())
                .categoryDistribution(analyticsRepository.getCategoryDistribution())
                .performanceMetrics(metricsCollector.getAllMetrics())
                .build();
    }

    @Override
    public UserAnalyticsResponse getUserAnalytics(Integer userId) {
        Long totalOrders = analyticsRepository.getUserTotalOrders(userId);
        Double totalSpent = analyticsRepository.getUserTotalSpent(userId);
        Long totalItems = analyticsRepository.getUserTotalItems(userId);
        List<UserAnalyticsResponse.CategorySpending> categorySpending = analyticsRepository
                .getUserCategorySpending(userId);
        List<UserAnalyticsResponse.RecentActivity> recentActivities = analyticsRepository.getUserRecentActivity(userId);

        return UserAnalyticsResponse.builder()
                .totalOrders(totalOrders != null ? totalOrders : 0)
                .totalSpent(totalSpent != null ? totalSpent : 0.0)
                .totalItemsPurchased(totalItems != null ? totalItems : 0)
                .spendingByCategory(categorySpending)
                .recentActivities(recentActivities)
                .build();
    }
}
