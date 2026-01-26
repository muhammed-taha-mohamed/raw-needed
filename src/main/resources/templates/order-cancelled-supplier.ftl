<!DOCTYPE html>
<html lang="ar">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>تم إلغاء الطلب | Order Cancelled</title>
    <#include "includes/email-brand.ftl">
</head>
<body>
<div class="container">
    <#if logoUrl?? && logoUrl?has_content>
    <div class="logo"><img src="${logoUrl}" alt="Logo"/></div>
    </#if>

    <div class="section" dir="rtl">
        <h2>تم إلغاء الطلب</h2>
        <p>عزيزي المورد <strong>${supplierName!''}</strong>،</p>
        <p>تم إلغاء الطلب رقم <strong>${orderNumber!''}</strong> من قبل العميل.</p>
        <p>مع تحيات فريق Raw Needed</p>
    </div>

    <hr class="divider"/>

    <div class="section" dir="ltr">
        <h2>Order Cancelled</h2>
        <p>Dear Supplier <strong>${supplierName!''}</strong>,</p>
        <p>Order number <strong>${orderNumber!''}</strong> has been cancelled by the customer.</p>
        <p>Best regards, Raw Needed Team</p>
    </div>

    <div class="footer">للاستفسارات | For inquiries: info@rawneeded.com</div>
</div>
</body>
</html>
