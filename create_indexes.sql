-- Creates a unique index on the `login` column in the `Users` table.
-- Ensures that each user has a unique login ID, preventing duplicate accounts.
CREATE UNIQUE INDEX idx_users_login ON Users(login);

-- Creates an index on the `role` column in the `Users` table.
-- Speeds up queries that filter or search users based on their role (e.g., customer, manager, driver).
CREATE INDEX idx_users_role ON Users(role);

-- Creates a unique index on the `itemName` column in the `Items` table.
-- Ensures that each item in the menu has a unique name, preventing duplicates.
CREATE UNIQUE INDEX idx_items_itemname ON Items(itemName);

-- Creates an index on the `typeOfItem` column in the `Items` table.
-- Improves performance when searching for items by category (e.g., entrees, drinks, sides).
CREATE INDEX idx_items_typeofitem ON Items(typeOfItem);

-- Creates an index on the `price` column in the `Items` table.
-- Speeds up queries that filter or sort items based on price.
CREATE INDEX idx_items_price ON Items(price);

-- Creates a unique index on the `storeID` column in the `Store` table.
-- Ensures each store has a unique store ID, preventing duplicates.
CREATE UNIQUE INDEX idx_store_storeid ON Store(storeID);

-- Creates an index on the `reviewScore` column in the `Store` table.
-- Speeds up queries that sort or filter stores based on review scores.
CREATE INDEX idx_store_reviewscore ON Store(reviewScore);

-- Creates an index on the `isOpen` column in the `Store` table.
-- Improves performance for queries checking which stores are currently open.
CREATE INDEX idx_store_isopen ON Store(isOpen);

-- Creates a unique index on the `orderID` column in the `FoodOrder` table.
-- Ensures each order has a unique order ID, preventing duplicates.
CREATE UNIQUE INDEX idx_foodorder_orderid ON FoodOrder(orderID);

-- Creates an index on the `login` column in the `FoodOrder` table.
-- Speeds up queries that retrieve a user’s order history.
CREATE INDEX idx_foodorder_login ON FoodOrder(login);

-- Creates an index on the `storeID` column in the `FoodOrder` table.
-- Improves performance for queries retrieving orders placed at a specific store.
CREATE INDEX idx_foodorder_storeid ON FoodOrder(storeID);

-- Creates an index on the `orderTimestamp` column in the `FoodOrder` table in descending order.
-- Speeds up queries that retrieve the most recent orders first.
CREATE INDEX idx_foodorder_timestamp ON FoodOrder(orderTimestamp DESC);

-- Creates an index on the `orderID` column in the `ItemsInOrder` table.
-- Improves performance for queries retrieving items associated with a specific order.
CREATE INDEX idx_itemsinorder_orderid ON ItemsInOrder(orderID);

-- Creates an index on the `itemName` column in the `ItemsInOrder` table.
-- Speeds up queries that search for specific items in past orders.
CREATE INDEX idx_itemsinorder_itemname ON ItemsInOrder(itemName);
