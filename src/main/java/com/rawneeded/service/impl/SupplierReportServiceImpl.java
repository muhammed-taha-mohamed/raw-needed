package com.rawneeded.service.impl;

import com.rawneeded.dto.report.ProductCardReportDto;
import com.rawneeded.dto.report.ProductCardReportOrderDto;
import com.rawneeded.dto.report.SupplierInsightsReportDto;
import com.rawneeded.dto.report.SupplierSalesReportDto;
import com.rawneeded.dto.report.SupplierProductOptionDto;
import com.rawneeded.enumeration.LineStatus;
import com.rawneeded.enumeration.PlanFeatures;
import com.rawneeded.enumeration.Role;
import com.rawneeded.error.exceptions.AbstractException;
import com.rawneeded.jwt.JwtTokenProvider;
import com.rawneeded.model.Product;
import com.rawneeded.model.RFQOrder;
import com.rawneeded.model.RFQOrderLine;
import com.rawneeded.model.User;
import com.rawneeded.model.UserSubscription;
import com.rawneeded.repository.ProductRepository;
import com.rawneeded.repository.RFQOrderLineRepository;
import com.rawneeded.repository.RFQOrderRepository;
import com.rawneeded.repository.UserRepository;
import com.rawneeded.repository.UserSubscriptionRepository;
import com.rawneeded.service.ISupplierReportService;
import com.rawneeded.util.MessagesUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Collections;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class SupplierReportServiceImpl implements ISupplierReportService {

    private final ProductRepository productRepository;
    private final RFQOrderLineRepository lineRepository;
    private final RFQOrderRepository orderRepository;
    private final UserRepository userRepository;
    private final UserSubscriptionRepository subscriptionRepository;
    private final JwtTokenProvider tokenProvider;
    private final MessagesUtil messagesUtil;

    @Override
    public List<SupplierProductOptionDto> getSupplierProducts() {
        String supplierOwnerId = assertSupplierAndFeatureAccess();

        return productRepository.findAll().stream()
                .filter(p -> p.getSupplier() != null && supplierOwnerId.equals(p.getSupplier().getId()))
                .sorted(Comparator.comparing(Product::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(p -> SupplierProductOptionDto.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .image(p.getImage())
                        .unit(p.getUnit())
                        .build())
                .toList();
    }

    @Override
    public ProductCardReportDto getProductCardReport(String productId, String customerId) {
        String supplierOwnerId = assertSupplierAndFeatureAccess();

        if (productId == null || productId.isBlank()) {
            throw new AbstractException("Product id is required");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AbstractException(messagesUtil.getMessage("PRODUCT_NOT_FOUND")));

        String productSupplierId = product.getSupplier() != null ? product.getSupplier().getId() : null;
        if (!Objects.equals(productSupplierId, supplierOwnerId)) {
            throw new AbstractException(messagesUtil.getMessage("UNAUTHORIZED"));
        }

        List<RFQOrderLine> allSupplierLines = lineRepository.findBySupplierId(supplierOwnerId);
        List<RFQOrderLine> productLines = allSupplierLines.stream()
                .filter(l -> Objects.equals(l.getProductId(), productId))
                .filter(l -> customerId == null || customerId.isBlank() || Objects.equals(l.getCustomerOwnerId(), customerId))
                .toList();

        Map<String, RFQOrder> ordersById = new HashMap<>();
        List<String> orderIds = productLines.stream()
                .map(RFQOrderLine::getOrderId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (!orderIds.isEmpty()) {
            ordersById = StreamSupport.stream(orderRepository.findAllById(orderIds).spliterator(), false)
                    .collect(Collectors.toMap(RFQOrder::getId, o -> o, (a, b) -> a));
        }

        Map<String, String> customerNames = new HashMap<>();
        List<String> customerIds = productLines.stream()
                .map(RFQOrderLine::getCustomerOwnerId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (!customerIds.isEmpty()) {
            for (User u : userRepository.findAllById(customerIds)) {
                customerNames.put(u.getId(), u.getName() != null ? u.getName() : "");
            }
        }

        Map<String, RFQOrder> finalOrdersById = ordersById;
        Map<String, String> finalCustomerNames = customerNames;
        List<ProductCardReportOrderDto> lineDtos = new ArrayList<>(productLines.stream()
                .map(line -> {
                    RFQOrder order = finalOrdersById.get(line.getOrderId());
                    return ProductCardReportOrderDto.builder()
                            .lineId(line.getId())
                            .orderId(line.getOrderId())
                            .orderNumber(order != null ? order.getOrderNumber() : null)
                            .orderCreatedAt(order != null ? order.getCreatedAt() : null)
                            .customerOwnerId(line.getCustomerOwnerId())
                            .customerName(finalCustomerNames.get(line.getCustomerOwnerId()))
                            .customerOrganizationName(line.getCustomerOrganizationName())
                            .status(line.getStatus() != null ? line.getStatus().name() : null)
                            .quantity(line.getQuantity())
                            .unit(line.getUnit())
                            .manualOrder(line.getManualOrder())
                            .build();
                })
                .toList());

        lineDtos.sort((a, b) -> {
            if (a.getOrderCreatedAt() == null && b.getOrderCreatedAt() == null) return 0;
            if (a.getOrderCreatedAt() == null) return 1;
            if (b.getOrderCreatedAt() == null) return -1;
            return b.getOrderCreatedAt().compareTo(a.getOrderCreatedAt());
        });

        Map<String, Long> statusCounts = productLines.stream()
                .collect(Collectors.groupingBy(
                        l -> l.getStatus() != null ? l.getStatus().name() : "UNKNOWN",
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        int totalLines = productLines.size();
        int totalOrders = (int) orderIds.size();
        double totalSalesAmount = productLines.stream()
                .filter(l -> l.getStatus() == LineStatus.COMPLETED)
                .filter(l -> l.getSupplierResponse() != null)
                .mapToDouble(l -> {
                    float effectiveQty = l.getSupplierResponse().getAvailableQuantity() > 0
                            ? l.getSupplierResponse().getAvailableQuantity()
                            : l.getQuantity();
                    double lineSubtotal = l.getSupplierResponse().getPrice() * effectiveQty;
                    double shipping = l.getSupplierResponse().getShippingCost();
                    return lineSubtotal + shipping;
                })
                .sum();

        return ProductCardReportDto.builder()
                .product(ProductCardReportDto.ProductSummary.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .origin(product.getOrigin())
                        .image(product.getImage())
                        .unit(product.getUnit())
                        .inStock(product.isInStock())
                        .stockQuantity(product.getStockQuantity())
                        .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                        .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                        .subCategoryId(product.getSubCategory() != null ? product.getSubCategory().getId() : null)
                        .subCategoryName(product.getSubCategory() != null ? product.getSubCategory().getName() : null)
                        .build())
                .totalLines(totalLines)
                .totalOrders(totalOrders)
                .totalSalesAmount(totalSalesAmount)
                .statusCounts(statusCounts)
                .lines(lineDtos)
                .build();
    }

    @Override
    public SupplierSalesReportDto getSalesReport(Integer month, Integer year) {
        String supplierOwnerId = assertSupplierAndFeatureAccess();

        List<RFQOrderLine> supplierLines = lineRepository.findBySupplierId(supplierOwnerId);
        supplierLines = filterLinesByMonth(supplierLines, month, year);
        int totalRequestsCount = supplierLines.size();
        int completedRequestsCount = (int) supplierLines.stream()
                .filter(l -> l.getStatus() == LineStatus.COMPLETED)
                .count();

        double totalSalesAmount = supplierLines.stream()
                .filter(l -> l.getStatus() == LineStatus.COMPLETED)
                .filter(l -> l.getSupplierResponse() != null)
                .mapToDouble(l -> {
                    float effectiveQty = l.getSupplierResponse().getAvailableQuantity() > 0
                            ? l.getSupplierResponse().getAvailableQuantity()
                            : l.getQuantity();
                    double lineSubtotal = l.getSupplierResponse().getPrice() * effectiveQty;
                    double shipping = l.getSupplierResponse().getShippingCost();
                    return lineSubtotal + shipping;
                })
                .sum();

        Map<String, Long> overallStatusCounts = supplierLines.stream()
                .collect(Collectors.groupingBy(
                        l -> l.getStatus() != null ? l.getStatus().name() : "UNKNOWN",
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        SupplierSalesReportDto.TopProduct topProduct = buildTopProduct(supplierLines);
        SupplierSalesReportDto.TopCustomer topCustomer = buildTopCustomer(supplierLines);

        return SupplierSalesReportDto.builder()
                .totalRequestsCount(totalRequestsCount)
                .completedRequestsCount(completedRequestsCount)
                .totalSalesAmount(totalSalesAmount)
                .overallStatusCounts(overallStatusCounts)
                .topProduct(topProduct)
                .topCustomer(topCustomer)
                .build();
    }

    @Override
    public SupplierInsightsReportDto getInsightsReport() {
        String supplierOwnerId = assertSupplierAndFeatureAccess();
        List<RFQOrderLine> supplierLines = lineRepository.findBySupplierId(supplierOwnerId);
        if (supplierLines == null) supplierLines = Collections.emptyList();

        Map<String, RFQOrder> ordersById = loadOrdersByLineIds(supplierLines);

        List<SupplierInsightsReportDto.MonthlyTrendPoint> monthlyTrend = buildMonthlyTrend(supplierLines, ordersById);
        List<SupplierInsightsReportDto.TopProductPoint> topProducts = buildTopProductsInsights(supplierLines);
        List<SupplierInsightsReportDto.TopCustomerPoint> topCustomers = buildTopCustomersInsights(supplierLines);
        List<SupplierInsightsReportDto.StatusDistributionPoint> statusDistribution = buildStatusDistributionInsights(supplierLines);
        List<SupplierInsightsReportDto.ValueBucketPoint> valueBuckets = buildCompletedValueBuckets(supplierLines);

        return SupplierInsightsReportDto.builder()
                .monthlyTrend(monthlyTrend)
                .topProducts(topProducts)
                .topCustomers(topCustomers)
                .statusDistribution(statusDistribution)
                .completedOrderValueBuckets(valueBuckets)
                .build();
    }

    private List<RFQOrderLine> filterLinesByMonth(List<RFQOrderLine> lines, Integer month, Integer year) {
        if (month == null || year == null) {
            return lines;
        }
        if (month < 1 || month > 12) {
            throw new AbstractException("Invalid month");
        }

        List<String> orderIds = lines.stream()
                .map(RFQOrderLine::getOrderId)
                .filter(Objects::nonNull)
                .filter(id -> !id.isBlank())
                .distinct()
                .toList();
        if (orderIds.isEmpty()) {
            return List.of();
        }

        Map<String, RFQOrder> ordersById = StreamSupport.stream(orderRepository.findAllById(orderIds).spliterator(), false)
                .collect(Collectors.toMap(RFQOrder::getId, o -> o, (a, b) -> a));

        return lines.stream()
                .filter(line -> {
                    RFQOrder order = ordersById.get(line.getOrderId());
                    if (order == null || order.getCreatedAt() == null) return false;
                    return order.getCreatedAt().getYear() == year && order.getCreatedAt().getMonthValue() == month;
                })
                .toList();
    }

    private SupplierSalesReportDto.TopProduct buildTopProduct(List<RFQOrderLine> lines) {
        Map<String, List<RFQOrderLine>> byProduct = lines.stream()
                .filter(l -> l.getProductId() != null && !l.getProductId().isBlank())
                .collect(Collectors.groupingBy(RFQOrderLine::getProductId));
        if (byProduct.isEmpty()) return null;

        Map.Entry<String, List<RFQOrderLine>> best = byProduct.entrySet().stream()
                .max(Comparator.comparingInt(e -> e.getValue().size()))
                .orElse(null);
        if (best == null) return null;

        String productId = best.getKey();
        List<RFQOrderLine> productLines = best.getValue();
        String productName = productLines.stream()
                .map(RFQOrderLine::getProductName)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(productId);

        Map<String, Long> statusCounts = productLines.stream()
                .collect(Collectors.groupingBy(
                        l -> l.getStatus() != null ? l.getStatus().name() : "UNKNOWN",
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        return SupplierSalesReportDto.TopProduct.builder()
                .productId(productId)
                .productName(productName)
                .requestedCount(productLines.size())
                .statusCounts(statusCounts)
                .build();
    }

    private SupplierSalesReportDto.TopCustomer buildTopCustomer(List<RFQOrderLine> lines) {
        Map<String, List<RFQOrderLine>> byCustomer = lines.stream()
                .collect(Collectors.groupingBy(line -> {
                    String ownerId = line.getCustomerOwnerId() != null ? line.getCustomerOwnerId().trim() : null;
                    if (ownerId != null && !ownerId.isBlank()) return ownerId;

                    // Prefer organization CRN when owner id is not available.
                    String orgCrn = line.getCustomerOrganizationCRN() != null ? line.getCustomerOrganizationCRN().trim() : null;
                    if (orgCrn != null && !orgCrn.isBlank()) {
                        return "CRN::" + orgCrn;
                    }

                    // Then fallback to organization name from line itself.
                    String orgName = line.getCustomerOrganizationName() != null ? line.getCustomerOrganizationName().trim() : null;
                    if (orgName != null && !orgName.isBlank()) {
                        return "ORG::" + orgName;
                    }

                    return "UNKNOWN";
                }));
        if (byCustomer.isEmpty()) return null;

        Map.Entry<String, List<RFQOrderLine>> best = byCustomer.entrySet().stream()
                .max(
                        Comparator.comparingInt((Map.Entry<String, List<RFQOrderLine>> e) -> "UNKNOWN".equals(e.getKey()) ? 0 : 1)
                                .thenComparingInt(e -> e.getValue().size())
                )
                .orElse(null);
        if (best == null) return null;

        String customerId = best.getKey();
        List<RFQOrderLine> customerLines = best.getValue();

        String customerName = null;
        if (!customerId.startsWith("ORG::") && !customerId.startsWith("CRN::") && !"UNKNOWN".equals(customerId)) {
            User customer = userRepository.findById(customerId).orElse(null);
            if (customer != null) customerName = customer.getName();
        }

        String customerOrgName = customerLines.stream()
                .map(RFQOrderLine::getCustomerOrganizationName)
                .filter(Objects::nonNull)
                .filter(name -> !name.isBlank())
                .findFirst()
                .orElse(null);
        if ((customerOrgName == null || customerOrgName.isBlank()) && customerId.startsWith("ORG::")) {
            customerOrgName = customerId.replaceFirst("^ORG::", "");
        }
        if ((customerOrgName == null || customerOrgName.isBlank()) && customerId.startsWith("CRN::")) {
            customerOrgName = customerId.replaceFirst("^CRN::", "");
        }

        Map<String, Long> statusCounts = customerLines.stream()
                .collect(Collectors.groupingBy(
                        l -> l.getStatus() != null ? l.getStatus().name() : "UNKNOWN",
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        return SupplierSalesReportDto.TopCustomer.builder()
                .customerOwnerId(
                        customerId.startsWith("ORG::")
                                || customerId.startsWith("CRN::")
                                || "UNKNOWN".equals(customerId)
                                ? null
                                : customerId
                )
                .customerName(customerName)
                .customerOrganizationName(customerOrgName)
                .requestedCount(customerLines.size())
                .statusCounts(statusCounts)
                .build();
    }

    private Map<String, RFQOrder> loadOrdersByLineIds(List<RFQOrderLine> lines) {
        List<String> orderIds = lines.stream()
                .map(RFQOrderLine::getOrderId)
                .filter(Objects::nonNull)
                .filter(id -> !id.isBlank())
                .distinct()
                .toList();
        if (orderIds.isEmpty()) return new HashMap<>();

        return StreamSupport.stream(orderRepository.findAllById(orderIds).spliterator(), false)
                .collect(Collectors.toMap(RFQOrder::getId, o -> o, (a, b) -> a));
    }

    private double lineCompletedAmount(RFQOrderLine line) {
        if (line == null || line.getStatus() != LineStatus.COMPLETED || line.getSupplierResponse() == null) {
            return 0d;
        }
        float effectiveQty = line.getSupplierResponse().getAvailableQuantity() > 0
                ? line.getSupplierResponse().getAvailableQuantity()
                : line.getQuantity();
        return (line.getSupplierResponse().getPrice() * effectiveQty) + line.getSupplierResponse().getShippingCost();
    }

    private List<SupplierInsightsReportDto.MonthlyTrendPoint> buildMonthlyTrend(
            List<RFQOrderLine> lines,
            Map<String, RFQOrder> ordersById
    ) {
        Map<YearMonth, List<RFQOrderLine>> grouped = lines.stream()
                .filter(l -> l.getOrderId() != null)
                .filter(l -> ordersById.containsKey(l.getOrderId()))
                .filter(l -> ordersById.get(l.getOrderId()).getCreatedAt() != null)
                .collect(Collectors.groupingBy(l -> YearMonth.from(ordersById.get(l.getOrderId()).getCreatedAt())));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.<YearMonth, List<RFQOrderLine>>comparingByKey().reversed())
                .limit(6)
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    List<RFQOrderLine> monthLines = e.getValue();
                    int total = monthLines.size();
                    int completed = (int) monthLines.stream().filter(l -> l.getStatus() == LineStatus.COMPLETED).count();
                    double sales = monthLines.stream().mapToDouble(this::lineCompletedAmount).sum();
                    return SupplierInsightsReportDto.MonthlyTrendPoint.builder()
                            .monthKey(e.getKey().toString())
                            .totalRequests(total)
                            .completedRequests(completed)
                            .totalSalesAmount(sales)
                            .build();
                })
                .toList();
    }

    private List<SupplierInsightsReportDto.TopProductPoint> buildTopProductsInsights(List<RFQOrderLine> lines) {
        Map<String, List<RFQOrderLine>> byProduct = lines.stream()
                .filter(l -> l.getProductId() != null && !l.getProductId().isBlank())
                .collect(Collectors.groupingBy(RFQOrderLine::getProductId));

        return byProduct.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
                .limit(5)
                .map(e -> {
                    List<RFQOrderLine> productLines = e.getValue();
                    Map<String, Long> statusCounts = productLines.stream()
                            .collect(Collectors.groupingBy(
                                    l -> l.getStatus() != null ? l.getStatus().name() : "UNKNOWN",
                                    LinkedHashMap::new,
                                    Collectors.counting()
                            ));
                    double sales = productLines.stream().mapToDouble(this::lineCompletedAmount).sum();
                    String name = productLines.stream()
                            .map(RFQOrderLine::getProductName)
                            .filter(Objects::nonNull)
                            .filter(v -> !v.isBlank())
                            .findFirst()
                            .orElse(e.getKey());
                    return SupplierInsightsReportDto.TopProductPoint.builder()
                            .productId(e.getKey())
                            .productName(name)
                            .requestedCount(productLines.size())
                            .totalSalesAmount(sales)
                            .statusCounts(statusCounts)
                            .build();
                })
                .toList();
    }

    private List<SupplierInsightsReportDto.TopCustomerPoint> buildTopCustomersInsights(List<RFQOrderLine> lines) {
        Map<String, List<RFQOrderLine>> byCustomer = lines.stream()
                .collect(Collectors.groupingBy(line -> {
                    String ownerId = line.getCustomerOwnerId() != null ? line.getCustomerOwnerId().trim() : null;
                    if (ownerId != null && !ownerId.isBlank()) return ownerId;

                    String orgCrn = line.getCustomerOrganizationCRN() != null ? line.getCustomerOrganizationCRN().trim() : null;
                    if (orgCrn != null && !orgCrn.isBlank()) return "CRN::" + orgCrn;

                    String orgName = line.getCustomerOrganizationName() != null ? line.getCustomerOrganizationName().trim() : null;
                    if (orgName != null && !orgName.isBlank()) return "ORG::" + orgName;

                    return "UNKNOWN";
                }));

        return byCustomer.entrySet().stream()
                .filter(e -> !"UNKNOWN".equals(e.getKey()))
                .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
                .limit(5)
                .map(e -> {
                    List<RFQOrderLine> customerLines = e.getValue();
                    Map<String, Long> statusCounts = customerLines.stream()
                            .collect(Collectors.groupingBy(
                                    l -> l.getStatus() != null ? l.getStatus().name() : "UNKNOWN",
                                    LinkedHashMap::new,
                                    Collectors.counting()
                            ));
                    double sales = customerLines.stream().mapToDouble(this::lineCompletedAmount).sum();
                    String customerKey = e.getKey();
                    String customerName = customerLines.stream()
                            .map(RFQOrderLine::getCustomerOrganizationName)
                            .filter(Objects::nonNull)
                            .filter(v -> !v.isBlank())
                            .findFirst()
                            .orElse(customerKey.startsWith("ORG::") ? customerKey.replaceFirst("^ORG::", "") : customerKey);

                    return SupplierInsightsReportDto.TopCustomerPoint.builder()
                            .customerKey(customerKey)
                            .customerName(customerName)
                            .requestedCount(customerLines.size())
                            .totalSalesAmount(sales)
                            .statusCounts(statusCounts)
                            .build();
                })
                .toList();
    }

    private List<SupplierInsightsReportDto.StatusDistributionPoint> buildStatusDistributionInsights(List<RFQOrderLine> lines) {
        int total = lines.size();
        Map<String, Long> grouped = lines.stream()
                .collect(Collectors.groupingBy(
                        l -> l.getStatus() != null ? l.getStatus().name() : "UNKNOWN",
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        return grouped.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(e -> SupplierInsightsReportDto.StatusDistributionPoint.builder()
                        .status(e.getKey())
                        .count(e.getValue().intValue())
                        .percentage(total > 0 ? (e.getValue() * 100.0) / total : 0.0)
                        .build())
                .toList();
    }

    private List<SupplierInsightsReportDto.ValueBucketPoint> buildCompletedValueBuckets(List<RFQOrderLine> lines) {
        Map<String, List<Double>> buckets = new LinkedHashMap<>();
        buckets.put("0-499", new ArrayList<>());
        buckets.put("500-1999", new ArrayList<>());
        buckets.put("2000-4999", new ArrayList<>());
        buckets.put("5000+", new ArrayList<>());

        lines.stream()
                .map(this::lineCompletedAmount)
                .filter(v -> v > 0)
                .forEach(value -> {
                    if (value < 500) buckets.get("0-499").add(value);
                    else if (value < 2000) buckets.get("500-1999").add(value);
                    else if (value < 5000) buckets.get("2000-4999").add(value);
                    else buckets.get("5000+").add(value);
                });

        return buckets.entrySet().stream()
                .map(e -> SupplierInsightsReportDto.ValueBucketPoint.builder()
                        .bucketLabel(e.getKey())
                        .count(e.getValue().size())
                        .totalSalesAmount(e.getValue().stream().mapToDouble(Double::doubleValue).sum())
                        .build())
                .toList();
    }

    private String assertSupplierAndFeatureAccess() {
        String token = messagesUtil.getAuthToken();
        Role role = tokenProvider.getRoleFromToken(token);

        if (role != Role.SUPPLIER_OWNER && role != Role.SUPPLIER_STAFF) {
            throw new AbstractException(messagesUtil.getMessage("UNAUTHORIZED"));
        }

        String supplierOwnerId = tokenProvider.getOwnerIdFromToken(token);
        UserSubscription subscription = subscriptionRepository.findFirstByUserId(supplierOwnerId).orElse(null);
        if (subscription == null || subscription.getSelectedFeatures() == null
                || !subscription.getSelectedFeatures().contains(PlanFeatures.SUPPLIER_ADVANCED_REPORTS)) {
            throw new AbstractException(messagesUtil.getMessage("FEATURE_NOT_AVAILABLE"));
        }

        return supplierOwnerId;
    }
}
