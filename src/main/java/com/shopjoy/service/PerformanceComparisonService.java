package com.shopjoy.service;

import com.shopjoy.entity.Product;
import com.shopjoy.util.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PerformanceComparisonService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
            .getLogger(PerformanceComparisonService.class);

    private final com.shopjoy.repository.ProductRepository productRepository;

    public PerformanceComparisonService(com.shopjoy.repository.ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Map<String, BenchmarkResult> compareSortingAlgorithms(int datasetSize) {
        // Fetch real data from DB
        List<Product> realProducts = productRepository.findAll();
        logger.info("Benchmarking: Fetched {} real products from DB for sorting analysis", realProducts.size());

        List<Product> products = prepareDataset(realProducts, datasetSize);
        logger.info("Benchmarking: Dataset scaled to {} items", products.size());

        Map<String, BenchmarkResult> results = new LinkedHashMap<>();

        Comparator<Product> comparator = ProductComparators.BY_PRICE_ASC;

        results.put("QuickSort", AlgorithmBenchmark.benchmarkSort(
                products, comparator, "QuickSort", SortingAlgorithms::quickSort));

        results.put("MergeSort", AlgorithmBenchmark.benchmarkSort(
                products, comparator, "MergeSort", SortingAlgorithms::mergeSort));

        results.put("HeapSort", AlgorithmBenchmark.benchmarkSort(
                products, comparator, "HeapSort", SortingAlgorithms::heapSort));

        return results;
    }

    public Map<String, BenchmarkResult> compareSearchAlgorithms(int datasetSize) {
        List<Product> realProducts = productRepository.findAll();
        List<Product> products = prepareDataset(realProducts, datasetSize);

        Map<String, BenchmarkResult> results = new LinkedHashMap<>();

        // Ensure list is sorted for binary/jump search
        SortingAlgorithms.quickSort(products, ProductComparators.BY_ID_ASC);

        Product target = products.isEmpty() ? new Product() : products.get(products.size() / 2);

        results.put("BinarySearch", AlgorithmBenchmark.benchmarkSearch(
                products,
                target,
                "BinarySearch",
                (list, t) -> SearchAlgorithms.binarySearch(list, t, ProductComparators.BY_ID_ASC)));

        results.put("JumpSearch", AlgorithmBenchmark.benchmarkSearch(
                products,
                target,
                "JumpSearch",
                (list, t) -> SearchAlgorithms.jumpSearch(list, t, ProductComparators.BY_ID_ASC)));

        results.put("LinearSearch", AlgorithmBenchmark.benchmarkSearch(
                products,
                target,
                "LinearSearch",
                (list, t) -> {
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).getProductId() == t.getProductId()) {
                            return i;
                        }
                    }
                    return -1;
                }));

        return results;
    }

    public Map<String, Object> generateRecommendations(int datasetSize) {
        Map<String, Object> recommendations = new LinkedHashMap<>();

        recommendations.put("sortingAlgorithm",
                OptimizationRecommendation.recommendSortingAlgorithm(datasetSize));

        recommendations.put("searchAlgorithm",
                OptimizationRecommendation.recommendSearchAlgorithm(datasetSize, true));

        recommendations.put("paginationStrategy",
                OptimizationRecommendation.recommendPaginationStrategy(false, 20));

        recommendations.put("memoryOptimization",
                OptimizationRecommendation.recommendMemoryOptimization(
                        datasetSize,
                        Runtime.getRuntime().freeMemory()));

        return recommendations;
    }

    private List<Product> prepareDataset(List<Product> source, int targetSize) {
        if (source.isEmpty()) {
            return generateTestProducts(targetSize); // Fallback if DB is empty
        }

        List<Product> dataset = new ArrayList<>();
        while (dataset.size() < targetSize) {
            dataset.addAll(source);
        }
        return dataset.subList(0, targetSize);
    }

    private List<Product> generateTestProducts(int size) {
        List<Product> products = new ArrayList<>();
        Random random = new Random(42);

        for (int i = 1; i <= size; i++) {
            Product product = new Product();
            product.setProductId(i);
            product.setProductName("Product_" + i);
            product.setDescription("Description for product " + i);
            product.setPrice(BigDecimal.valueOf(10 + random.nextDouble() * 990));
            product.setCategoryId(1 + random.nextInt(10));
            product.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(365)));
            products.add(product);
        }

        return products;
    }
}
