<!DOCTYPE html>
<html lang="ar">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>رد الإدارة على الشكوى | Admin Reply</title>
    <#include "includes/email-brand.ftl">
</head>
<body>
<div class="container">
    <#if logoUrl?? && logoUrl?has_content>
    <div class="logo"><img src="${logoUrl}" alt="Logo"/></div>
    </#if>

    <div class="section" dir="rtl">
        <h2>رد الإدارة على الشكوى</h2>
        <p>عزيزي/تي <strong>${userName!''}</strong>،</p>
        <p>قامت الإدارة بالرد على شكواك بموضوع «<strong>${subject!''}</strong>».</p>
        <p>يرجى تسجيل الدخول لقراءة الرد والمتابعة.</p>
        <p>مع تحيات فريق Raw Needed</p>
    </div>

    <hr class="divider"/>

    <div class="section" dir="ltr">
        <h2>Admin Reply to Your Complaint</h2>
        <p>Dear <strong>${userName!''}</strong>,</p>
        <p>Admin has replied to your complaint regarding "<strong>${subject!''}</strong>".</p>
        <p>Please log in to read the reply and follow up.</p>
        <p>Best regards, Raw Needed Team</p>
    </div>

    <div class="footer">للاستفسارات | For inquiries: info@rawneeded.com</div>
</div>
</body>
</html>
