# Frontend Update Prompt for AI Agent

## Overview
The backend has been updated with new features for subscription plans, payment information, and product search management. The frontend needs to be updated to support these new features.

## New Features Added

### 1. Payment Information Management (Admin Only)
**Endpoints:**
- `POST /api/v1/admin/payment-info` - Create payment info
- `GET /api/v1/admin/payment-info` - Get all payment info
- `GET /api/v1/admin/payment-info/{id}` - Get payment info by ID
- `PUT /api/v1/admin/payment-info/{id}` - Update payment info
- `DELETE /api/v1/admin/payment-info/{id}` - Delete payment info
- `GET /api/v1/admin/payment-info/type/{paymentType}` - Get by type (BANK_ACCOUNT/ELECTRONIC_WALLET)
- `GET /api/v1/admin/payment-info/active` - Get active payment info

**Payment Info Model:**
```typescript
interface PaymentInfo {
  id: string;
  transferNumber: string;
  accountNumber?: string;
  paymentType: 'BANK_ACCOUNT' | 'ELECTRONIC_WALLET';
  accountHolderName?: string;
  bankName?: string; // For bank accounts
  walletProvider?: string; // For electronic wallets
  active: boolean;
  createdAt: string;
  updatedAt: string;
}
```

**Required UI:**
- Admin page to manage payment information (CRUD operations)
- Form to create/edit payment info with:
  - Transfer number (required)
  - Account number (optional)
  - Payment type dropdown (BANK_ACCOUNT/ELECTRONIC_WALLET)
  - Account holder name (optional)
  - Bank name (shown when type is BANK_ACCOUNT)
  - Wallet provider (shown when type is ELECTRONIC_WALLET)
  - Active toggle

### 2. Enhanced Subscription Plans with Features and Searches

**Updated Plan Model:**
```typescript
interface SubscriptionPlan {
  id: string;
  name: string;
  pricePerUser: number;
  description?: string;
  billingFrequency: 'MONTHLY' | 'QUARTERLY' | 'YEARLY';
  planType: 'SUPPLIER' | 'CUSTOMER' | 'BOTH';
  
  // NEW: Features with prices
  features?: PlanFeature[];
  
  // NEW: For Customer plans - Product searches configuration
  productSearchesConfig?: {
    from?: number;
    to?: number;
    unlimited?: boolean;
    pricePerSearch?: number;
  };
  
  // NEW: For Supplier plans - Base subscription price
  baseSubscriptionPrice?: number;
  
  specialOffers?: SpecialOffer[];
  exclusive: boolean;
  active: boolean;
}

interface PlanFeature {
  feature: PlanFeaturesEnum;
  price: number;
}

enum PlanFeatures { // translate to arabic and english please
  // Supplier Features
  SUPPLIER_ADVERTISEMENTS = "Advertisements",
  SUPPLIER_PRIVATE_ORDERS = "Private Orders",
  SUPPLIER_SPECIAL_OFFERS = "Special Offers",
  SUPPLIER_ADVANCED_REPORTS = "Advanced Reports",
  
  // Customer Features
  CUSTOMER_PRIVATE_ORDERS = "Private Orders",
  CUSTOMER_RAW_MATERIALS_ADVANCE = "Raw Materials Advance",
  CUSTOMER_VIEW_SUPPLIER_OFFERS = "View Supplier Special Offers",
  CUSTOMER_ADVANCED_REPORTS = "Advanced Reports"
}
```

**Updated Calculate Price Request:**
```typescript
interface CalculatePriceRequest {
  planId: string;
  numberOfUsers: number;
  numberOfSearches?: number; // NEW: For customer plans
  selectedFeatures?: PlanFeatures[]; // NEW: Selected features
}
```

**Updated Calculate Price Response:**
```typescript
interface CalculatePriceResponse {
  planId: string;
  planName: string;
  pricePerUser: number;
  numberOfUsers: number;
  basePrice?: number; // NEW
  numberOfSearches?: number; // NEW
  searchesPrice?: number; // NEW
  featuresPrice?: number; // NEW
  total: number;
  discount: number;
  finalPrice: number;
  appliedOffer?: SpecialOffer;
  availableOffers: SpecialOffer[];
}
```

