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
  product_amount INT,
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