package com.rawneeded.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderMessageRequestDto {
    @NotBlank(message = "Message is required")
    private String message;
    
    private String image;
}



ضيف اوبشن شات مع الطلبات بين المورد والعمليل مع كل طلب
يظهر وقت ما تظهر ايقونه التفاوض يعني لما المورد يرد او حاله
ال apis
get messages by order :
GET : https://45.94.58.36:8787/raw-needed/api/v1/orders/order-id/messages
response :
list of :
public class OrderMessageResponseDto {
private String id;
private String orderId;
private String userId;
private String userName;
private String userOrganizationName;
private String message;
private String image;
private LocalDateTime createdAt;
}
ADD MESSAGE
POST
https://45.94.58.36:8787/raw-needed/api/v1/orders/orderid/messages
BODY : {
"message": "string",
"image": "string"
}