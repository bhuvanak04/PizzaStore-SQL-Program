-- LOG TABLE: RoleChangeLog
CREATE TABLE IF NOT EXISTS RoleChangeLog (
    log_id SERIAL PRIMARY KEY, 
    user_updated VARCHAR(50), 
    old_role VARCHAR(20), 
    new_role VARCHAR(20), 
    changed_by VARCHAR(50), 
    change_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- LOG TABLE: OrderStatusLog
CREATE TABLE IF NOT EXISTS OrderStatusLog (
    log_id SERIAL PRIMARY KEY, 
    orderID INT NOT NULL, 
    old_status VARCHAR(50), 
    new_status VARCHAR(50), 
    changed_by VARCHAR(50), 
    change_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- LOG TABLE: OrderLog (Tracks all new orders)
CREATE TABLE IF NOT EXISTS OrderLog (HS
    log_id SERIAL PRIMARY KEY, 
    orderID INT NOT NULL, 
    login VARCHAR(50) NOT NULL, 
    storeID INT NOT NULL, 
    totalPrice DECIMAL(10,2), 
    orderStatus VARCHAR(50), 
    placed_by VARCHAR(50), 
    orderTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- STORED PROCEDURE: Log Role Change
CREATE OR REPLACE FUNCTION log_role_change() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO RoleChangeLog (user_updated, old_role, new_role, changed_by, change_timestamp) 
    VALUES (OLD.login, OLD.role, NEW.role, CURRENT_USER, NOW()); 
    RETURN NEW; 
END;
$$ LANGUAGE plpgsql;

-- STORED PROCEDURE: Log Order Placement
CREATE OR REPLACE FUNCTION log_order_placement() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO OrderLog(orderID, login, storeID, totalPrice, orderStatus, placed_by, orderTimestamp)
    VALUES (NEW.orderID, NEW.login, NEW.storeID, NEW.totalPrice, NEW.orderStatus, CURRENT_USER, NOW());
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- STORED PROCEDURE: Log Order Status Change
CREATE OR REPLACE FUNCTION log_order_status_change() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO OrderStatusLog(orderID, old_status, new_status, changed_by, change_timestamp)
    VALUES (NEW.orderID, OLD.orderStatus, NEW.orderStatus, CURRENT_USER, NOW());
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- TRIGGER: Log Role Changes
DROP TRIGGER IF EXISTS trg_log_role_change ON Users;

CREATE TRIGGER trg_log_role_change
AFTER UPDATE OF role ON Users
FOR EACH ROW
WHEN (OLD.role IS DISTINCT FROM NEW.role)
EXECUTE PROCEDURE log_role_change();

-- TRIGGER: Log Order Placement
DROP TRIGGER IF EXISTS trg_log_order_placement ON FoodOrder;

CREATE TRIGGER trg_log_order_placement
AFTER INSERT ON FoodOrder
FOR EACH ROW
EXECUTE PROCEDURE log_order_placement();

-- TRIGGER: Log Order Status Changes
DROP TRIGGER IF EXISTS trg_log_order_status_change ON FoodOrder;

CREATE TRIGGER trg_log_order_status_change
AFTER UPDATE OF orderStatus ON FoodOrder
FOR EACH ROW
WHEN (OLD.orderStatus IS DISTINCT FROM NEW.orderStatus)
EXECUTE PROCEDURE log_order_status_change();