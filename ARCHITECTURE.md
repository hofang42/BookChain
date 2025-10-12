# BookChain FE - Project Architecture Documentation

## Tổng quan kiến trúc

Dự án sử dụng **Single Activity Architecture** kết hợp với **Navigation Component** - kiến trúc được Google khuyến nghị cho các ứng dụng Android hiện đại.

## Kiến trúc chính

### Single Activity + Multiple Fragments
```
MainActivity (Duy nhất Activity)
├── Bottom Navigation
├── NavHostFragment (Container cho tất cả fragments)
│   ├── HomeFragment
│   ├── CategoriesFragment
│   ├── CartFragment
│   ├── AccountFragment
│   ├── AddressListFragment
│   ├── CheckoutFragment
│   └── PaymentSuccessFragment
└── Navigation Graph (nav_graph.xml)
```

## Cấu trúc thư mục

```
app/src/main/
├── java/com/hofang/bookchainfe/
│   ├── MainActivity.java                    # Activity duy nhất
│   ├── ui/
│   │   ├── home/
│   │   │   └── HomeFragment.java           # Màn Home
│   │   ├── categories/
│   │   │   └── CategoriesFragment.java     # Màn Categories
│   │   ├── cart/
│   │   │   └── CartFragment.java           # Màn Cart
│   │   ├── account/
│   │   │   └── AccountFragment.java        # Màn Account
│   │   ├── address/
│   │   │   ├── AddressListFragment.java    # Màn danh sách địa chỉ
│   │   │   └── AddAddressBottomSheet.java  # Bottom sheet thêm địa chỉ
│   │   ├── checkout/
│   │   │   └── CheckoutFragment.java       # Màn Checkout
│   │   └── payment/
│   │       └── PaymentSuccessFragment.java # Màn thanh toán thành công
│
├── res/
│   ├── layout/
│   │   ├── activity_main.xml               # Layout MainActivity
│   │   ├── fragment_home.xml
│   │   ├── fragment_categories.xml
│   │   ├── fragment_cart.xml
│   │   ├── fragment_account.xml
│   │   ├── fragment_address_list.xml
│   │   ├── fragment_checkout.xml
│   │   ├── fragment_payment_success.xml
│   │   └── fragment_add_address_bottom_sheet.xml
│   ├── navigation/
│   │   └── nav_graph.xml                   # Navigation graph
│   └── menu/
│       └── bottom_nav_menu.xml             # Bottom navigation menu
```

## Navigation Flow

### Navigation Graph (nav_graph.xml)
Navigation graph định nghĩa tất cả các màn hình và cách chúng liên kết với nhau:

```xml
Home (startDestination)
├── → Payment Success (test)

Cart
└── → Checkout
    ├── → Address List (via bottom sheet)
    └── → Payment Success

Categories (placeholder)

Account (placeholder)

Address List (standalone)

Payment Success (fullscreen, no bottom nav)

Checkout (fullscreen with bottom nav)
```

### Bottom Navigation
Bottom navigation có 4 tab chính:
- **Home** (`nav_home`)
- **Categories** (`nav_categories`)
- **Cart** (`nav_cart`)
- **Account** (`nav_account`)

## Chi tiết từng màn hình

### 1. MainActivity
**File:** `MainActivity.java`
**Chức năng:**
- Activity duy nhất trong app
- Chứa NavHostFragment để host tất cả fragments
- Quản lý Bottom Navigation
- Setup Navigation Component tự động

**Code quan trọng:**
```java
NavHostFragment navHostFragment = getSupportFragmentManager()
    .findFragmentById(R.id.nav_host_fragment);
NavController navController = navHostFragment.getNavController();
NavigationUI.setupWithNavController(bottomNav, navController);
```

### 2. HomeFragment
**File:** `ui/home/HomeFragment.java`
**Chức năng:**
- Màn hình chính khi mở app
- Hiển thị danh sách sản phẩm (TODO)
- Có Bottom Navigation

### 3. CartFragment
**File:** `ui/cart/CartFragment.java`
**Chức năng:**
- Hiển thị giỏ hàng
- Nút "Go to Checkout" để chuyển sang màn Checkout
- Có Bottom Navigation

**Navigation:**
```java
navController.navigate(R.id.action_cart_to_checkout);
```

### 4. CheckoutFragment
**File:** `ui/checkout/CheckoutFragment.java`
**Chức năng:**
- Màn hình thanh toán
- Hiển thị địa chỉ giao hàng
- Chọn phương thức thanh toán (Credit Card / Cash on Delivery)
- Nút "Change" và "Add New Address" → hiện AddAddressBottomSheet
- Nút "Pay $60.00" → chuyển sang PaymentSuccessFragment
- **CÓ Bottom Navigation**

**Bottom Sheet Integration:**
```java
btnChangeAddress.setOnClickListener(v -> {
    AddAddressBottomSheet bottomSheet = new AddAddressBottomSheet();
    bottomSheet.show(getParentFragmentManager(), "add_address_sheet");
});
```

### 5. AddressListFragment
**File:** `ui/address/AddressListFragment.java`
**Chức năng:**
- Màn hình danh sách địa chỉ
- Nút "Add New Address" → hiện AddAddressBottomSheet
- **KHÔNG có Bottom Navigation** (ẩn khi vào màn hình này)

