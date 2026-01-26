<!DOCTYPE html>
<html lang="ar">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>تم إغلاق الشكوى | Complaint Closed</title>
    <#include "includes/email-brand.ftl">
</head>
<body>
<div class="container">
    <#if logoUrl?? && logoUrl?has_content>
    <div class="logo"><img src="${logoUrl}" alt="Logo"/></div>
    </#if>

    <div class="section" dir="rtl">
        <h2>تم إغلاق الشكوى</h2>
        <p>عزيزي/تي <strong>${userName!''}</strong>،</p>
        <p>تم إغلاق شكواك بموضوع «<strong>${subject!''}</strong>» من قبل الإدارة.</p>
        <p>مع تحيات فريق Raw Needed</p>
    </div>

    <hr class="divider"/>

    <div class="section" dir="ltr">
        <h2>Complaint Closed</h2>
        <p>Dear <strong>${userName!''}</strong>,</p>
        <p>Your complaint regarding "<strong>${subject!''}</strong>" has been closed by the admin.</p>
        <p>Best regards, Raw Needed Team</p>
    </div>

    <div class="footer">للاستفسارات | For inquiries: info@rawneeded.com</div>
</div>
</body>
</html>
