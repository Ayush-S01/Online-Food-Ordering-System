# Online Food Ordering System — Java Mini Project

> A complete, fully functional multi-cuisine desktop application built with **Core Java (JDK 17+) · JavaFX 21 · MySQL · JDBC · Maven**.  
> Features 45+ dishes across 7 major world cuisines, instant multi-channel payments (UPI, Card, Net Banking, COD), real-time search & dietary filters, and transactional order persistence.

---

## What's New in this Multi-Cuisine Release

### 1. 7 World Cuisines (45+ Dishes)
- **North Indian** 🍛: Dal Makhani, Butter Chicken, Paneer Tikka, Paneer Butter Masala, Garlic Naan, Chole Bhature...
- **South Indian** 🥞: Masala Dosa, Mysore Masala Dosa, Idli Vada, Chicken Chettinad, Chicken 65, Filter Coffee...
- **Chinese** 🥢: Veg Hakka Noodles, Schezwan Chicken Fried Rice, Chilli Paneer, Veg Manchurian, Momos...
- **Italian** 🍕: Margherita Pizza (10"), Farmhouse Veg Pizza, Chicken Pepperoni, Penne Alfredo, Cheesy Garlic Bread, Tiramisu...
- **Mexican** 🌮: Loaded Cheesy Nachos, Paneer Fiesta Burrito, Crispy Chicken Tacos, Grilled Quesadilla, Churros...
- **Fast Food** 🍔: Crispy Veggie Delight Burger, Double Cheese Smash Burger, Peri-Peri Fries, Belgian Chocolate Shake...
- **Biryani & Mughlai** 🍚: Authentic Hyderabadi Dum Chicken Biryani, Lucknowi Veg Dum Biryani, Mutton Galouti Kabab, Shahi Tukda...

### 2. Multi-Channel Simulated Payment Gateway
- **Instant UPI**: Enter any UPI ID (e.g. `yourname@oksbi`, `phone@paytm`) for simulated instant bank authorization.
- **Credit / Debit Cards**: Card number, expiry date, CVV, and cardholder name validation with simulated OTP / payment gateway approval.
- **Net Banking**: Choose from major banks (HDFC, SBI, ICICI, Axis, Kotak).
- **Cash on Delivery (COD)**: Doorstep cash collection option.
- **Automated Transaction Receipts**: Generates unique transaction references (`UPI-XXXX`, `CARD-XXXX`, `NB-XXXX`, `COD-VERIFIED`) with 5% GST tax calculation and free delivery promotion.

### 3. Smart Dietary & Category Filters
- **Interactive Cuisine Chips**: One-click horizontal filter bar to toggle between cuisines.
- **🟢 Pure Veg Filter**: Instant toggle to show only vegetarian dishes.
- **Sorting Options**: By **Rating (⭐ High to Low)**, Price (Low to High), Price (High to Low), and Name (A-Z).
- **Real-time Search**: Search simultaneously across dish name, description, and cuisine.

---

## Demo Credentials & Sample Coupons

### Login Credentials
| Role | Email | Password |
|---|---|---|
| **Administrator** | `admin@food.com` | `Admin@123` |
| **Customer** | `ayush@example.com` | `customer123` |
| **Customer** | `john@example.com` | `Customer@1` |
| **Customer** | `priya@example.com` | `Customer@1` |

### Sample Coupons (Tap to Apply at Checkout)
- `WELCOME10`: 10% OFF on all orders
- `SAVE50`: ₹50 OFF on orders above ₹200
- `FEAST20`: 20% OFF on orders above ₹500
- `FLAT100`: ₹100 OFF on orders above ₹600

---

## How to Run

1. **Launch Directly**:
   Double-click [run.bat](file:///c:/Users/ayush/Desktop/AS/Online%20Food%20Odering%20System/run.bat) or execute:
   ```powershell
   java -jar target/online-food-ordering-system-1.0-SNAPSHOT.jar
   ```
2. **Via Maven**:
   ```powershell
   mvn javafx:run
   ```
3. **Run Unit Tests**:
   ```powershell
   mvn test
   ```
   *(All 39 automated tests passing: Cart, Discount Engine, Home View, and Order State Machine).*
