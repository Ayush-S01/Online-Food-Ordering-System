-- ============================================================
-- Online Food Ordering System - Sample Data (Multi-Cuisine)
-- Run AFTER schema.sql
-- ============================================================

USE food_ordering_system;

-- ============================================================
-- Customers (passwords: SHA-256)
-- admin@food.com   / Admin@123
-- john@example.com / Customer@1
-- priya@example.com/ Customer@1
-- ============================================================
INSERT INTO customers (name, email, password_hash, role) VALUES
('Admin User',    'admin@food.com',    SHA2('Admin@123',    256), 'ADMIN'),
('John Doe',      'john@example.com',  SHA2('Customer@1',   256), 'CUSTOMER'),
('Priya Sharma',  'priya@example.com', SHA2('Customer@1',   256), 'CUSTOMER')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- ============================================================
-- Food Items Across Multiple Cuisines
-- ============================================================
-- Clear existing to reseed cleanly
DELETE FROM order_items;
DELETE FROM payments;
DELETE FROM orders;
DELETE FROM food_items;

INSERT INTO food_items (name, category, cuisine, description, price, image_path, is_veg, rating, prep_time, available) VALUES
-- ---------------- NORTH INDIAN ----------------
('Paneer Tikka',           'Starters',    'North Indian', 'Soft cottage cheese cubes marinated in tandoori spiced yogurt, grilled in clay oven.', 180.00, 'paneer_tikka.png', 1, 4.7, '20 mins', 1),
('Hara Bhara Kabab',       'Starters',    'North Indian', 'Crisp spinach, green pea and potato patties lightly spiced with fresh mint.',           140.00, 'hara_bhara_kabab.png', 1, 4.5, '15 mins', 1),
('Dal Makhani',            'Main Course', 'North Indian', 'Slow-cooked black lentils simmered overnight with butter, cream and gentle spices.',     220.00, 'dal_makhani.png', 1, 4.8, '25 mins', 1),
('Butter Chicken',         'Main Course', 'North Indian', 'Tender boneless chicken roasted in tandoor and folded into a velvety makhani gravy.',   280.00, 'butter_chicken.png', 0, 4.9, '30 mins', 1),
('Paneer Butter Masala',   'Main Course', 'North Indian', 'Fresh paneer in a rich, mildly sweet cashew-tomato sauce with aromatic kasuri methi.',   250.00, 'paneer_butter_masala.png', 1, 4.7, '25 mins', 1),
('Chole Bhature',          'Main Course', 'North Indian', 'Spicy Amritsari chickpea curry paired with two fluffy golden puffed breads.',            180.00, 'chole_bhature.png', 1, 4.6, '20 mins', 1),
('Butter Naan',            'Breads',      'North Indian', 'Soft leavened tandoori bread glazed with fresh golden butter.',                           50.00, 'butter_naan.png', 1, 4.7, '10 mins', 1),
('Garlic Naan',            'Breads',      'North Indian', 'Tandoor-baked flatbread infused with roasted garlic flakes and fresh coriander.',         60.00, 'garlic_naan.png', 1, 4.8, '10 mins', 1),
('Gulab Jamun (2 pcs)',    'Desserts',    'North Indian', 'Warm melt-in-mouth milk-solid dumplings soaked in rose and cardamom sugar syrup.',        80.00, 'gulab_jamun_2_pcs.png', 1, 4.9, '5 mins',  1),
('Mango Lassi',            'Beverages',   'North Indian', 'Chilled sweet creamy yogurt shake blended with authentic Alphonso mango pulp.',           80.00, 'mango_lassi.png', 1, 4.8, '5 mins',  1),

-- ---------------- SOUTH INDIAN ----------------
('Masala Dosa',            'Main Course', 'South Indian', 'Golden crispy rice-lentil crepe filled with spiced potato masala, coconut chutney & sambar.', 140.00, 'masala_dosa.png', 1, 4.8, '15 mins', 1),
('Idli Vada Combo',        'Starters',    'South Indian', 'Two fluffy steamed rice cakes and one crispy medu vada served with hot vegetable sambar.',    110.00, 'idli_vada_combo.png', 1, 4.6, '10 mins', 1),
('Mysore Masala Dosa',     'Main Course', 'South Indian', 'Bangalore-style crispy dosa spread with fiery red garlic-chilli chutney and potato filling.',160.00, 'mysore_masala_dosa.png', 1, 4.7, '15 mins', 1),
('Chicken Chettinad',      'Main Course', 'South Indian', 'Fiery Tamil chicken curry with black peppercorns, roasted coconut and curry leaves.',        290.00, 'chicken_chettinad.png', 0, 4.8, '30 mins', 1),
('Chicken 65',             'Starters',    'South Indian', 'Deep-fried bite-sized chicken spiced with ginger, garlic, red chillies and curry leaves.',    200.00, 'chicken_65.png', 0, 4.7, '20 mins', 1),
('South Indian Filter Coffee','Beverages','South Indian', 'Classic degree decoction coffee brewed with chicory and frothed with thick boiling milk.',    50.00, 'south_indian_filter_coffee.png', 1, 4.9, '5 mins',  1),
('Semiya Payasam',         'Desserts',    'South Indian', 'Rich vermicelli pudding slow-cooked in sweetened whole milk, cardamom and roasted cashews.',  90.00, 'semiya_payasam.png', 1, 4.6, '10 mins', 1),

