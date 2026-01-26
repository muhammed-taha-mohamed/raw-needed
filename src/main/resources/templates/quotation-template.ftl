<!DOCTYPE html>
<html lang="ar">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>طلب عرض أسعار | Quotation Request</title>
    <#include "includes/email-brand.ftl">
    <style>.container { max-width: 900px; } .footer { font-size: 14px; padding-top: 20px; } .divider { margin: 30px 0; }</style>
</head>
<body>

<!-- Container for the content -->
<div class="container">

    <#if logoUrl?? && logoUrl?has_content>
    <div class="logo"><img src="${logoUrl}" alt="Logo"/></div>
    </#if>


    <!-- Arabic Section -->
    <div class="section" dir="rtl">
        <h1>طلب عرض أسعار</h1>
        <p>عزيزي المورد ${supplier_name}،</p>

        <p>تم إرسال طلب عرض أسعار جديد إليك. المنتجات المطلوبة موضحة في الجدول التالي:</p>

        <div class="table-container">
            ${data_ar}
        </div>

        <p>معلومات العميل:</p>
        <ul>
            <li>الاسم: ${customer_name}</li>
            <li>البريد الإلكتروني: ${customer_email}</li>
            <li>رقم الهاتف: ${customer_phone}</li>
        </ul>

        <p>مع خالص التحية،<br/>Raw-Needed Team</p>
    </div>

    <!-- English Section -->
    <div class="divider"></div>

    <div class="section" dir="ltr">
        <h1>Quotation Request</h1>
        <p>Dear ${supplier_name},</p>

        <p>You have received a new quotation request. Please find the requested products in the table below:</p>

        <div class="table-container">
            ${data}
        </div>

        <p>Customer Information:</p>
        <ul>
            <li>Name: ${customer_name}</li>
            <li>Email: ${customer_email}</li>
            <li>Phone: ${customer_phone}</li>
        </ul>

        <p>Best regards,<br/>Raw-Needed Team</p>
    </div>

    <div class="footer">
        <p>For any inquiries, please contact us at rawneeded@gmail.com</p>
    </div>
</div>

</body>
</html>
