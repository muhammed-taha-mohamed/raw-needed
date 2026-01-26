<!DOCTYPE html>
<html lang="ar">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>موافقة على الطلب | Order Approved</title>
    <#include "includes/email-brand.ftl">
</head>
<body>
<div class="container">
    <#if logoUrl?? && logoUrl?has_content>
    <div class="logo"><img src="${logoUrl}" alt="Logo"/></div>
    </#if>

    <div class="section" dir="rtl">
        <h2>موافقة على الطلب</h2>
        <p>عزيزي المورد <strong>${supplierName!''}</strong>،</p>
        <p>قام العميل بالموافقة على عرضكم للمنتج <strong>${productName!''}</strong> في الطلب.</p>
        <p>مع تحيات فريق Raw Needed</p>
    </div>

    <hr class="divider"/>

    <div class="section" dir="ltr">
        <h2>Order Approved</h2>
        <p>Dear Supplier <strong>${supplierName!''}</strong>,</p>
        <p>The customer has approved your offer for <strong>${productName!''}</strong> in the order.</p>
        <p>Best regards, Raw Needed Team</p>
    </div>

    <div class="footer">للاستفسارات | For inquiries: info@rawneeded.com</div>
</div>
</body>
</html>
