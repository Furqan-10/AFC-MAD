# AFC-MAD Project Explanation

## 1) Project Overview

`AFC-MAD` is a single-module Android app (Kotlin + XML) that simulates a food ordering system with two roles:

- **Customer side**: browse menu, view details, add to cart, checkout.
- **Admin side**: manage menu items, view placed orders.

The app is primarily **offline/local**. It does not call a remote backend API. Persistent data is stored in local internal files through a utility class.

---

## 2) Technical Stack

- **Platform**: Android (minSdk 24, targetSdk 35, compileSdk 35)
- **Language**: Kotlin
- **UI**: XML layouts + ViewBinding
- **List UI**: RecyclerView + custom adapters
- **Dependencies**: AndroidX core/appcompat/activity/constraintlayout, Material Components, GridLayout
- **Build tooling**: Gradle Kotlin DSL + version catalog (`libs.versions.toml`)

---

## 3) Module and Package Structure

Single app module: `:app`

Main package: `com.example.afc_mad`

- **Activities (UI controllers)**:
  - `SplashActivity`
  - `LoginActivity`
  - `SignupActivity`
  - `HomeActivity`
  - `ProductDetailActivity`
  - `CartActivity`
  - `CheckoutActivity`
  - `AdminHomeActivity`
  - `ManageMenuActivity`
  - `ViewOrdersActivity`
  - `MainActivity` (seed/redirect logic present but not declared in manifest)
- **Adapters**:
  - `MenuAdapter`
  - `CartAdapter`
  - `OrderAdapter`
- **Models**:
  - `MenuItem`
  - `Category`
  - `User`
  - `CartItem`
  - `Order`
- **Utilities**:
  - `FileHandler` (persistent storage I/O)
  - `CartManager` (in-memory cart singleton)
- **Resources**:
  - layouts in `res/layout`
  - colors/theme/strings in `res/values`
  - shape/gradient assets in `res/drawable`

---

## 4) Architecture (Layered View)

The app follows a practical, activity-driven layered style rather than strict Clean Architecture.

### UI Layer (Presentation)

- XML screens render UI.
- Activities bind UI, react to clicks, and control screen navigation.
- RecyclerView adapters map model objects to card/list rows.

### BL Layer (Business Logic)

Business rules currently live mostly inside Activities:

- auth/registration validation
- role routing (admin vs customer)
- menu filtering by order type/category
- cart quantity and totals
- checkout order creation and payment selection

There is no separate use-case/interactor layer yet.

### DL Layer (Data Layer)

- `FileHandler` reads/writes app data to internal files (`context.filesDir`).
- Storage format is line-based text with custom delimiters (`|`, `;`, `:`).
- `CartManager` stores cart in-memory for current process session.
- SharedPreferences stores small session-ish user data (`user_phone`, `user_address`).

---

## 5) Activity Flow and Navigation

### App Start

1. `SplashActivity` is launcher in `AndroidManifest.xml`.
2. After animation delay, splash navigates to `LoginActivity`.

### Authentication Flow

- `LoginActivity`
  - If credentials are `admin / 1234` -> `AdminHomeActivity`
  - Else checks `users.txt` through `FileHandler.getUsers()`
  - On user login, saves phone/address in `SharedPreferences`, then opens `HomeActivity`
- `SignupActivity`
  - Validates fields
  - Creates `User` and appends to `users.txt`
  - Returns to login screen

### Customer Flow

1. `HomeActivity` loads menu categories and items.
2. User switches order type tabs (`Delivery`, `Pickup`, `Merch`) and category chips.
3. Selecting item opens `ProductDetailActivity`.
4. User selects quantity and adds item(s) to `CartManager`.
5. `CartActivity` shows cart contents and total.
6. `CheckoutActivity` creates `Order`, saves it, clears cart, returns to `HomeActivity`.

### Admin Flow

