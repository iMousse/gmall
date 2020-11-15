package com.example.gmall.oms.vo;

import com.example.gmall.ums.entity.MemberReceiveAddressEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderSubmitVO {
    private String orderToken;
    private MemberReceiveAddressEntity address;
    private Integer payType;
    private List<OrderItemVO> orderItems;
    private Integer bounds;
    private BigDecimal totalPrice;//校验价格
}
