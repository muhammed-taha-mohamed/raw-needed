# برومبت لتحديث الفرونت إند - Raw Needed

## التغييرات المطلوبة في الفرونت إند

### 1. تحديث نموذج المنتج (Product Form)

#### إضافة حقول جديدة (اختيارية):
- **وحدة القياس (Unit)**: حقل نصي اختياري
- **تاريخ الإنتاج (Production Date)**: حقل تاريخ اختياري
- **تاريخ الانتهاء (Expiration Date)**: حقل تاريخ اختياري

#### تحديثات واجهة المستخدم:
- إضافة هذه الحقول في نموذج إنشاء/تعديل المنتج
- الحقول اختيارية (optional)
- استخدام date picker للحقول التاريخية
- عرض الحقول في قائمة المنتجات وتفاصيل المنتج

---

### 2. التحقق من الاسم الفريد لكل مورد

#### رسالة الخطأ:
عند محاولة إنشاء أو تعديل منتج بنفس الاسم لمورد موجود:
- **الإنجليزية**: "A product with this name already exists for this supplier"
- **العربية**: "منتج بهذا الاسم موجود بالفعل لهذا المورد"

#### التنفيذ:
- إظهار رسالة خطأ عند محاولة حفظ منتج بنفس الاسم
- التحقق من الاسم قبل الإرسال (client-side validation اختياري)
- عرض رسالة الخطأ من الـ API response

---

### 3. ميزة تصدير المخزون (Export Stock)

#### Endpoint الجديد:
```
GET /api/v1/product/export-stock
```

#### المتطلبات:
- متاح فقط للموردين (SUPPLIER_OWNER و SUPPLIER_STAFF)
- يعيد ملف Excel (.xlsx) للتحميل
- اسم الملف: `stock-report-YYYY-MM-DD-HHmmss.xlsx`

#### التنفيذ في الفرونت إند:

1. **إضافة زر/أيقونة تصدير**:
   - في صفحة قائمة المنتجات للمورد
   - في Dashboard المورد
   - يمكن أن يكون في شريط الأدوات أو قائمة Actions

2. **وظيفة التصدير**:
   ```javascript
   // مثال باستخدام axios
   const exportStock = async () => {
     try {
       const response = await axios.get('/api/v1/product/export-stock', {
         responseType: 'blob',
         headers: {
           'Authorization': `Bearer ${token}`
         }
       });
       
       // إنشاء رابط تحميل
       const url = window.URL.createObjectURL(new Blob([response.data]));
       const link = document.createElement('a');
       link.href = url;
       link.setAttribute('download', `stock-report-${new Date().toISOString().slice(0,10)}.xlsx`);
       document.body.appendChild(link);
       link.click();
       link.remove();
       
       // إظهار رسالة نجاح
       showSuccessMessage('Stock exported successfully');
     } catch (error) {
       showErrorMessage('Failed to export stock');
     }
   };
   ```

3. **تصميم الزر**:
   - أيقونة Excel أو Download
   - نص: "Export Stock" / "تصدير المخزون"
   - يمكن أن يكون في شريط الأدوات مع باقي الأزرار

4. **حالات التحميل**:
   - إظهار loading indicator أثناء التصدير
   - إظهار رسالة نجاح بعد التحميل
   - معالجة الأخطاء بشكل مناسب

---

## تفاصيل ملف Excel المُصدّر

### الأعمدة:
1. # (رقم تسلسلي)
2. Product Name (اسم المنتج)
3. Origin (الأصل)
4. Category (الفئة)
5. Sub Category (الفئة الفرعية)
6. Unit (وحدة القياس)
7. In Stock (متوفر في المخزون)
8. Stock Quantity (الكمية في المخزون)
9. Production Date (تاريخ الإنتاج)
10. Expiration Date (تاريخ الانتهاء)

