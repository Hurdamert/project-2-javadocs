-- -- CREATE TABLE products (
-- --     product_name VARCHAR(255),
-- --     price INT
-- -- );

-- -- INSERT INTO products (product_name, price)
-- -- VALUES
-- -- ('Desktop Computer',800),
-- -- ('Laptop',1200),
-- -- ('Tablet',200),
-- -- ('Monitor',350),
-- -- ('Printer',150);

-- -- SELECT * FROM products;
-- CREATE TABLE EMPLOYEE(
--     employee_id INT PRIMARY KEY,
--     employee_name VARCHAR(255),
--     is_admin boolean;

-- );

-- CREATE TABLE ORDERS (
--     order_id INT PRIMARY KEY,
--     employee_id INT,
--     sub_total FLOAT,
--     date_time DATE,
--     week_id INT;

--     constraint order_made_by_employee FOREIGN KEY (employee_id) FROM REFERENCES EMPLOYEE(employee_id)

-- );



-- INSERT INTO CATEGORIES (category_id, category_name, active) VALUES ( );

-- INSERT INTO PRODUCTS (product_id, product_name, product_price, category_id) VALUES ( );

-- INSERT INTO EMPLOYEE (employee_id, employee_name, is_admin) VALUES ( );

-- INSERT INTO ORDERS (order_id, employee_id, sub_total, date_time) VALUES ( );

-- Categories Table:
CREATE TABLE categories (
  category_id   SERIAL PRIMARY KEY,
  category_name TEXT NOT NULL UNIQUE,
  active        BOOLEAN NOT NULL DEFAULT TRUE
);



-- Add-Ons table:
CREATE TABLE addons (
  addon_id     SERIAL PRIMARY KEY,
  addon_name   TEXT NOT NULL,
  price_delta  NUMERIC(6,2) NOT NULL DEFAULT 0 CHECK (price_delta >= 0),
  category_id  INT  NOT NULL REFERENCES categories(category_id) ON UPDATE CASCADE,
  is_available BOOLEAN NOT NULL DEFAULT TRUE,
  UNIQUE (category_id, addon_name)
);



-- Products table:
CREATE TABLE Products (
	product_id 	INT PRIMARY KEY,
	product_name TEXT NOT NULL,
	product_price 	DECIMAL,
	category_id 	INT,
	FOREIGN KEY (category_id) REFERENCES Categories (category_id)
);
-- Employee Table
CREATE TABLE employee (
  employee_id   SERIAL PRIMARY KEY,
  employee_name TEXT NOT NULL,
  role          TEXT NOT NULL CHECK (role IN ('Cashier','Manager')),
  status        TEXT NOT NULL DEFAULT 'Active' CHECK (status IN ('Active','Inactive'))
);

-- Order Table
CREATE TABLE orders (
  order_id    SERIAL PRIMARY KEY,
  employee_id INT NOT NULL REFERENCES employee(employee_id),
  order_ts    TIMESTAMPTZ NOT NULL,
  sub_total   NUMERIC(10,2) NOT NULL CHECK (sub_total >= 0),
  tax         NUMERIC(10,2) NOT NULL CHECK (tax >= 0),
  total       NUMERIC(10,2) NOT NULL CHECK (total >= 0),
  pay_type    TEXT NOT NULL CHECK (pay_type IN ('CASH','CARD','OTHER')),
  CONSTRAINT chk_totals CHECK (ABS((sub_total + tax) - total) <= 0.01)
);
CREATE INDEX idx_orders_ts  ON orders(order_ts);
CREATE INDEX idx_orders_emp ON orders(employee_id);


-- Line items for each order
CREATE TABLE order_items(
    order_item_id SERIAL PRIMARY KEY,
    order_id INT NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    product_id INT NOT NULL REFERENCES products(product_id),
    qty INT NOT NULL CHECK (qty > 0),
    unit_price NUMERIC(8,2) NOT NULL CHECK (unit_price >= 0)
);

-- Ingredients Table:
CREATE TABLE Ingredients(
	ingredient_id 	     	INT PRIMARY KEY,
	ingredient_name 	TEXT NOT NULL,
	quantity 	    	INT,
	minimum_quantity INT,
	full_quantity INT
);


CREATE TABLE ProductIngredients(
	ingredient_id INT,
	product_id INT,
FOREIGN KEY (ingredient_id) REFERENCES Ingredients(ingredient_id),
FOREIGN KEY (product_id) REFERENCES Products(product_id)
); 
