# AFC-MAD Technical Design and Logic Specification

## 1. Scope

This document is a deeper technical reference for the current AFC-MAD Android project.  
It complements `PROJECT_EXPLANATION.md` and focuses on implementation-level design and logic behavior.

---

## 2. System Context

### 2.1 Runtime Context

- Client: Android mobile app (`:app` module)
- Local persistence: app internal files (`context.filesDir`)
- Session storage: `SharedPreferences`
- Remote backend: none in current implementation

### 2.2 Architectural Style

Current implementation is **Activity + Adapter + Utility layering**:

- Presentation/UI: Activities + XML + RecyclerView adapters
- Business logic: mostly in Activity event handlers
- Data access: `FileHandler` and `CartManager`

This is pragmatic for academic/prototype scale, with low abstraction overhead.

---

## 3. Layered Design (UI / BL / DL)

## 3.1 UI Layer

Main responsibilities:

- Render screen state from model data
- Capture user intent (click/tap/input)
- Trigger navigation and use-case actions

Primary UI controllers:

- `SplashActivity` - intro animation and routing
- `LoginActivity` / `SignupActivity` - auth forms
- `HomeActivity` - menu discovery/filtering
- `ProductDetailActivity` - quantity + add-to-cart action
- `CartActivity` - cart management
- `CheckoutActivity` - order finalization
- `AdminHomeActivity` - admin entry dashboard
- `ManageMenuActivity` - menu CRUD (add/delete)
- `ViewOrdersActivity` - order listing

Rendering patterns:

- One layout per activity for screen-level composition
- One adapter per list type (`MenuAdapter`, `CartAdapter`, `OrderAdapter`)

## 3.2 BL (Business Logic) Layer

Implemented mainly inside activities and utility objects:

- Input validation (phone/pin/address/required fields)
- Role routing (`admin` hardcoded path vs user path)
- Product filtering (`orderType` + `category`)
- Quantity update and cart totals
- Checkout order construction
- Admin menu item creation/deletion flow

## 3.3 DL (Data Layer)

### Persistent data utility

`FileHandler` provides CRUD-like operations for:

- users
- menu items
- categories
- orders

### Volatile state utility

`CartManager` singleton stores cart in memory for active process.

### Session key-value storage

`SharedPreferences("UserPrefs")` stores current user's phone and address.

---

## 4. Data Contract and Schema

## 4.1 Domain Model Contracts

### User

- `phone: String`
- `address: String`
- `pin: String`
- `isAdmin: Boolean = false`

### MenuItem

- `id: String`
- `name: String`
- `price: Double`
- `description: String`
- `category: String`
- `imagePath: String?`
- `orderType: String` (`Delivery`, `Pickup`, `Merch`)

### Category

- `id: String`
- `name: String`
- `orderType: String`

### CartItem

- `menuItem: MenuItem`
- `quantity: Int`
- derived: `totalLinePrice = menuItem.price * quantity`

### Order

- `orderId: String`
- `userPhone: String`
- `userAddress: String`
- `items: List<CartItem>`
- `totalPrice: Double`
- `paymentMethod: String`
- `status: String = "Pending"`

## 4.2 Physical Storage Format

All persisted entities are line-oriented text files:

- `users.txt` -> `phone|address|pin|isAdmin`
- `menu.txt` -> `id|name|price|description|category|imagePathOrNone|orderType`
- `categories.txt` -> `id|name|orderType`
- `orders.txt` -> `orderId|userPhone|userAddress|itemPairs|totalPrice|paymentMethod|status`

`itemPairs` in orders:

- `menuId:quantity;menuId:quantity;...`

Parsing behavior:

- malformed entries are skipped in try/catch blocks
- delete operations are implemented as read/filter/rewrite

---

## 5. Navigation and Control Flow

## 5.1 Launch Flow

1. Launcher intent starts `SplashActivity`.
2. `SplashActivity` waits for animation duration.
3. Routes to `LoginActivity`.

## 5.2 Authentication and Role Flow

`LoginActivity`:

1. Validate non-empty phone and pin.
2. If `admin / 1234`, navigate to `AdminHomeActivity`.
3. Else query `FileHandler.getUsers()` and match user.
4. On success, persist lightweight session data (phone/address), then `HomeActivity`.
5. On failure, set input errors and show toast.

`SignupActivity`:

1. Validate required fields and PIN minimum length.
2. Create `User`.
3. Persist via `FileHandler.saveUser()`.
4. Return to login.

## 5.3 Customer Ordering Flow

`HomeActivity`:

1. Select order type (Delivery/Pickup/Merch).
2. Load matching categories from local storage.
3. Filter menu by selected type + category.
4. Tap item -> `ProductDetailActivity`.

`ProductDetailActivity`:

1. Display item detail and image.
2. Quantity +/- updates local quantity state.
3. Add to cart loops quantity times into `CartManager`.