-- ---------------- CHINESE ----------------
('Veg Manchurian Dry',     'Starters',    'Chinese',      'Crispy fried cabbage and carrot dumplings tossed in spicy soya-chilli glaze with spring onions.', 160.00, 'veg_manchurian_dry.png', 1, 4.5, '20 mins', 1),
('Chilli Paneer',          'Starters',    'Chinese',      'Wok-tossed paneer cubes with crunchy bell peppers, onions and spicy dark soya sauce.',         210.00, 'chilli_paneer.png', 1, 4.7, '20 mins', 1),
('Veg Hakka Noodles',      'Rice & Noodles','Chinese',    'Classic stir-fried thin noodles with julienned vegetables and light white pepper seasoning.',   150.00, 'veg_hakka_noodles.png', 1, 4.6, '20 mins', 1),
('Schezwan Chicken Rice',  'Rice & Noodles','Chinese',    'Wok-tossed basmati rice with shredded chicken in our fiery house-special Schezwan sauce.',    240.00, 'schezwan_chicken_rice.png', 0, 4.8, '25 mins', 1),
('Crispy Veg Spring Rolls','Starters',    'Chinese',      'Deep-fried golden pastry sheets stuffed with savoury glass noodles and crunchy vegetables.',   140.00, 'crispy_veg_spring_rolls.png', 1, 4.5, '15 mins', 1),
('Steamed Chicken Momos',  'Starters',    'Chinese',      'Eight delicate Himalayan dumplings filled with seasoned minced chicken, served with spicy dip.',170.00, 'steamed_chicken_momos.png', 0, 4.7, '15 mins', 1),

-- ---------------- ITALIAN ----------------
('Margherita Pizza (10")', 'Main Course', 'Italian',      'Traditional thin crust with Italian San Marzano tomato sauce, fresh mozzarella & sweet basil.', 299.00, 'margherita_pizza_10.png', 1, 4.8, '25 mins', 1),
('Farmhouse Veg Pizza',    'Main Course', 'Italian',      'Stone-baked pizza loaded with bell peppers, button mushrooms, sweet corn, olives and mozzarella.', 349.00, 'farmhouse_veg_pizza.png', 1, 4.7, '25 mins', 1),
('Chicken Pepperoni Pizza','Main Course', 'Italian',      'Wood-fired style crust topped with spicy sliced chicken pepperoni and double mozzarella cheese.', 399.00, 'chicken_pepperoni_pizza.png', 0, 4.9, '25 mins', 1),
('Creamy Penne Alfredo',   'Main Course', 'Italian',      'Penne pasta tossed in a rich butter, garlic, fresh cream and Parmesan cheese white sauce.',   260.00, 'creamy_penne_alfredo.png', 1, 4.6, '20 mins', 1),
('Spicy Penne Arrabiata',  'Main Course', 'Italian',      'Al dente penne pasta tossed in a spicy sun-ripened tomato, crushed garlic and red chilli sauce.',250.00, 'spicy_penne_arrabiata.png', 1, 4.5, '20 mins', 1),
('Cheesy Garlic Bread',    'Starters',    'Italian',      'Toasted French baguette slices brushed with garlic-herb butter and loaded with molten mozzarella.',140.00, 'cheesy_garlic_bread.png', 1, 4.7, '15 mins', 1),
('Classic Tiramisu',       'Desserts',    'Italian',      'Espresso-soaked savoiardi sponge fingers layered with creamy mascarpone cheese and cocoa powder.',160.00, 'classic_tiramisu.png', 1, 4.9, '5 mins',  1),

