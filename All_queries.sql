-- Q1: Categories (POS tabs)
SELECT category_id, category_name
FROM categories
ORDER BY category_name;

-- Q2: Products in a category (example: 1)
SELECT product_id, product_name, product_price
FROM products
WHERE category_id = 1
ORDER BY product_name;

-- Q3: Add-ons for a category (example: 1)
SELECT addon_id, addon_name, price_delta
FROM addons
WHERE category_id = 1
ORDER BY addon_name;

-- Special query Q4: Weekly Sales History 
SELECT date_part('isoyear', order_ts)::int AS iso_year,
       date_part('week',    order_ts)::int AS iso_week,
       COUNT(*) AS orders
FROM orders
GROUP BY 1,2
ORDER BY 1,2;
--OR
-- SELECT EXTRACT(YEAR FROM date_time) AS year_num, EXTRACT(WEEK FROM date_time) AS week_num, COUNT(order_id) AS total_orders FROM Orders
-- GROUP BY year_num, week_num
-- ORDER BY year_num, week_num;

-- Special query Q5: Realistic Sales History (by hour)
SELECT to_char(order_ts, 'HH24') AS hour_of_day,
       COUNT(*)                  AS orders,
       ROUND(SUM(total), 2)      AS total_revenue
FROM orders
GROUP BY 1
ORDER BY 1;
--OR
-- SELECT EXTRACT(HOUR FROM date_time) AS hour_of_day, COUNT(order_id) AS total_orders, SUM(sub_total) AS total_sales FROM Orders
-- GROUP BY hour_of_day
-- ORDER BY hour_of_day;


-- Special query Q6: Top Sales Day (top 10 by revenue)
SELECT order_ts::date AS day,
       ROUND(SUM(total), 2) AS revenue
FROM orders
GROUP BY day
ORDER BY revenue DESC
LIMIT 10;
--OR
-- SELECT DATE(date_time) AS order_day, SUM(sub_total) AS total_sales FROM Orders
-- GROUP BY order_day
-- ORDER BY total_sales DESC
-- LIMIT 10;


-- Special Query Q7: Menu Item Inventory (ingredients per product)  <-- use productingredients
SELECT p.product_name, COUNT(pi.ingredient_id) AS ingredient_count
FROM products p
JOIN productingredients pi ON pi.product_id = p.product_id
GROUP BY p.product_name
ORDER BY ingredient_count DESC, p.product_name;

-- Q8: Product Count per Category
SELECT c.category_name, COUNT(*) AS products_in_category
FROM products p
JOIN categories c ON c.category_id = p.category_id
GROUP BY c.category_name
ORDER BY products_in_category DESC, c.category_name;

-- Q9: Top 10 products by units sold
SELECT p.product_name, SUM(oi.qty) AS units
FROM order_items oi
JOIN products p ON p.product_id = oi.product_id
GROUP BY p.product_name
ORDER BY units DESC
LIMIT 10;

-- Q10: Sales by Category (revenue)
SELECT c.category_name,
       ROUND(SUM(oi.qty * oi.unit_price), 2) AS revenue
FROM order_items oi
JOIN products p ON p.product_id = oi.product_id
JOIN categories c ON c.category_id = p.category_id
GROUP BY c.category_name
ORDER BY revenue DESC;

-- Q11: Recent Orders
SELECT order_id, order_ts, sub_total, tax, total, pay_type
FROM orders
ORDER BY order_ts DESC
LIMIT 20;

-- Special query Q12: Best of the Worst
SELECT worst_day.order_day, p.product_name, worst_day.total_sales, SUM(oi.item_quantity) AS total_qty FROM OrderItems oi JOIN Orders o ON oi.order_id = o.order_id JOIN Products p ON oi.product_id = p.product_id
JOIN (
    SELECT DATE(date_time) AS order_day, SUM(sub_total) AS total_sales
    FROM Orders
    GROUP BY DATE(date_time)
    ORDER BY total_sales ASC
    LIMIT 1
) worst_day
ON DATE(o.date_time) = worst_day.order_day
GROUP BY worst_day.order_day, p.product_name, worst_day.total_sales
ORDER BY total_qty DESC
LIMIT 1;


-- Q13: Daily orders & revenues (last 30 days)
SELECT order_ts::date AS day,
       COUNT(*)       AS orders,
       ROUND(SUM(total),2) AS revenue
FROM orders
WHERE order_ts >= NOW() - INTERVAL '30 days'
GROUP BY day
ORDER BY day;

-- Q14: Employee productivity (orders + revenue by cashier/manager)
SELECT e.employee_name,
       e.role,
       COUNT(o.order_id)        AS orders_rung,
       ROUND(SUM(o.total), 2)   AS revenue_rung
FROM employee e
LEFT JOIN orders o ON o.employee_id = e.employee_id
GROUP BY e.employee_name, e.role
ORDER BY revenue_rung DESC NULLS LAST, orders_rung DESC;

-- Q15: Average customer order/receipt by weekday
SELECT to_char(order_ts, 'Dy') AS dow,
       ROUND(AVG(total), 2)    AS avg_ticket,
       COUNT(*)                AS orders
FROM orders
GROUP BY 1
ORDER BY MIN(EXTRACT(DOW FROM order_ts));  

-- Q16: Ingredient Usage (total things consumed)
SELECT i.ingredient_name,
       i.unit,
       ROUND(SUM(oi.qty * pi.units_per_item), 3) AS total_units_used
FROM order_items oi
JOIN productingredients pi ON pi.product_id = oi.product_id
JOIN ingredients i        ON i.ingredient_id = pi.ingredient_id
GROUP BY i.ingredient_name, i.unit
ORDER BY total_units_used DESC;

-- Q17: Products never sold (gap check)
SELECT p.product_id, p.product_name
FROM products p
LEFT JOIN order_items oi ON oi.product_id = p.product_id
WHERE oi.order_item_id IS NULL
ORDER BY p.product_name;

-- Q18: Orders by hour & weekday
SELECT EXTRACT(DOW  FROM order_ts)::int AS dow,   
       EXTRACT(HOUR FROM order_ts)::int AS hour24,
       COUNT(*) AS orders
FROM orders
GROUP BY 1,2
ORDER BY 1,2;








