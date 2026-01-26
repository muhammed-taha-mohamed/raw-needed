<!DOCTYPE html>
<html lang="ar">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>رسالة جديدة في الطلب | New Order Message</title>
    <#include "includes/email-brand.ftl">
</head>
<body>
<div class="container">
    <#if logoUrl?? && logoUrl?has_content>
    <div class="logo"><img src="${logoUrl}" alt="Logo"/></div>
    </#if>

    <div class="section" dir="rtl">
        <h2>رسالة جديدة في الطلب</h2>
        <p>عزيزي/تي <strong>${recipientName!''}</strong>،</p>
        <p>وصلتك رسالة جديدة في الطلب رقم <strong>${orderNumber!''}</strong> من <strong>${senderName!''}</strong> (${senderOrg!''}):</p>
        <div class="message-box" dir="rtl">${messageText!''}</div>
        <p>يرجى تسجيل الدخول للرد أو متابعة المحادثة.</p>
        <p>مع تحيات فريق Raw Needed</p>
    </div>

    <hr class="divider"/>

    <div class="section" dir="ltr">
        <h2>New Order Message</h2>
        <p>Dear <strong>${recipientName!''}</strong>,</p>
        <p>You have received a new message in order <strong>${orderNumber!''}</strong> from <strong>${senderName!''}</strong> (${senderOrg!''}):</p>
        <div class="message-box">${messageText!''}</div>
        <p>Please log in to reply or continue the conversation.</p>
        <p>Best regards, Raw Needed Team</p>
    </div>

    <div class="footer">للاستفسارات | For inquiries: info@rawneeded.com</div>
</div>
</body>
</html>