**Updated User Subscription Request:**
```typescript
interface UserSubscriptionRequest {
  planId: string;
  numberOfUsers: number;
  subscriptionFile: string;
  numberOfSearches?: number; // NEW: For customer plans
  selectedFeatures?: PlanFeatures[]; // NEW: Selected features
}
```

**Required UI Updates:**

1. **Admin - Create/Edit Plan Page:**
   - Add section for "Features" where admin can:
     - Select features from available list (filtered by plan type)
     - Set price for each selected feature
     - Display features with Arabic and English descriptions
   - For Customer plans:
     - Add "Product Searches Configuration" section:
       - Option: Range (from - to) with price per search
       - Option: Unlimited flag
       - Price per search input
   - For Supplier plans:
     - Add "Base Subscription Price" input field

2. **Customer - Plan Selection & Subscription:**
   - When selecting a customer plan:
     - Show available features with prices
     - Allow customer to select features (checkboxes)
     - Show total features price
   - Add "Number of Searches" input (if plan has productSearchesConfig)
     - Show price per search
     - Show total searches price
   - Update price calculation to show:
     - Base price
     - Searches price (if applicable)
     - Features price
     - Total before discount
     - Discount
     - Final price

3. **Subscription Details Page:**
   - Display:
     - Number of searches purchased
     - Remaining searches
     - Points earned
     - Selected features list

### 3. Product Search with Searches & Points System

**New Behavior:**
- When a customer (CUSTOMER_OWNER or CUSTOMER_STAFF) searches for products:
  - System deducts 1 search from remaining searches
  - System adds 1 point
  - If no searches left but has points, uses 1 point
  - If no searches and no points, shows error: "No searches or points available"

**Error Message:**
- `NO_SEARCHES_OR_POINTS_AVAILABLE`: "No searches or points available. Please purchase more searches or wait for your subscription to be renewed."

**Required UI Updates:**

1. **Product Search Page:**
   - Show remaining searches count
   - Show available points
   - Show warning/error when searches/points are low
   - Display message when search is blocked due to no searches/points

2. **Dashboard/Profile:**
   - Display subscription info including:
     - Remaining searches
     - Points balance
     - Progress indicator for searches usage

3. **Search Results:**
   - Show notification when search is successful
   - Update remaining searches/points in real-time

## Implementation Checklist

### Admin Pages
- [ ] Payment Information Management Page (CRUD)
- [ ] Update Plan Creation/Edit form with:
  - [ ] Features selection with prices
  - [ ] Product searches config (for customer plans)
  - [ ] Base subscription price (for supplier plans)

### Customer Pages
- [ ] Update Plan Selection page with:
  - [ ] Features selection
  - [ ] Number of searches input
  - [ ] Updated price calculation display
- [ ] Update Subscription Details page with searches/points info
- [ ] Update Product Search page with:
  - [ ] Remaining searches display
  - [ ] Points display
  - [ ] Error handling for no searches/points

### General
- [ ] Update API service methods for new endpoints
- [ ] Update TypeScript interfaces/types
- [ ] Add error handling for new error messages
- [ ] Update subscription flow to include new fields
- [ ] Add real-time updates for searches/points after product search

## API Endpoints Summary

### Payment Info (Admin)
- `POST /api/v1/admin/payment-info`
- `GET /api/v1/admin/payment-info`
- `GET /api/v1/admin/payment-info/{id}`
- `PUT /api/v1/admin/payment-info/{id}`
- `DELETE /api/v1/admin/payment-info/{id}`
- `GET /api/v1/admin/payment-info/type/{paymentType}`
- `GET /api/v1/admin/payment-info/active`

### Subscription (Updated)
- `POST /api/v1/user-subscriptions/calculate-price` (updated request/response)
- `POST /api/v1/user-subscriptions/submit` (updated request)

### Product Search (Updated Behavior)
- `POST /api/v1/product/filter` (now deducts searches for customers)

## Notes
- All endpoints require authentication
- Payment info endpoints require admin role
- Product search now has different behavior for customers vs suppliers
- Points are earned 1:1 with searches (1 search = 1 point)
- Points can be used when searches are exhausted (1 point = 1 search)