`CartActivity`:

1. Bind cart list from `CartManager`.
2. Quantity controls update item quantity.
3. Display computed total.
4. Checkout navigates to `CheckoutActivity`.

`CheckoutActivity`:

1. Read user info from `SharedPreferences`.
2. Build `Order` from cart and payment method.
3. Save order via `FileHandler`.
4. Clear cart via `CartManager.clearCart()`.
5. Return to home.

## 5.4 Admin Flow

`AdminHomeActivity`:

- route to menu management or orders view

`ManageMenuActivity`:

1. Pick image from system document picker.
2. Copy image into internal app storage.
3. Fill item form and select order type.
4. Persist new menu item.
5. List existing items and allow delete.

`ViewOrdersActivity`:

- fetch and render all orders in RecyclerView.

---

## 6. UI/UX Design Breakdown

## 6.1 Design Intent

Visual style follows a branded food-ordering pattern:

- dark surfaces
- red accent calls-to-action
- card-based list presentation
- icon + image-heavy interactions

## 6.2 Core UI Patterns

- **Auth forms**: text inputs with inline validation errors
- **Filter-first browsing**: order type toggles + category chips
- **Master-detail**: list -> detail -> action
- **Cart review**: editable quantities and aggregate total
- **Admin task screens**: direct operational controls for menu/orders

## 6.3 Component Strategy

- RecyclerView for scalable list rendering
- Material buttons/chips/text fields for consistency
- custom shape drawables for look and feel
- ViewBinding for strongly typed view access

## 6.4 Current UX Constraints

- no loading skeletons beyond auth button progress
- no empty-state messaging in some lists
- no robust error surfaces for storage failures
- no explicit state restoration strategy for process death

---

## 7. Logic Rules and Edge-Case Behavior

## 7.1 Validation Rules

- Login: phone required, pin required
- Signup: phone required, address required, pin min length 4
- Manage menu: name required, price required, category required

## 7.2 Role Rules

- Admin path is static and hardcoded (`admin/1234`)
- Normal users are those persisted in `users.txt`

## 7.3 Filtering Rules

Displayed menu item must satisfy:

- `item.orderType == currentOrderType`
- and (`currentCategory == "All"` or `item.category == currentCategory`)

## 7.4 Cart Rules

- Add item increments existing quantity if already in cart
- Minus decreases quantity until 1, then stops removing in `CartAdapter` UI behavior
- Total is sum of `CartItem.totalLinePrice`

## 7.5 Order Rules

- Checkout always creates status `Pending`
- Payment method selected from radio controls (`Cash` else `Card`)
- Order stores item IDs + quantities, then reconstructs display items from current menu table when reading

### Important side effect

If a menu item is deleted after an order is placed, item reconstruction may lose that line item display because lookup is based on current menu data.

---

## 8. Security and Data Integrity Notes

Current state (prototype level):

- PIN is plain text in `users.txt`
- no encryption-at-rest
- no secure auth token/session design
- no tamper protection for local data files
- no transaction isolation or locking around file writes

Recommended hardening:

1. hash PIN (and never store plain value)
2. migrate to Room DB with schema constraints
3. add structured error channels and retry-safe writes
4. replace hardcoded admin path with proper role model

---

## 9. Performance and Scalability Notes

Current profile:

- file read/parse occurs on demand in UI paths
- no pagination/diffing in adapters (`notifyDataSetChanged`)
- suitable for small data volume

Potential scaling issues:

- large `menu.txt`/`orders.txt` files increase parse cost
- repeated full reads for filtering and listing

Optimization path:

- introduce repository cache
- use `ListAdapter` + `DiffUtil`
- move I/O off main thread with coroutines
- migrate to Room for indexed query patterns

---

## 10. Testability and Quality Gaps

Current code has limited separation for unit testing:

- BL embedded in activities
- direct file I/O calls inside UI controllers

Suggested testing strategy:

- unit tests for validation and filtering logic
- integration tests for file serialization/deserialization
- UI tests for auth, ordering, and admin flows

---

## 11. Known Design Deviations

- `MainActivity` includes seed logic but launcher is `SplashActivity`; seed path may be unused unless `MainActivity` is invoked explicitly.
- Category model/storage exists, but admin category management UI is not fully represented in current screens.
- Cart persistence is in-memory only; app restart resets cart.

---

## 12. Future Target Architecture (Suggested)

Recommended transition:

1. **Presentation**: MVVM (`Activity/Fragment` + `ViewModel`)
2. **Domain**: explicit use cases (`LoginUseCase`, `PlaceOrderUseCase`, etc.)
3. **Data**: repository interfaces + Room implementation (+ optional remote API)
4. **Cross-cutting**: central error model, logging, analytics hooks

This evolution keeps current UX while improving reliability, testability, and maintainability.
