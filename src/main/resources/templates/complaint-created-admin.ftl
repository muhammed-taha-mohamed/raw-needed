<!DOCTYPE html>
<html lang="ar">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>شكوى جديدة | New Complaint</title>
    <#include "includes/email-brand.ftl">
</head>
<body>
<div class="container">
    <#if logoUrl?? && logoUrl?has_content>
    <div class="logo"><img src="${logoUrl}" alt="Logo"/></div>
    </#if>

    <div class="section" dir="rtl">
        <h2>شكوى جديدة</h2>
        <p>تم تقديم شكوى جديدة من المستخدم <strong>${userName!''}</strong>.</p>
        <p><strong>الموضوع:</strong> ${subject!''}</p>
        <p><strong>نص الشكوى:</strong></p>
        <div class="message-box" dir="rtl">${description!''}</div>
        <p>يرجى تسجيل الدخول لمعالجة الشكوى.</p>
        <p>مع تحيات فريق Raw Needed</p>
    </div>

    <hr class="divider"/>

    <div class="section" dir="ltr">
        <h2>New Complaint</h2>
        <p>A new complaint has been submitted by user <strong>${userName!''}</strong>.</p>
        <p><strong>Subject:</strong> ${subject!''}</p>
        <p><strong>Description:</strong></p>
        <div class="message-box">${description!''}</div>
        <p>Please log in to handle the complaint.</p>
        <p>Best regards, Raw Needed Team</p>
    </div>

    <div class="footer">للاستفسارات | For inquiries: info@rawneeded.com</div>
</div>
</body>
</html>
