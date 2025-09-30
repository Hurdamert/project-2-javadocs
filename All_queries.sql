-- Q1: Categories (POS tabs) -- Pass
SELECT category_id, category_name
FROM categories
ORDER BY category_name;

-- Q2: Products in a category (example: 1) -- Pass
SELECT product_id, product_name, product_price
FROM products
WHERE category_id = 1
ORDER BY product_name;

-- Q3: Add-ons for a category (example: 1) -- Pass
SELECT addon_id, addon_name, addon_price FROM addons
ORDER BY addon_name;

-- Special query Q4: Weekly Sales History -- Pass
SELECT date_part('isoyear', date_time)::int AS Years, date_part('week', date_time)::int AS Weeks, COUNT(*) AS orders FROM orders
GROUP BY Years, Weeks
ORDER BY Years, Weeks;

-- Special query Q5: Realistic Sales History (by hour) -- Pass
SELECT EXTRACT(HOUR FROM date_time) AS hour_of_day, COUNT(order_id) AS total_orders, SUM(sub_total) AS total_sales FROM Orders
GROUP BY hour_of_day
ORDER BY hour_of_day;


-- Special query Q6: Top Sales Day (top 10 by revenue) -- Pass
SELECT DATE(date_time) AS order_day, SUM(sub_total) AS total_sales FROM Orders
GROUP BY order_day
ORDER BY total_sales DESC
LIMIT 10;


-- Special Query Q7: Menu Item Inventory (ingredients per product)  <-- use productingredients -- Pass
SELECT p.product_name, COUNT(pi.ingredient_id) AS ingredient_count
FROM products p
JOIN productingredients pi ON pi.product_id = p.product_id
GROUP BY p.product_name
ORDER BY ingredient_count DESC, p.product_name;

-- Q8: Product Count per Category -- Pass
SELECT c.category_name, COUNT(*) AS products_in_category
FROM products p
JOIN categories c ON c.category_id = p.category_id
GROUP BY c.category_name
ORDER BY products_in_category DESC, c.category_name;

-- Q9: Top 10 products by units sold - Pass
SELECT p.product_name, SUM(oi.qty) AS units
FROM orderitems oi
JOIN products p ON p.product_id = oi.product_id
GROUP BY p.product_name
ORDER BY units DESC
LIMIT 10;

-- Q10: Sales by Category (revenue) -- Pass
SELECT c.category_name, ROUND(SUM(oi.qty * oi.item_price), 2) AS revenue FROM orderitems oi
JOIN products p ON p.product_id = oi.product_id
JOIN categories c ON c.category_id = p.category_id
GROUP BY c.category_name
ORDER BY revenue DESC;

-- Q11: Recent Orders -- Pass
SELECT order_id, sub_total, date_time
FROM orders
ORDER BY date_time DESC
LIMIT 20;

-- Special query Q12: Best of the Worst -- Pass
SELECT worst_day.order_day, p.product_name, worst_day.total_sales, SUM(oi.qty) AS total_qty FROM OrderItems oi JOIN Orders o ON oi.order_id = o.order_id JOIN Products p ON oi.product_id = p.product_id
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

-- Q13: Daily orders & revenues (last 30 days) -- Pass
SELECT DATE(date_time) AS day, COUNT(*) AS orders, ROUND(SUM(sub_total),2) AS revenue FROM orders
WHERE date_time >= NOW() - INTERVAL '31 days'
GROUP BY day
ORDER BY day;

-- Q14: Employee productivity (orders + revenue by cashier/manager) -- Pass
SELECT e.employee_name, e.role, COUNT(o.order_id) AS orders_rung, ROUND(SUM(o.sub_total), 2) AS revenue_rung FROM employees e
LEFT JOIN orders o ON o.employee_id = e.employee_id
GROUP BY e.employee_name, e.role
ORDER BY revenue_rung DESC NULLS LAST, orders_rung DESC;

-- Q15: Average customer order/receipt by weekday -- Pass
SELECT to_char(date_time, 'Day') AS day_of_week, ROUND(AVG(sub_total), 2) AS avg_ticket, COUNT(*) AS orders FROM orders
GROUP BY day_of_week, to_char(date_time, 'ID')
ORDER BY to_char(date_time, 'ID'); 

-- Q16: Ingredient Usage (total things consumed) -- Pass
SELECT i.ingredient_name, SUM(oi.qty) AS total_units_used, i.ingredient_unit FROM orderitems oi
JOIN productingredients pi ON pi.product_id = oi.product_id
JOIN ingredients i ON i.ingredient_id = pi.ingredient_id
GROUP BY i.ingredient_name, i.ingredient_unit
ORDER BY total_units_used DESC;

-- Q17: Products never sold (gap check) -- Pass
SELECT p.product_id, p.product_name FROM products p
LEFT JOIN orderitems oi ON oi.product_id = p.product_id
WHERE oi.order_item_id IS NULL
ORDER BY p.product_name;

-- Q18: Orders by hour & weekday -- Pass
SELECT to_char(date_time, 'Dy') AS dow, EXTRACT(HOUR FROM date_time) AS 24hour, COUNT(*) AS orders FROM orders
GROUP BY dow, to_char(date_time, 'ID'), 24hour
ORDER BY to_char(date_time, 'ID'), 24hour;







