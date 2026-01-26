<!DOCTYPE html>
<html lang="ar">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>رد المورد على الطلب | Supplier Response</title>
    <#include "includes/email-brand.ftl">
</head>
<body>
<div class="container">
    <#if logoUrl?? && logoUrl?has_content>
    <div class="logo"><img src="${logoUrl}" alt="Logo"/></div>
    </#if>

    <div class="section" dir="rtl">
        <h2>رد المورد على الطلب</h2>
        <p>عزيزي العميل <strong>${customerName!''}</strong>،</p>
        <p>قام المورد <strong>${supplierName!''}</strong> بالرد على طلبكم بخصوص المنتج <strong>${productName!''}</strong>.</p>
        <p>يرجى تسجيل الدخول للمنصة لمراجعة العرض والموافقة أو التواصل.</p>
        <p>مع تحيات فريق Raw Needed</p>
    </div>

    <hr class="divider"/>

    <div class="section" dir="ltr">
        <h2>Supplier Response</h2>
        <p>Dear Customer <strong>${customerName!''}</strong>,</p>
        <p>Supplier <strong>${supplierName!''}</strong> has responded to your order for <strong>${productName!''}</strong>.</p>
        <p>Please log in to the platform to review the offer and approve or get in touch.</p>
        <p>Best regards, Raw Needed Team</p>
    </div>

    <div class="footer">للاستفسارات | For inquiries: info@rawneeded.com</div>
</div>
</body>
</html>
