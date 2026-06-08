package haui.foxtrip.order.service;

import haui.foxtrip.order.service.dto.CreateRefundReqDTO;
import haui.foxtrip.order.service.dto.RefundResDTO;
import haui.foxtrip.order.service.dto.UpdateRefundReqDTO;

import java.util.UUID;

public interface RefundService {
    
    RefundResDTO createRefund(UUID orderId, CreateRefundReqDTO request);
    
    RefundResDTO updateRefund(UUID refundId, UpdateRefundReqDTO request);
}
