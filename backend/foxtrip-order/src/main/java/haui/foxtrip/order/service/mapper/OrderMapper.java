package haui.foxtrip.order.service.mapper;

import haui.foxtrip.order.domain.Order;
import haui.foxtrip.order.domain.OrderAddon;
import haui.foxtrip.order.domain.OrderItem;
import haui.foxtrip.order.domain.Refund;
import haui.foxtrip.order.service.dto.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    // Order mappings
    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "orderCode", source = "orderCode")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "totalAmount", source = "totalAmount")
    @Mapping(target = "expiresAt", source = "expiresAt")
    CreateOrderResDTO toCreateOrderResDTO(Order order);

    // MyOrderListItemDTO mappings
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderCode", source = "order.orderCode")
    @Mapping(target = "status", source = "order.status")
    @Mapping(target = "totalAmount", source = "order.totalAmount")
    @Mapping(target = "createdAt", source = "order.createdAt")
    @Mapping(target = "expiresAt", source = "order.expiresAt")
    @Mapping(target = "paidAt", source = "order.paidAt")
    @Mapping(target = "tour", source = "orderItem")
    @Mapping(target = "quantity", source = "orderItem.quantity")
    MyOrderListItemDTO toMyOrderListItemDTO(Order order, OrderItem orderItem);

    @Mapping(target = "tourId", source = "tourId")
    @Mapping(target = "tourNameAtTime", source = "tourNameAtTime")
    @Mapping(target = "startDateAtTime", source = "startDateAtTime")
    @Mapping(target = "endDateAtTime", source = "endDateAtTime")
    @Mapping(target = "thumbnailAtTime", source = "thumbnailAtTime")
    MyOrderListItemDTO.TourSnapshotDTO toTourSnapshotDTO(OrderItem orderItem);

    // AdminOrderListItemDTO mappings
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderCode", source = "order.orderCode")
    @Mapping(target = "status", source = "order.status")
    @Mapping(target = "totalAmount", source = "order.totalAmount")
    @Mapping(target = "createdAt", source = "order.createdAt")
    @Mapping(target = "expiresAt", source = "order.expiresAt")
    @Mapping(target = "paidAt", source = "order.paidAt")
    @Mapping(target = "customerName", source = "order.customerName")
    @Mapping(target = "customerPhone", source = "order.customerPhone")
    @Mapping(target = "customerEmail", source = "order.customerEmail")
    @Mapping(target = "tourNameAtTime", source = "orderItem.tourNameAtTime")
    @Mapping(target = "startDateAtTime", source = "orderItem.startDateAtTime")
    @Mapping(target = "quantity", source = "orderItem.quantity")
    AdminOrderListItemDTO toAdminOrderListItemDTO(Order order, OrderItem orderItem);

    // OrderDetailResDTO mappings
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderCode", source = "order.orderCode")
    @Mapping(target = "status", source = "order.status")
    @Mapping(target = "totalAmount", source = "order.totalAmount")
    @Mapping(target = "createdAt", source = "order.createdAt")
    @Mapping(target = "expiresAt", source = "order.expiresAt")
    @Mapping(target = "paidAt", source = "order.paidAt")
    @Mapping(target = "customerName", source = "order.customerName")
    @Mapping(target = "customerPhone", source = "order.customerPhone")
    @Mapping(target = "customerEmail", source = "order.customerEmail")
    @Mapping(target = "tour", source = "orderItem")
    @Mapping(target = "quantity", source = "orderItem.quantity")
    @Mapping(target = "addons", ignore = true)
    @Mapping(target = "qrPayload", ignore = true)
    OrderDetailResDTO toOrderDetailResDTO(Order order, OrderItem orderItem);

    @Mapping(target = "tourId", source = "tourId")
    @Mapping(target = "tourNameAtTime", source = "tourNameAtTime")
    @Mapping(target = "startDateAtTime", source = "startDateAtTime")
    @Mapping(target = "endDateAtTime", source = "endDateAtTime")
    @Mapping(target = "thumbnailAtTime", source = "thumbnailAtTime")
    OrderDetailResDTO.TourSnapshotDTO toOrderDetailTourSnapshotDTO(OrderItem orderItem);

    @Mapping(target = "tourAddonId", source = "addon.tourAddonId")
    @Mapping(target = "nameAtTime", source = "nameAtTime")
    @Mapping(target = "unitPriceAtTime", source = "addon.priceAtTime")
    @Mapping(target = "quantity", source = "addon.quantity")
    OrderDetailResDTO.AddonSnapshotDTO toAddonSnapshotDTO(OrderAddon addon, String nameAtTime);

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "orderCode", source = "order.orderCode")
    @Mapping(target = "expiresAt", source = "order.expiresAt")
    @Mapping(target = "paymentUrl", source = "paymentUrl")
    PaymentInitResDTO toPaymentInitResDTO(Order order, String paymentUrl);

    // Refund mappings
    @Mapping(target = "refundId", source = "id")
    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "reason", source = "reason")
    @Mapping(target = "processedAt", source = "processedAt")
    RefundResDTO toRefundResDTO(Refund refund);
}