1. `AdminHomeActivity` offers:
   - `ManageMenuActivity`
   - `ViewOrdersActivity`
2. `ManageMenuActivity` adds/removes menu entries (with optional image copy to internal storage).
3. `ViewOrdersActivity` reads and displays all orders.

---

## 6) Data Schema (Logical Models)

### `User`

- `phone: String`
- `address: String`
- `pin: String`
- `isAdmin: Boolean = false`

### `MenuItem`

- `id: String`
- `name: String`
- `price: Double`
- `description: String`
- `category: String`
- `imagePath: String?`
- `orderType: String` (`Delivery` / `Pickup` / `Merch`)

### `Category`

- `id: String`
- `name: String`
- `orderType: String`

### `CartItem`

- `menuItem: MenuItem`
- `quantity: Int`
- computed `totalLinePrice`

### `Order`

- `orderId: String`
- `userPhone: String`
- `userAddress: String`
- `items: List<CartItem>`
- `totalPrice: Double`
- `paymentMethod: String` (`Cash` or `Card`)
- `status: String = "Pending"`

---

## 7) Physical Storage Schema (Files)

Managed by `FileHandler` in internal app storage:

- `users.txt`
  - line format: `phone|address|pin|isAdmin`
- `menu.txt`
  - line format: `id|name|price|description|category|imagePathOrNone|orderType`
- `categories.txt`
  - line format: `id|name|orderType`
- `orders.txt`
  - line format: `orderId|userPhone|userAddress|itemPairs|totalPrice|paymentMethod|status`
  - `itemPairs` format: `menuId:quantity;menuId:quantity;...`

Notes:

- parsing is tolerant to malformed lines (skip on exception)
- deletes are implemented as read-filter-rewrite operations

---

## 8) UI Design System and Components

- **Theme**: Material3-based app theme
- **Primary style direction**: dark background + red accent
- **Components in use**:
  - RecyclerView for menu/cart/order lists
  - Material buttons/chips/text fields
  - custom rounded/gradient drawables for branded styling
- **Layouts**: one XML layout per screen plus reusable row/card layouts (`item_menu`, `item_cart`, `item_order`)

---

## 9) Frontend to Backend Explanation

In this project:

- **Frontend** = Android app screens, adapters, and interaction logic.
- **Backend** = local on-device storage logic (`FileHandler`) rather than a remote server.

So communication is direct function calls:

1. UI events in activities trigger logic.
2. Logic calls `FileHandler` to read/write text files.
3. Returned data models are rendered by adapters/views.

There is **no network transport**, no REST API, and no server database in the current implementation.

---

## 10) Strengths and Current Gaps

### Strengths

- Clear end-to-end user and admin flows.
- Simple persistence layer that is easy to understand.
- Reusable adapters and model classes.
- Dynamic filtering by order type and category.

### Gaps / Risks

- PINs are stored in plain text.
- Hardcoded admin credentials in `LoginActivity`.
- Flat-file format is fragile for special characters/delimiter collisions.
- Cart is in-memory only (not process-persistent).
- No MVVM/Repository abstraction boundaries; much logic is inside activities.
- `MainActivity` seed logic is disconnected from manifest launcher flow.

---

## 11) Suggested Evolution Path

If this app is expanded toward production:

1. Move from flat files to Room/SQLite.
2. Replace hardcoded auth with secure authentication flow.
3. Hash/encrypt sensitive user fields.
4. Introduce MVVM with Repository + ViewModel layers.
5. Add remote backend integration (API + sync) if multi-device/admin panel is needed.
6. Add unit/UI tests for validation, filtering, cart, and order persistence.

---

## 12) Quick End-to-End Sequence

1. App launches -> splash animation.
2. User logs in or signs up.
3. Customer browses menu -> detail -> cart -> checkout.
4. Checkout saves order to local file storage.
5. Admin logs in and views orders or modifies menu.
6. All persisted entities are read back from local files on next app use.