**Hide/Show Bottom Nav:**
```java
@Override
public View onCreateView(...) {
    // Hide bottom nav
    if (getActivity() != null) {
        View bottomNav = getActivity().findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }
    }
    return inflater.inflate(R.layout.fragment_address_list, container, false);
}

@Override
public void onDestroyView() {
    super.onDestroyView();
    // Show bottom nav when leaving
    if (getActivity() != null) {
        View bottomNav = getActivity().findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.VISIBLE);
        }
    }
}
```

### 6. PaymentSuccessFragment
**File:** `ui/payment/PaymentSuccessFragment.java`
**Chức năng:**
- Màn hình xác nhận thanh toán thành công
- Nút close (X) ở góc trên trái để quay lại
- Hiển thị icon success và message
- **KHÔNG có Bottom Navigation** (ẩn toàn bộ)

### 7. AddAddressBottomSheet
**File:** `ui/address/AddAddressBottomSheet.java`
**Chức năng:**
- Bottom sheet dialog để thêm địa chỉ mới
- Có các trường: Street, City, Postal Code
- Checkbox "Save address"
- Nút "Add" và "Cancel"

## Ưu điểm của kiến trúc này

### 1. Single Activity Architecture
✅ **Dễ quản lý navigation:** Tất cả navigation logic ở một nơi (nav_graph.xml)
✅ **Shared state dễ dàng:** Fragments có thể chia sẻ data qua ViewModel
✅ **Transitions mượt mà:** Animation giữa các màn hình mượt hơn
✅ **Bottom nav tự động:** NavigationUI tự động highlight tab đúng

### 2. Navigation Component
✅ **Type-safe arguments:** Pass data an toàn giữa các màn hình
✅ **Back stack tự động:** Không cần quản lý back stack thủ công
✅ **Deep linking:** Dễ dàng implement deep links
✅ **Visual editor:** Có thể edit navigation graph bằng visual editor trong Android Studio

### 3. Fragment-based
✅ **Reusable:** Fragments có thể tái sử dụng
✅ **Lifecycle chuẩn:** Lifecycle của Fragment rõ ràng và dễ quản lý
✅ **Modular:** Dễ dàng thêm/xóa màn hình

## Quy tắc khi phát triển

### 1. Thêm màn hình mới
**Bước 1:** Tạo Fragment
```java
public class NewFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new, container, false);
    }
}
```

**Bước 2:** Tạo layout XML
```xml
<!-- fragment_new.xml -->
<androidx.constraintlayout.widget.ConstraintLayout>
    <!-- Your UI here -->
</androidx.constraintlayout.widget.ConstraintLayout>
```

**Bước 3:** Thêm vào nav_graph.xml
```xml
<fragment
    android:id="@+id/newFragment"
    android:name="com.hofang.bookchainfe.ui.new.NewFragment"
    android:label="New Screen"
    tools:layout="@layout/fragment_new" />
```

**Bước 4:** Tạo action để navigate
```xml
<fragment android:id="@+id/sourceFragment">
    <action
        android:id="@+id/action_source_to_new"
        app:destination="@id/newFragment" />
</fragment>
```

**Bước 5:** Navigate trong code
```java
navController.navigate(R.id.action_source_to_new);
```

### 2. Ẩn/hiện Bottom Navigation
Nếu màn hình cần ẩn bottom nav (như fullscreen):

```java
@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container,
                         Bundle savedInstanceState) {
    if (getActivity() != null) {
        View bottomNav = getActivity().findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }
    }
    return inflater.inflate(R.layout.fragment_your_screen, container, false);
}

@Override
public void onDestroyView() {
    super.onDestroyView();
    if (getActivity() != null) {
        View bottomNav = getActivity().findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.VISIBLE);
        }
    }
}
```

### 3. Sử dụng Bottom Sheet
Thay vì navigate sang màn hình mới, dùng Bottom Sheet cho các form nhỏ:

```java
YourBottomSheet bottomSheet = new YourBottomSheet();
bottomSheet.show(getParentFragmentManager(), "your_sheet_tag");
```

### 4. Pass data giữa các màn hình
Sử dụng Safe Args hoặc Bundle:

```java
// Option 1: Bundle
Bundle bundle = new Bundle();
bundle.putString("key", "value");
navController.navigate(R.id.action_to_dest, bundle);

// Nhận data
String value = getArguments().getString("key");

// Option 2: Safe Args (recommended - cần setup gradle plugin)
NewFragmentDirections.ActionToNew action =
    NewFragmentDirections.actionToNew("value");
navController.navigate(action);
```

## Debugging Tips

### 1. Check current destination
```java
NavController navController = Navigation.findNavController(view);
int currentDestId = navController.getCurrentDestination().getId();
```

### 2. Pop back stack
```java
navController.popBackStack(); // Go back
navController.popBackStack(R.id.homeFragment, false); // Pop to specific destination
```

### 3. Navigate with pop
```java
NavOptions navOptions = new NavOptions.Builder()
    .setPopUpTo(R.id.homeFragment, true)
    .build();
navController.navigate(R.id.destination, null, navOptions);
```

## Dependencies

```kotlin
// Navigation Component
implementation("androidx.navigation:navigation-fragment:2.8.0")
implementation("androidx.navigation:navigation-ui:2.8.0")

// Material Design (cho Bottom Navigation)
implementation("com.google.android.material:material:1.x.x")
```

## Tài liệu tham khảo
- [Navigation Component Guide](https://developer.android.com/guide/navigation)
- [Single Activity Architecture](https://developer.android.com/guide/navigation/navigation-principles)
- [Fragment Best Practices](https://developer.android.com/guide/fragments/best-practices)

---

**Last Updated:** 2025-01-12
**Version:** 1.0
**Author:** Development Team
