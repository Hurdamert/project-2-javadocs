-- Creating all database tables

-- Categories Table:
CREATE TABLE Categories (
  category_id SERIAL PRIMARY KEY,
  category_name TEXT NOT NULL UNIQUE
);

-- Products Table:
CREATE TABLE Products (
	product_id SERIAL PRIMARY KEY,
	product_name TEXT NOT NULL,
	product_price NUMERIC(10, 2),
	category_id INT NOT NULL,
	FOREIGN KEY (category_id) REFERENCES Categories (category_id)
);

-- Ingredients Table:
CREATE TABLE Ingredients(
	ingredient_id SERIAL PRIMARY KEY,
	ingredient_name TEXT NOT NULL,
	quantity INT,
	minimum_quantity INT,
	full_quantity INT,
  ingredient_unit TEXT
);

-- ProductIngredients Table:
CREATE TABLE ProductIngredients(
	ingredient_id INT,
	product_id INT,
  ingredient_amount INT,
  FOREIGN KEY (ingredient_id) REFERENCES Ingredients (ingredient_id),
  FOREIGN KEY (product_id) REFERENCES Products (product_id)
);

-- AddOns table:
CREATE TABLE AddOns (
  addon_id SERIAL PRIMARY KEY,
  addon_name TEXT NOT NULL,
  addon_price NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (addon_price >= 0),
  ingredient_id INT,
  is_available BOOLEAN NOT NULL DEFAULT TRUE,
  ingredient_amount INT,
  FOREIGN KEY (ingredient_id) REFERENCES Ingredients (ingredient_id)
);
 
-- Employees Table:
CREATE TABLE Employees (
  employee_id SERIAL PRIMARY KEY,
  employee_name TEXT NOT NULL,
  role TEXT NOT NULL CHECK (role IN ('Cashier','Manager')),
  status TEXT NOT NULL DEFAULT 'Active' CHECK (status IN ('Active','Inactive'))
);

-- Orders Table:
CREATE TABLE Orders (
  order_id SERIAL PRIMARY KEY,
  employee_id INT NOT NULL,
  sub_total NUMERIC(10,2) NOT NULL CHECK (sub_total >= 0),
  date_time TIMESTAMP NOT NULL DEFAULT NOW(),
  FOREIGN KEY (employee_id) REFERENCES Employees (employee_id)
);

-- OrderItems Table:
CREATE TABLE OrderItems(
  order_item_id SERIAL PRIMARY KEY,
  order_id INT,
  product_id INT,
  qty INT NOT NULL CHECK (qty > 0),
  item_price NUMERIC(10, 2),
  FOREIGN KEY (order_id) REFERENCES Orders (order_id),
  FOREIGN KEY (product_id) REFERENCES Products (product_id)
);


-- Generates menu including products, ingredients, categories, addons

-- inserting categories
INSERT INTO Categories (category_name) VALUES
('Milky Series'), ('Fresh Milk Series'), ('Fruity Beverages'), ('Non Caffeinated'), ('Seasonal');

-- inserting products
-- Milky Series
INSERT INTO Products (product_name, product_price, category_id) VALUES
('Classic Pearl Milk Tea', 5.80, 1),
('Honey Pearl Milk Tea', 6.00, 1),
('Coffee Creama', 6.50, 1),
('Coffee Milk Tea w/ Coffee Jelly', 6.25, 1),
('Hokkaido Pearl Milk Tea', 6.25, 1),
('Thai Pearl Milk Tea', 6.25, 1),
('Taro Pearl Milk Tea', 6.25, 1),
('Mango Green Milk Tea', 6.50, 1),
('Golden Retriever', 6.75, 1),
('Coconut Pearl Milk Tea', 6.75, 1);

-- Fresh Milk Series
INSERT INTO Products (product_name, product_price, category_id) VALUES
('Fresh Milk Tea', 4.65, 2),
('Wintermelon w/ Fresh Milk', 5.20, 2),
('Cocoa Lover w/ Fresh Milk', 5.20, 2),
('Matcha Fresh Milk', 6.25, 2),
('Strawberry Matcha Fresh Milk', 6.50, 2);

-- Fruity Drinks
INSERT INTO Products (product_name, product_price, category_id) VALUES
('Mango Green Tea', 5.80, 3),
('Passion Fruit Tea', 6.25, 3),
('Berry Lychee Burst', 6.25, 3),
('Peach Tea w/ Honey Jelly', 6.25, 3),
('Mango & Passion Fruit Tea', 6.25, 3),
('Honey Lemonade', 5.20, 3);

-- Non Caffeinated
INSERT INTO Products (product_name, product_price, category_id) VALUES
('Halo Halo', 6.95, 4),
('Wintermelon Lemonade', 5.80, 4),
('Strawberry Coconut', 6.50, 4);

