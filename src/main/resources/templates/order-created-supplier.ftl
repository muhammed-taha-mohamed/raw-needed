<!DOCTYPE html>
<html lang="ar">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>طلب جديد | New Order</title>
    <#include "includes/email-brand.ftl">
</head>
<body>
<div class="container">
    <#if logoUrl?? && logoUrl?has_content>
    <div class="logo"><img src="${logoUrl}" alt="Logo"/></div>
    </#if>

    <div class="section" dir="rtl">
        <h2>طلب جديد</h2>
        <p>عزيزي المورد <strong>${supplierName!''}</strong>،</p>
        <p>تم استلام طلب جديد من العميل. تفاصيل الطلب أدناه:</p>
        <p><strong>رقم الطلب:</strong> ${orderNumber!''}</p>
        <p><strong>العميل / المنظمة:</strong> ${customerName!''} - ${customerOrg!''}</p>
        <p>المنتجات المطلوبة منكم:</p>
        <table dir="rtl">
            <thead><tr><th>الكمية</th><th>المنتج</th></tr></thead>
            <tbody>
            <#list items as item>
                <tr><td>${item.quantity!''}</td><td>${item.productName!''}</td></tr>
            </#list>
            </tbody>
        </table>
        <p>مع تحيات فريق Raw Needed</p>
    </div>

    <hr class="divider"/>

    <div class="section" dir="ltr">
        <h2>New Order</h2>
        <p>Dear Supplier <strong>${supplierName!''}</strong>,</p>
        <p>A new order has been received from the customer. Order details below:</p>
        <p><strong>Order number:</strong> ${orderNumber!''}</p>
        <p><strong>Customer / Organization:</strong> ${customerName!''} - ${customerOrg!''}</p>
        <p>Products requested from you:</p>
        <table>
            <thead><tr><th>Product</th><th>Quantity</th></tr></thead>
            <tbody>
            <#list items as item>
                <tr><td>${item.productName!''}</td><td>${item.quantity!''}</td></tr>
            </#list>
            </tbody>
        </table>
        <p>Best regards, Raw Needed Team</p>
    </div>

    <div class="footer">للاستفسارات | For inquiries: info@rawneeded.com</div>
</div>
</body>
</html>