-- ---------------- MEXICAN ----------------
('Loaded Cheesy Nachos',   'Starters',    'Mexican',      'Crunchy corn tortilla chips layered with warm cheddar sauce, refried beans, jalapeños & salsa.',180.00, 'loaded_cheesy_nachos.png', 1, 4.7, '15 mins', 1),
('Paneer Fiesta Burrito',  'Main Course', 'Mexican',      'Warm flour tortilla rolled with cilantro-lime rice, spiced paneer, black beans & sour cream.',  230.00, 'paneer_fiesta_burrito.png', 1, 4.6, '20 mins', 1),
('Crispy Chicken Tacos (3)','Main Course','Mexican',      'Three crunchy taco shells packed with crispy chicken, shredded lettuce, pico de gallo & chipotle.',260.00, 'crispy_chicken_tacos_3.png', 0, 4.8, '20 mins', 1),
('Grilled Veg Quesadilla', 'Main Course', 'Mexican',      'Toasted flour tortilla stuffed with melted Monterey Jack, grilled sweet peppers, corn & cilantro.',210.00, 'grilled_veg_quesadilla.png', 1, 4.5, '20 mins', 1),
('Cinnamon Churros',       'Desserts',    'Mexican',      'Golden fried pastry sticks coated in cinnamon sugar, served with warm bittersweet chocolate dip.',150.00, 'cinnamon_churros.png', 1, 4.8, '15 mins', 1),

-- ---------------- FAST FOOD ----------------
('Crispy Veg Burger',      'Main Course', 'Fast Food',    'Herb potato and corn patty topped with crisp lettuce, sliced tomato and mayo in a sesame bun.', 120.00, 'crispy_veg_burger.png', 1, 4.5, '15 mins', 1),
('Double Cheese Chicken Burger','Main Course','Fast Food','Two grilled chicken patties with melted cheddar slices, caramelized onions and secret burger sauce.',220.00, 'double_cheese_chicken_burger.png', 0, 4.9, '20 mins', 1),
('Peri-Peri French Fries', 'Starters',    'Fast Food',    'Crispy golden crinkle fries tossed generously in fiery African bird’s eye peri-peri spice dust.', 110.00, 'peri_peri_french_fries.png', 1, 4.7, '10 mins', 1),
('BBQ Chicken Wings (6 pcs)','Starters',  'Fast Food',    'Oven-baked and fried chicken wings glazed with smoky Kansas sweet barbecue sauce.',             240.00, 'bbq_chicken_wings_6_pcs.png', 0, 4.8, '20 mins', 1),
('Belgian Chocolate Shake','Beverages',   'Fast Food',    'Ultra-rich thick milkshake blended with pure Belgian dark chocolate fudge and vanilla ice cream.',140.00, 'belgian_chocolate_shake.png', 1, 4.9, '5 mins',  1),
('Cold Coffee with Ice Cream','Beverages','Fast Food',    'Chilled espresso blended with whole milk and topped with a scoop of creamy vanilla ice cream.',  110.00, 'cold_coffee_with_ice_cream.png', 1, 4.7, '5 mins',  1),

-- ---------------- BIRYANI & MUGHLAI ----------------
('Hyderabadi Chicken Biryani','Main Course','Biryani & Mughlai','Fragrant long-grain basmati layered with tender chicken pieces, saffron, mint and fried onions.',290.00, 'hyderabadi_chicken_biryani.png', 0, 4.9, '30 mins', 1),
('Lucknowi Veg Dum Biryani',  'Main Course','Biryani & Mughlai','Royal Awadhi-style fragrant rice cooked on slow dum with seasonal vegetables and paneer cubes.',  220.00, 'lucknowi_veg_dum_biryani.png', 1, 4.7, '25 mins', 1),
('Mutton Galouti Kabab',      'Starters',   'Biryani & Mughlai','Melt-in-mouth smoked minced mutton patties subtly flavored with 32 royal Nawabi spices.',        340.00, 'mutton_galouti_kabab.png', 0, 4.9, '25 mins', 1),
('Shahi Tukda',               'Desserts',   'Biryani & Mughlai','Deep-fried bread triangles dipped in saffron syrup and covered with rich thick condensed rabri.', 110.00, 'shahi_tukda.png', 1, 4.8, '10 mins', 1);

-- ============================================================
-- Discount Coupons
-- ============================================================
INSERT INTO discounts (coupon_code, discount_type, discount_value, minimum_order_amount, expiry_date, enabled) VALUES
('WELCOME10',  'PERCENTAGE', 10.00,   0.00, '2027-12-31', 1),
('SAVE50',     'FIXED',      50.00, 200.00, '2027-12-31', 1),
('FEAST20',    'PERCENTAGE', 20.00, 500.00, '2027-12-31', 1),
('FLAT100',    'FIXED',     100.00, 600.00, '2027-12-31', 1),
('EXPIRED',    'PERCENTAGE', 15.00,   0.00, '2020-01-01', 1),
('DISABLED',   'FIXED',      30.00,   0.00, '2027-12-31', 0)
ON DUPLICATE KEY UPDATE discount_value=VALUES(discount_value);