-- inserting Ingredients
INSERT INTO Ingredients (ingredient_name, quantity, minimum_quantity, full_quantity, ingredient_unit) VALUES
('Black Tea', 500000, 100000, 500000, 'ml'),
('Green Tea', 250000, 50000, 250000, 'ml'),
('Oolang Tea', 100000, 25000, 100000, 'ml'),
('Milk', 300000, 50000, 300000, 'ml'),
('Fresh Milk', 300000, 50000, 300000, 'ml'),
('Sugar Syrup', 140000, 50000, 140000, 'ml'),
('Pearls', 250000, 50000, 250000, 'g'),
('Ice', 300000, 50000, 300000, 'g'),
('Honey', 75000, 10000, 75000, 'ml'),
('Creama', 5000, 1000, 5000, 'ml'),
('Coffee', 25000, 5000, 25000, 'ml'),
('Black Tea + Coffee Mix', 25000, 5000, 25000, 'ml'),
('Coffee Jelly', 7000, 2000, 7000, 'g'),
('Caramel Syrup', 5000, 1000, 5000, 'ml'),
('Condensed Milk', 30000, 5000, 30000, 'ml'),
('Taro Powder', 10000, 2000, 10000, 'g'),
('Mango Syrup', 10000, 2000, 10000, 'ml'),
('Lychee Jelly', 7000, 2000, 7000, 'g'),
('Coconut Syrup', 7000, 2000, 7000, 'ml'),
('Wintermelon Syrup Base', 10000, 2000, 10000, 'ml'),
('Water', -1, -1, -1, 'ml'),
('Cocoa Powder', 5000, 1000, 5000, 'g'),
('Matcha Powder', 2000, 100, 2000, 'g'),
('Lemon Juice', 7000, 2000, 7000, 'ml'),
('Passion Fruit Syrup', 5000, 2000, 5000, 'ml'),
('Berry Syrup', 5000, 2000, 5000, 'ml'),
('Lychee Syrup', 5000, 2000, 5000, 'ml'),
('Peach Syrup', 5000, 2000, 5000, 'ml'),
('Honey Jelly', 7000, 2000, 7000, 'g'),
('Sweet Beans/Jelly/Fruit Mix', 5000, 2000, 5000, 'g'),
('Pudding', 5000, 2000, 5000, 'g'),
('Strawberry Syrup', 5000, 2000, 5000, 'ml'),
('Crystal Boba', 1000, 100, 1000, 'g'),
('Strawberry Popping Boba', 1000, 100, 1000, 'g'),
('Mango Popping Boba', 1000, 100, 1000, 'g'),
('Ice Cream', 3000, 500, 3000, 'g');

-- inserting ProductIngredients
INSERT INTO ProductIngredients (ingredient_id, product_id, ingredient_amount) VALUES
(1, 1, 200),
(4, 1, 120),
(6, 1, 20),
(7, 1, 80),
(1, 2, 200),
(4, 2, 120),
(9, 2, 30),
(7, 2, 80),
(8, 2, 100),
(11, 3, 200),
(4, 3, 100),
(6, 3, 30),
(10, 3, 50),
(8, 3, 100),
(12, 4, 180),
(4, 4, 120),
(6, 4, 20),
(13, 4, 80),
(8, 4, 100),
(1, 5, 200),
(4, 5, 120),
(14, 5, 30),
(7, 5, 80),
(8, 5, 100),
(1, 6, 200),
(15, 6, 100),
(6, 6, 20),
(7, 6, 80),
(8, 6, 100),
(1, 7, 200),
(4, 7, 100),
(16, 7, 40),
(7, 7, 80),
(8, 7, 100),
(2, 8, 180),
(4, 8, 100),
(17, 8, 40),
(7, 8, 80),
(8, 8, 100),
(1, 9, 100),
(4, 9, 50),
(7, 9, 60),
(31, 9, 40),
(13, 9, 40),
(18, 9, 40),
(8, 9, 100),
(1, 10, 180),
(4, 10, 100),
(19, 10, 30),
(7, 10, 80),
(8, 10, 100),
(1, 11, 200),
(5, 11, 200),
(6, 11, 20),
(8, 11, 100),
(20, 12, 150),
(5, 12, 180),
(21, 12, 80),
(8, 12, 100),
(22, 13, 30),
(5, 13, 200),
(6, 13, 20),
(8, 13, 100),
(23, 14, 5),
(5, 14, 220),
(6, 14, 20),
(8, 14, 100),
(32, 15, 40),
(5, 15, 200),
(6, 15, 20),
(8, 15, 100),
(2, 16, 200),
(17, 16, 40),
(6, 16, 20),
(8, 16, 100),
(1, 17, 200),
(25, 17, 40),
(6, 17, 20),
(8, 17, 100),
(1, 18, 180),
(26, 18, 30),
(27, 18, 30),
(6, 18, 20),
(8, 18, 100),
(1, 19, 180),
(28, 19, 40),
(29, 19, 60),
(8, 19, 100),
(2, 20, 180),
(17, 20, 30),
(25, 20, 30),
(6, 20, 20),
(8, 20, 100),
(24, 21, 40),
(21, 21, 200),
(9, 21, 30),
(8, 21, 100),
(30, 22, 80),
(4, 22, 150),
(6, 22, 20),
(8, 22, 100),
(20, 23, 40),
(24, 23, 40),
(21, 23, 150),
(8, 23, 100),
(23, 24, 5),
(4, 24, 150),
(6, 24, 20),
(8, 24, 100);

-- inserting AddOns
INSERT INTO AddOns (addon_name, addon_price, ingredient_id, ingredient_amount) VALUES
('Pearls', 0.75, 7, 80), 
('Crystal Boba', 1.00, 33, 70), 
('Honey Jelly', 0.75, 29, 50), 
('Lychee Jelly', 0.75, 18, 50), 
('Pudding', 0.75, 31, 40), 
('Strawberry Popping Boba', 1.00, 34, 60), 
('Mango Popping Boba', 1.00, 35, 60), 
('Creama', 1.00, 10, 40), 
('Coffee Jelly', 0.75, 13, 80), 
('Ice Cream', 1.00, 36, 60);

-- inserting employees
INSERT INTO Employees (employee_name, role) VALUES
('Evan Ganske', 'Cashier'),
('Licheng Yi', 'Cashier'),
('Sahil Kasturi', 'Cashier'),
('Jake Hewett', 'Cashier'),
('Hao Lin', 'Cashier'),
('Mason McGowan', 'Cashier');
