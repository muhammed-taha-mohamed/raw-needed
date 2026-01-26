<!DOCTYPE html>
<html lang="ar">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>تم إكمال الطلب | Order Completed</title>
    <#include "includes/email-brand.ftl">
</head>
<body>
<div class="container">
    <#if logoUrl?? && logoUrl?has_content>
    <div class="logo"><img src="${logoUrl}" alt="Logo"/></div>
    </#if>

    <div class="section" dir="rtl">
        <h2>تم إكمال الطلب</h2>
        <p>عزيزي العميل <strong>${customerName!''}</strong>،</p>
        <p>تم تنفيذ الطلب بنجاح. الصنف <strong>${productName!''}</strong> قد تم إكماله من قبل المورد.</p>
        <p>مع تحيات فريق Raw Needed</p>
    </div>

    <hr class="divider"/>

    <div class="section" dir="ltr">
        <h2>Order Completed</h2>
        <p>Dear Customer <strong>${customerName!''}</strong>,</p>
        <p>Your order has been fulfilled. The item <strong>${productName!''}</strong> has been completed by the supplier.</p>
        <p>Best regards, Raw Needed Team</p>
    </div>

    <div class="footer">للاستفسارات | For inquiries: info@rawneeded.com</div>
</div>
</body>
</html>
