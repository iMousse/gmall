package com.example.gmallorder.vo;

import com.example.gmall.ums.entity.MemberReceiveAddressEntity;
import lombok.Data;

import java.util.List;

@Data
public class OrderConfirmVO {

    private List<MemberReceiveAddressEntity> addresses;

    private List<OrderItemVO> orderItems;

    private Integer bounds;

    //订单唯一标志，防止重复提交
    private String orderToken;


}