### التصميم:
- العنوان: خلفية داكنة (#003259) مع نص أبيض
- رؤوس الأعمدة: خلفية فاتحة (#009aa7) مع نص أبيض
- البيانات: حدود رفيعة وتنسيق منظم
- ألوان المخطط: #009aa7 و #003259

---

## نقاط مهمة للتنفيذ

### 1. تحديث Product Interface/Type:
```typescript
interface Product {
  id: string;
  name: string;
  origin?: string;
  supplierId: string;
  supplierName?: string;
  inStock: boolean;
  stockQuantity?: number;
  category?: Category;
  subCategory?: SubCategory;
  image?: string;
  unit?: string;              // جديد
  productionDate?: string;    // جديد (ISO date format)
  expirationDate?: string;    // جديد (ISO date format)
}
```

### 2. تحديث Product Form:
- إضافة الحقول الجديدة في نموذج الإنشاء/التعديل
- استخدام date picker للحقول التاريخية
- التأكد من أن الحقول اختيارية

### 3. تحديث Product List/Table:
- إضافة أعمدة جديدة للعرض (اختياري)
- إظهار التواريخ بصيغة قابلة للقراءة
- إظهار وحدة القياس

### 4. إضافة Export Button:
- في صفحة المنتجات للمورد
- في Dashboard المورد
- مع معالجة الأخطاء والتحميل

### 5. معالجة الأخطاء:
- رسالة الخطأ عند الاسم المكرر
- رسالة الخطأ عند فشل التصدير
- التحقق من الصلاحيات (فقط للموردين)

---

## أمثلة الكود

### React Component Example:
```tsx
import { useState } from 'react';
import axios from 'axios';

const ProductExportButton = () => {
  const [loading, setLoading] = useState(false);

  const handleExport = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get('/api/v1/product/export-stock', {
        responseType: 'blob',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      const fileName = `stock-report-${new Date().toISOString().slice(0,10)}.xlsx`;
      link.setAttribute('download', fileName);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
      
      // Show success message
      alert('Stock exported successfully!');
    } catch (error) {
      console.error('Export error:', error);
      alert('Failed to export stock. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <button 
      onClick={handleExport} 
      disabled={loading}
      className="export-button"
    >
      {loading ? 'Exporting...' : 'Export Stock'}
    </button>
  );
};
```

### Product Form Example:
```tsx
const ProductForm = ({ product, onSubmit }) => {
  const [formData, setFormData] = useState({
    name: product?.name || '',
    origin: product?.origin || '',
    unit: product?.unit || '',                    // جديد
    productionDate: product?.productionDate || '', // جديد
    expirationDate: product?.expirationDate || '', // جديد
    // ... باقي الحقول
  });

  return (
    <form onSubmit={handleSubmit}>
      {/* الحقول الموجودة */}
      
      {/* حقل وحدة القياس */}
      <div className="form-group">
        <label>Unit (Optional)</label>
        <input
          type="text"
          value={formData.unit}
          onChange={(e) => setFormData({...formData, unit: e.target.value})}
          placeholder="e.g., kg, liter, piece"
        />
      </div>

      {/* حقل تاريخ الإنتاج */}
      <div className="form-group">
        <label>Production Date (Optional)</label>
        <input
          type="date"
          value={formData.productionDate}
          onChange={(e) => setFormData({...formData, productionDate: e.target.value})}
        />
      </div>

      {/* حقل تاريخ الانتهاء */}
      <div className="form-group">
        <label>Expiration Date (Optional)</label>
        <input
          type="date"
          value={formData.expirationDate}
          onChange={(e) => setFormData({...formData, expirationDate: e.target.value})}
        />
      </div>

      <button type="submit">Save Product</button>
    </form>
  );
};
```

---

## ملاحظات إضافية

1. **التحقق من الصلاحيات**: التأكد من أن زر التصدير يظهر فقط للموردين
2. **التواريخ**: استخدام date picker مناسب للغة (عربي/إنجليزي)
3. **التنسيق**: عرض التواريخ بصيغة قابلة للقراءة (dd/MM/yyyy)
4. **التحقق من الاسم**: يمكن إضافة client-side validation للتحقق من الاسم قبل الإرسال
5. **UX**: إظهار loading state أثناء التصدير لتحسين تجربة المستخدم

---

## API Endpoints Summary

### Updated Endpoints:
- `POST /api/v1/product` - Create product (now includes unit, productionDate, expirationDate)
- `PATCH /api/v1/product/{id}` - Update product (now includes unit, productionDate, expirationDate)
- `POST /api/v1/product/filter` - Filter products (response includes new fields)

### New Endpoint:
- `GET /api/v1/product/export-stock` - Export stock to Excel (Supplier only)

### Error Messages:
- `PRODUCT_NAME_EXISTS_FOR_SUPPLIER`: When trying to create/update product with duplicate name
- `EXPORT_STOCK_FAIL`: When export fails
- `UNAUTHORIZED_ACCESS`: When non-supplier tries to export

---

## Checklist للتنفيذ

- [ ] تحديث Product interface/type مع الحقول الجديدة
- [ ] إضافة الحقول الجديدة في نموذج المنتج
- [ ] تحديث عرض المنتجات لإظهار الحقول الجديدة
- [ ] إضافة معالجة رسالة الخطأ للاسم المكرر
- [ ] إضافة زر تصدير المخزون
- [ ] تنفيذ وظيفة التصدير
- [ ] إضافة loading state للتصدير
- [ ] إضافة رسائل النجاح/الفشل
- [ ] التحقق من الصلاحيات (Supplier only)
- [ ] اختبار جميع الوظائف

---

## 4. ميزة تحميل القالب ورفع المنتجات (Bulk Upload)

### نظرة عامة:
المورد يمكنه تحميل قالب Excel، تعبئته بالمنتجات، ثم رفعه لإضافة جميع المنتجات دفعة واحدة.

---

### 4.1 تحميل القالب (Download Template)

#### Endpoint:
```
GET /api/v1/product/download-template
```

#### المتطلبات:
- متاح فقط للموردين (SUPPLIER_OWNER و SUPPLIER_STAFF)
- يعيد ملف Excel (.xlsx) للتحميل
- اسم الملف: `products-template.xlsx`

#### محتوى الملف:
الملف يحتوي على **شيتين مرئيين** و **شيتين مخفيين**:

**Sheet 1: "Products"** (الشيت الرئيسي)
- صف عنوان: "Products Template - Select Category and SubCategory from dropdown lists..."
- صف رؤوس الأعمدة:
  - Product Name* (مطلوب - نص حر)
  - Origin (اختياري - نص حر)
  - **Category Name* (مطلوب - قائمة منسدلة)** 🎯
  - **SubCategory Name* (مطلوب - قائمة منسدلة تعتمد على الفئة)** 🎯
  - Unit (اختياري - نص حر)
  - **In Stock (Yes/No) (قائمة منسدلة: Yes/No)** 🎯
  - Stock Quantity (اختياري - رقم)
  - Production Date (dd/MM/yyyy) (اختياري - تاريخ)
  - Expiration Date (dd/MM/yyyy) (اختياري - تاريخ)
- صف مثال (يمكن حذفه)

**ميزات القوائم المنسدلة:**
- **Category Name**: قائمة منسدلة تحتوي على جميع الفئات (يظهر الـ **Name** فقط للمستخدم)
- **SubCategory Name**: قائمة منسدلة **تعتمد على الفئة المختارة** (يظهر الـ **Name** فقط)
- **In Stock**: قائمة منسدلة (Yes/No)
- **أعمدة مخفية**: عمود "Category ID" و "SubCategory ID" مخفيان في الشيت، ويتم تعبئتهما تلقائياً بصيغ Excel (VLOOKUP) عند اختيار الاسم من القائمة. **اللي يتبعت للباك إند هو الـ ID** — الباك يقرأ الـ ID من الملف إن وُجد، وإلا يحوّل الاسم إلى ID.

**Sheet 2: "Categories Reference"** (للرجوع فقط)
- قائمة بجميع الفئات والفرعية المتاحة
- الأعمدة: Category Name | SubCategory Name
- **ملاحظة**: هذا الشيت للرجوع فقط - استخدم القوائم المنسدلة في Sheet "Products"

**الشيتات المخفية** (للاستخدام الداخلي):
- `_Categories`: قائمة الفئات للقائمة المنسدلة
- `_SubCategories`: قائمة الفرعيات مع الفئات (للاستخدام في dependent dropdown)

#### التنفيذ في الفرونت إند:

1. **إضافة زر تحميل القالب**:
   - في صفحة المنتجات للمورد
   - بجانب زر "Export Stock"
   - نص: "Download Template" / "تحميل القالب"

2. **وظيفة التحميل**:
   ```javascript
   const downloadTemplate = async () => {
     try {
       const token = localStorage.getItem('token');
       const response = await axios.get('/api/v1/product/download-template', {
         responseType: 'blob',
         headers: {
           'Authorization': `Bearer ${token}`
         }
       });

       const url = window.URL.createObjectURL(new Blob([response.data]));
       const link = document.createElement('a');
       link.href = url;
       link.setAttribute('download', 'products-template.xlsx');
       document.body.appendChild(link);
       link.click();
       link.remove();
       window.URL.revokeObjectURL(url);
       
       showSuccessMessage('Template downloaded successfully');
     } catch (error) {
       showErrorMessage('Failed to download template');
     }
   };
   ```

---

### 4.2 رفع المنتجات (Upload Products)

#### مهم — عرض النتيجة في بوب أب:
- الـ API **دائماً يرجع HTTP 200** حتى لو كل الصفوط فشلت (مثلاً: "اسم الفئة مطلوب").
- الـ response يكون: `content.success: true` و `content.data` فيه:
  - `totalRows`, `successCount`, `failedCount`, `errors[]`.
- **يجب عرض هذه النتيجة في بوب أب (Modal)** بعد كل رفع:
  - ملخص: إجمالي الصفوط، تمت بنجاح، فشل.
  - إذا `failedCount > 0`: جدول بالأخطاء (رقم الصف، اسم المنتج، رسالة الخطأ).
- رسالة الخطأ قد تكون عربي (مثل "اسم الفئة مطلوب") أو إنجليزي حسب لغة الـ backend.

#### Endpoint:
```
POST /api/v1/product/upload-products
Content-Type: multipart/form-data
```

#### Parameters:
- `file`: ملف Excel (.xlsx) - الملف المعبأ من القالب

#### Response (مهم):
- **الـ API دائماً يرجع HTTP 200** حتى لو فيه صفوط فشلت (validation errors).
- الـ response body يكون بهذا الشكل:
```json
{
  "date": "2026-02-02 14:44:57",
  "content": {
    "success": true,
    "data": {
      "totalRows": 1,
      "successCount": 0,
      "failedCount": 1,
      "errors": [
        {
          "rowNumber": 3,
          "productName": "Example Product",
          "errorMessage": "اسم الفئة مطلوب"
        }
      ]
    }
  }
}
```
- **يجب التحقق من `content.data`**: إذا `failedCount > 0` أو `successCount < totalRows` معناه فيه أخطاء — اعرضها في **بوب أب**.

```typescript
interface BulkUploadResult {
  totalRows: number;        // عدد الصفوط التي تحتوي على اسم منتج
  successCount: number;     // عدد المنتجات المُنشأة بنجاح
  failedCount: number;      // عدد المنتجات التي فشلت
  errors: Array<{
    rowNumber: number;       // رقم الصف في Excel (يبدأ من 1)
    productName: string;     // اسم المنتج
    errorMessage: string;    // رسالة الخطأ (قد تكون عربي أو إنجليزي)
  }>;
}
```

#### التنفيذ في الفرونت إند:

1. **إضافة زر/منطقة رفع الملف**:
   - في صفحة المنتجات للمورد
   - يمكن أن يكون:
     - زر "Upload Products" / "رفع المنتجات" يفتح file picker
     - أو drag & drop area

2. **وظيفة الرفع** (الـ API يرجع 200 دائماً — تحقق من `failedCount`):
   ```javascript
   const uploadProducts = async (file) => {
     if (!file) {
       showErrorMessage('Please select a file');
       return;
     }

     if (!file.name.endsWith('.xlsx')) {
       showErrorMessage('Please upload an Excel file (.xlsx)');
       return;
     }

     setUploading(true);
     try {
       const token = localStorage.getItem('token');
       const formData = new FormData();
       formData.append('file', file);

       const response = await axios.post('/api/v1/product/upload-products', formData, {
         headers: {
           'Authorization': `Bearer ${token}`,
           'Content-Type': 'multipart/form-data'
         }
       });

       // الـ API يرجع 200 حتى مع وجود أخطاء — النتيجة في content.data
       const result = response.data.content.data;
       
       if (result.successCount > 0) {
         showSuccessMessage(
           `تم إضافة ${result.successCount} منتج بنجاح`
         );
         refreshProductsList();
       }

       // إذا فيه فشل — اعرض بوب أب بالأخطاء (دائماً عند failedCount > 0)
       if (result.failedCount > 0) {
         showUploadResultPopup(result);
       }
     } catch (error) {
       showErrorMessage('Failed to upload products');
     } finally {
       setUploading(false);
     }
   };
   ```

3. **بوب أب عرض نتيجة الرفع والأخطاء** (مطلوب):
   اعرض النتيجة في **بوب أب (Modal)** يحتوي على:
   - ملخص: عدد الصفوط، عدد النجاح، عدد الفشل.
   - إذا `failedCount > 0`: جدول بالأخطاء (رقم الصف، اسم المنتج، رسالة الخطأ).
   - زر إغلاق.

   ```tsx
   const UploadResultPopup = ({ result, onClose }) => {
     const { totalRows, successCount, failedCount, errors } = result;
     
     return (
       <Modal onClose={onClose}>
         <div className="upload-result-popup">
           <h2>نتيجة رفع المنتجات</h2>
           
           <div className="result-summary">
             <p>إجمالي الصفوط: <strong>{totalRows}</strong></p>
             <p className="success">تمت بنجاح: <strong>{successCount}</strong></p>
             <p className="error">فشل: <strong>{failedCount}</strong></p>
           </div>

           {failedCount > 0 && errors?.length > 0 && (
             <div className="errors-section">
               <h3>تفاصيل الأخطاء</h3>
               <div className="errors-table-wrapper">
                 <table>
                   <thead>
                     <tr>
                       <th>رقم الصف</th>
                       <th>اسم المنتج</th>
                       <th>رسالة الخطأ</th>
                     </tr>
                   </thead>
                   <tbody>
                     {errors.map((error, index) => (
                       <tr key={index}>
                         <td>{error.rowNumber}</td>
                         <td>{error.productName}</td>
                         <td className="error-text">{error.errorMessage}</td>
                       </tr>
                     ))}
                   </tbody>
                 </table>
               </div>
             </div>
           )}

           <button type="button" onClick={onClose} className="btn-primary">
             إغلاق
           </button>
         </div>
       </Modal>
     );
   };
   ```

   **متى تفتح البوب أب:**
   - بعد أي رفع (status 200) إذا `failedCount > 0` — ليعرف المستخدم أي صفوط فشلت ولماذا.
   - أو يمكن فتحه دائماً بعد الرفع وتعرض فيه الملخص + الأخطاء إن وجدت.

4. **تصميم واجهة الرفع**:
   ```tsx
   const BulkUploadSection = () => {
     const [uploading, setUploading] = useState(false);
     const [uploadErrors, setUploadErrors] = useState(null);

     const handleFileSelect = (event) => {
       const file = event.target.files[0];
       if (file) {
         uploadProducts(file);
       }
     };

     return (
       <div className="bulk-upload-section">
         <h3>Bulk Upload Products</h3>
         
         <div className="upload-actions">
           <button onClick={downloadTemplate} className="btn-secondary">
             Download Template
           </button>
           
           <label className="btn-primary">
             {uploading ? 'Uploading...' : 'Upload Products'}
             <input
               type="file"
               accept=".xlsx"
               onChange={handleFileSelect}
               disabled={uploading}
               style={{ display: 'none' }}
             />
           </label>
         </div>

         {uploading && <LoadingSpinner />}

         {uploadErrors && (
           <UploadErrorsModal 
             errors={uploadErrors} 
             onClose={() => setUploadErrors(null)} 
           />
         )}
       </div>
     );
   };
   ```

---

### 4.3 قواعد التحقق (Validation Rules)

#### عند الرفع:
1. **Product Name**: مطلوب - لا يمكن أن يكون فارغاً
2. **Category Name**: مطلوب - يجب أن يطابق اسم فئة موجودة (case-insensitive)
3. **SubCategory Name**: مطلوب - يجب أن يطابق اسم فرعية موجودة تحت الفئة المحددة
4. **Product Name Uniqueness**: لا يمكن أن يكون هناك منتجين بنفس الاسم لنفس المورد
5. **In Stock**: إذا كان فارغاً، الافتراضي هو "Yes"
6. **Dates**: يجب أن تكون بصيغة dd/MM/yyyy أو ISO format

#### رسائل الخطأ المحتملة:
- `CATEGORY_REQUIRED`: "Category Name is required"
- `SUBCATEGORY_REQUIRED`: "SubCategory Name is required"
- `CATEGORY_NOT_FOUND`: "Category not found: [category name]"
- `SUBCATEGORY_NOT_FOUND`: "SubCategory not found: [subcategory name]"
- `PRODUCT_NAME_EXISTS_FOR_SUPPLIER`: "A product with this name already exists for this supplier"
- `INVALID_EXCEL_FILE`: "Invalid file. Please upload an Excel file (.xlsx)"
- `UPLOAD_PRODUCTS_FAIL`: "Failed to upload products"

---

### 4.4 سير العمل الكامل (Complete Workflow)

1. **المورد يضغط على "Download Template"**
   - يتم تحميل ملف `products-template.xlsx`

2. **المورد يفتح الملف في Excel**
   - يفتح Sheet "Products"
   - يملأ البيانات:
     - **يختار الفئة من القائمة المنسدلة** في عمود "Category Name"
     - **يختار الفرعية من القائمة المنسدلة** في عمود "SubCategory Name" (ستظهر فقط الفرعيات الخاصة بالفئة المختارة)
     - يختار "Yes" أو "No" من القائمة المنسدلة في عمود "In Stock"
     - يملأ باقي الحقول (اسم المنتج، الأصل، الوحدة، الكمية، التواريخ)
   - يمكن الرجوع إلى Sheet "Categories Reference" لمعرفة الفئات والفرعيات المتاحة

3. **المورد يحفظ الملف**

4. **المورد يضغط على "Upload Products"**
   - يختار الملف المعبأ

5. **النظام يعالج الملف**:
   - يقرأ كل صف من Sheet "Products"
   - يتحقق من البيانات
   - ينشئ المنتجات الصحيحة
   - يجمع الأخطاء

6. **عرض النتائج**:
   - رسالة نجاح مع عدد المنتجات المُضافة
   - إذا كان هناك أخطاء، عرضها في جدول/Modal
   - تحديث قائمة المنتجات

---

### 4.5 أمثلة الكود الكاملة

#### React Component كامل:
```tsx
import { useState } from 'react';
import axios from 'axios';

const BulkProductUpload = () => {
  const [uploading, setUploading] = useState(false);
  const [downloading, setDownloading] = useState(false);
  const [uploadResult, setUploadResult] = useState(null);  // النتيجة الكاملة من الـ API
  const [showResultPopup, setShowResultPopup] = useState(false);  // بوب أب النتيجة

  const downloadTemplate = async () => {
    setDownloading(true);
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get('/api/v1/product/download-template', {
        responseType: 'blob',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'products-template.xlsx');
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
      
      showSuccessMessage('Template downloaded successfully');
    } catch (error) {
      showErrorMessage('Failed to download template');
    } finally {
      setDownloading(false);
    }
  };

  const handleFileUpload = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    if (!file.name.endsWith('.xlsx')) {
      showErrorMessage('Please upload an Excel file (.xlsx)');
      return;
    }

    setUploading(true);
    setUploadResult(null);
    
    try {
      const token = localStorage.getItem('token');
      const formData = new FormData();
      formData.append('file', file);

      const response = await axios.post('/api/v1/product/upload-products', formData, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'multipart/form-data'
        }
      });

      const result = response.data.content.data;
      setUploadResult(result);

      if (result.successCount > 0) {
        showSuccessMessage(
          `Successfully added ${result.successCount} product(s)!`
        );
        // refreshProducts();
      }

      // الـ API يرجع 200 حتى مع فشل بعض الصفوط — اعرض بوب أب بالنتيجة والأخطاء
      if (result.failedCount > 0 || result.totalRows > 0) {
        setShowResultPopup(true);
      }
    } catch (error) {
      showErrorMessage(
        error.response?.data?.message || 'Failed to upload products'
      );
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="bulk-upload-container">
      <h2>Bulk Upload Products</h2>
      
      <div className="upload-instructions">
        <p>1. Download the template Excel file</p>
        <p>2. Fill in your products using the Categories Reference sheet</p>
        <p>3. Upload the filled file</p>
      </div>

      <div className="upload-actions">
        <button 
          onClick={downloadTemplate}
          disabled={downloading}
          className="btn-secondary"
        >
          {downloading ? 'Downloading...' : 'Download Template'}
        </button>

        <label className="btn-primary">
          {uploading ? 'Uploading...' : 'Upload Products'}
          <input
            type="file"
            accept=".xlsx"
            onChange={handleFileUpload}
            disabled={uploading}
            style={{ display: 'none' }}
          />
        </label>
      </div>

      {/* بوب أب نتيجة الرفع — يظهر عند وجود نتيجة (نجاح أو فشل) */}
      {showResultPopup && uploadResult && (
        <UploadResultPopup
          result={uploadResult}
          onClose={() => {
            setShowResultPopup(false);
            setUploadResult(null);
          }}
        />
      )}
    </div>
  );
};

export default BulkProductUpload;
```

---

### 4.6 Checklist للتنفيذ

- [ ] إضافة زر "Download Template" في صفحة المنتجات
- [ ] تنفيذ وظيفة تحميل القالب
- [ ] إضافة زر/منطقة رفع الملف
- [ ] تنفيذ وظيفة رفع المنتجات
- [ ] إضافة معالجة multipart/form-data
- [ ] **عرض نتيجة الرفع في بوب أب (Modal)** — الـ API يرجع 200 دائماً:
  - [ ] بعد الرفع، قراءة `response.data.content.data`
  - [ ] عرض ملخص: totalRows, successCount, failedCount
  - [ ] إذا `failedCount > 0`: عرض جدول الأخطاء (rowNumber, productName, errorMessage)
  - [ ] زر إغلاق للبوب أب
- [ ] إضافة loading states للتحميل والرفع
- [ ] إضافة رسائل النجاح/الفشل
- [ ] التحقق من نوع الملف (.xlsx فقط)
- [ ] تحديث قائمة المنتجات بعد الرفع الناجح
- [ ] اختبار سير العمل (حالة كل الصفوط فشلت — يظهر بوب أب بالأخطاء)

---

## API Endpoints Summary (Updated)

### Updated Endpoints:
- `POST /api/v1/product` - Create product (now includes unit, productionDate, expirationDate)
- `PATCH /api/v1/product/{id}` - Update product (now includes unit, productionDate, expirationDate)
- `POST /api/v1/product/filter` - Filter products (response includes new fields)

### New Endpoints:
- `GET /api/v1/product/export-stock` - Export stock to Excel (Supplier only)
- `GET /api/v1/product/download-template` - Download products template Excel (Supplier only)
- `POST /api/v1/product/upload-products` - Upload products from Excel (Supplier only, multipart/form-data)

### Error Messages:
- `PRODUCT_NAME_EXISTS_FOR_SUPPLIER`: When trying to create/update product with duplicate name
- `EXPORT_STOCK_FAIL`: When export fails
- `DOWNLOAD_TEMPLATE_FAIL`: When template download fails
- `INVALID_EXCEL_FILE`: When uploaded file is not Excel
- `UPLOAD_PRODUCTS_FAIL`: When upload fails
- `CATEGORY_REQUIRED`: Category Name is required in upload
- `SUBCATEGORY_REQUIRED`: SubCategory Name is required in upload
- `UNAUTHORIZED_ACCESS`: When non-supplier tries to access supplier-only endpoints

---

## Checklist الكامل للتنفيذ

### الحقول الجديدة:
- [ ] تحديث Product interface/type مع الحقول الجديدة
- [ ] إضافة الحقول الجديدة في نموذج المنتج
- [ ] تحديث عرض المنتجات لإظهار الحقول الجديدة

### التحقق من الاسم:
- [ ] إضافة معالجة رسالة الخطأ للاسم المكرر

### تصدير المخزون:
- [ ] إضافة زر تصدير المخزون
- [ ] تنفيذ وظيفة التصدير
- [ ] إضافة loading state للتصدير

### تحميل القالب ورفع المنتجات:
- [ ] إضافة زر "Download Template"
- [ ] تنفيذ وظيفة تحميل القالب
- [ ] إضافة زر/منطقة رفع الملف
- [ ] تنفيذ وظيفة رفع المنتجات
- [ ] عرض نتائج الرفع
- [ ] إنشاء Modal/Table لعرض الأخطاء
- [ ] إضافة loading states
- [ ] تحديث قائمة المنتجات بعد الرفع

### عام:
- [ ] إضافة رسائل النجاح/الفشل
- [ ] التحقق من الصلاحيات (Supplier only)
- [ ] اختبار جميع الوظائف

---

**تاريخ التحديث**: 2026-02-02
**الإصدار**: 2.0
