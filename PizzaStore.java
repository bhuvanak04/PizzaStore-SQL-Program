/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Scanner;
import java.util.Arrays; 

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class PizzaStore {

    static String authorisedUser = null;
   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of PizzaStore
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public PizzaStore(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end PizzaStore

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            PizzaStore.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      PizzaStore esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the PizzaStore object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new PizzaStore (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Profile");
                System.out.println("2. Update Profile");
                System.out.println("3. View Menu");
                System.out.println("4. Place Order"); //make sure user specifies which store
                System.out.println("5. View Full Order ID History");
                System.out.println("6. View Past 5 Order IDs");
                System.out.println("7. View Order Information"); //user should specify orderID and then be able to see detailed information about the order
                System.out.println("8. View Stores"); 

                //**the following functionalities should only be able to be used by drivers & managers**
                System.out.println("9. Update Order Status");

                //**the following functionalities should ony be able to be used by managers**
                System.out.println("10. Update Menu");
                System.out.println("11. Update User");

                System.out.println(".........................");
                System.out.println("20. Log out");

                switch (readChoice()){
                   case 1: viewProfile(esql); break;
                   case 2: updateProfile(esql); break;
                   case 3: viewMenu(esql); break;
                   case 4: placeOrder(esql); break;
                   case 5: viewAllOrders(esql); break;
                   case 6: viewRecentOrders(esql); break;
                   case 7: viewOrderInfo(esql); break;
                   case 8: viewStores(esql); break;
                   case 9: updateOrderStatus(esql); break;
                   case 10: updateMenu(esql); break;
                   case 11: updateUser(esql); break;



                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/

public static void CreateUser(PizzaStore esql) {
    try {
        System.out.println("\n*** New User Registration ***");

        // Get login ID
        System.out.print("Create login Username: ");
        String login = in.readLine().trim();

        // Check if login ID already exists
        String checkUserSQL = "SELECT COUNT(*) FROM Users WHERE login = '" + login + "'";
        List<List<String>> result = esql.executeQueryAndReturnResult(checkUserSQL);

        int userExists = Integer.parseInt(result.get(0).get(0)); // Get the actual count

        if (userExists > 0) {
            System.out.println("Error: Login ID already exists. Try a different one.");
            return;
        }

        // Get password (must be at least 6 characters)
        System.out.print("Create password: ");
        String password = in.readLine().trim();
        if (password.length() < 6) {
            System.out.println("Error: Password must be at least 6 characters long.");
            return;
        }

        // Default role and favorite items
        String role = "customer";
        String favoriteItems = "";

        // Get phone number (must be exactly 10 digits)
        System.out.print("Enter phone number: ");
        String phoneNum = in.readLine().trim();
        if (!phoneNum.matches("\\d{10}")) {
            System.out.println("Error: Phone number must be exactly 10 digits.");
            return;
        }

        // Insert new user into the database using string-based SQL query
        String query = "INSERT INTO Users (login, password, role, favoriteItems, phoneNum) " +
                       "VALUES ('" + login + "', '" + password + "', '" + role + "', '" +
                       favoriteItems + "', '" + phoneNum + "')";
        esql.executeUpdate(query);

        System.out.println("✅ User created successfully!");

    } catch (Exception e) {
        System.out.println("Error while creating user: " + e.getMessage());
    }
}


// LOGIN FUNCTION: Allows a user to log in by checking their credentials in the database.
    public static String LogIn(PizzaStore esql) {
    try {
        System.out.println("\n*** User Login ***");
        int attempts = 0;
        final int MAX_ATTEMPTS = 3; // Maximum login attempts

        while (attempts < MAX_ATTEMPTS) {
            System.out.print("Enter Login ID: ");
            String login = in.readLine().trim();

            System.out.print("Enter Password: ");
            String password = in.readLine().trim();

            // Check if the login credentials exist in the database
            String sql = "SELECT role FROM Users WHERE login = '" + login + "' AND password = '" + password + "'";
            List<List<String>> result = esql.executeQueryAndReturnResult(sql);

            if (!result.isEmpty()) {
                String role = result.get(0).get(0).trim(); // Trim extra spaces from role
                System.out.println("\n✅ Login successful! Welcome, " + login + " (" + role + ")");
                authorisedUser = login; // Store the logged-in user
                return login; // Return username for session tracking
            } else {
                attempts++; // Increase the failed attempt counter
                System.out.println("\nInvalid login credentials. Please try again.");
                if (attempts < MAX_ATTEMPTS) {
                    System.out.println("Attempts remaining: " + (MAX_ATTEMPTS - attempts));
                } else {
                    System.out.println("\nToo many failed attempts. Returning to the main menu...");
                    return null; // Exit login process
                }
            }
        }
    } catch (Exception e) {
        System.out.println("\nAn unexpected error occurred. Please try again later.");
    }
    return null; // If all attempts are used up, return null
}



// VIEW PROFILE FUNCTION: Allows a logged-in user to view their profile information
public static void viewProfile(PizzaStore esql) {
    try {
        // Ensure the user is logged in
        if (authorisedUser == null) {
            System.out.println("\nError: No user logged in. Please log in first.");
            return;
        }

        // Fetch user details
        String query = "SELECT favoriteItems, phoneNum FROM Users WHERE login = '" + authorisedUser + "'";
        List<List<String>> result = esql.executeQueryAndReturnResult(query);

        if (result.isEmpty()) {
            System.out.println("\nError: User not found.");
            return;
        }

        // Extract user info
        String favoriteItems = result.get(0).get(0).isEmpty() ? "None" : result.get(0).get(0);
        String phoneNum = result.get(0).get(1);

        // Display profile
        System.out.println("\n========= YOUR PROFILE =========");
        System.out.println(" User: " + authorisedUser);
        System.out.println(" Phone Number: " + phoneNum);
        System.out.println(" Favorite Items: " + favoriteItems);
        System.out.println("================================");

    } catch (Exception e) {
        System.out.println("\nError retrieving profile. Please try again.");
    }
}


  // UPDATE PROFILE FUNCTION - Allows the logged-in user to update their phone number, favorite item, or password
public static void updateProfile(PizzaStore esql) {
    try {
        if (authorisedUser == null) {
            System.out.println("\nError: No user logged in. Please log in first.");
            return;
        }

        while (true) {
            // Display update options
            System.out.println("\n==== UPDATE PROFILE ====");
            System.out.println("1. Update Favorite Item");
            System.out.println("2. Update Phone Number");
            System.out.println("3. Update Password");
            System.out.println("4. Go Back");
            System.out.print("Choose an option: ");

            int choice = readChoice();

            if (choice == 4) {
                System.out.println("\nReturning to main menu...");
                break;
            }

            String column = "", newValue = "";

            switch (choice) {
                case 1:
                    System.out.print("Enter new favorite item: ");
                    column = "favoriteItems";
                    newValue = in.readLine().trim();
                    break;
                case 2:
                    do {
                        System.out.print("Enter new phone number (10 digits): ");
                        newValue = in.readLine().trim();
                        if (!newValue.matches("\\d{10}")) {
                            System.out.println("Invalid phone number! Must be exactly 10 digits.");
                        }
                    } while (!newValue.matches("\\d{10}"));
                    column = "phoneNum";
                    break;
                case 3:
                    do {
                        System.out.print("Enter new password (at least 6 characters): ");
                        newValue = in.readLine().trim();
                        if (newValue.length() < 6) {
                            System.out.println("Password must be at least 6 characters long.");
                        }
                    } while (newValue.length() < 6);
                    column = "password";
                    break;
                default:
                    System.out.println("Invalid choice! Please try again.");
                    continue;
            }

            // Update user profile
            String updateQuery = "UPDATE Users SET " + column + " = '" + newValue + "' WHERE login = '" + authorisedUser + "'";
            esql.executeUpdate(updateQuery);
            System.out.println("\n✅ Profile updated successfully!");
        }
    } catch (Exception e) {
        System.out.println("\nError updating profile. Please try again.");
    }
}


    // VIEW MENU FUNCTION - Allows users to browse and filter menu items
    public static void viewMenu(PizzaStore esql) {
    try {
        while (true) {
            // Display menu options
            System.out.println("\n===== MENU OPTIONS =====");
            System.out.println("1. View all menu items");
            System.out.println("2. Filter by category (entree, drinks, sides)");
            System.out.println("3. Filter by price range");
            System.out.println("4. Sort by price (Low to High)");
            System.out.println("5. Sort by price (High to Low)");
            System.out.println("6. Go back");
            System.out.print("Choose an option: ");

            int choice = readChoice();
            String query = "";

            switch (choice) {
                case 1:
                    query = "SELECT itemName, price, typeOfItem, description FROM Items";
                    break;
                case 2:
                    System.out.print("Enter food type (entree, drinks, sides): ");
                    String type = in.readLine().trim().toLowerCase();
                    if (!Arrays.asList("entree", "drinks", "sides").contains(type)) {
                        System.out.println("Invalid type! Please enter 'entree', 'drinks', or 'sides'.");
                        continue;
                    }
                    query = "SELECT itemName, price, typeOfItem, description FROM Items WHERE typeOfItem = '" + type + "'";
                    break;
                case 3:
                    System.out.print("Enter maximum price: ");
                    double maxPrice = Double.parseDouble(in.readLine().trim());
                    query = "SELECT itemName, price, typeOfItem, description FROM Items WHERE price <= " + maxPrice;
                    break;
                case 4:
                    query = "SELECT itemName, price, typeOfItem, description FROM Items ORDER BY price ASC";
                    break;
                case 5:
                    query = "SELECT itemName, price, typeOfItem, description FROM Items ORDER BY price DESC";
                    break;
                case 6:
                    System.out.println("\nReturning to main menu...");
                    return;
                default:
                    System.out.println("Invalid choice! Please try again.");
                    continue;
            }

            // Execute the query and display results
            List<List<String>> results = esql.executeQueryAndReturnResult(query);

            if (results.isEmpty()) {
                System.out.println("\nNo items found.");
                continue;
            }

            System.out.println("\n===== MENU ITEMS =====");
            System.out.printf("%-20s %-10s %-10s %-30s\n", "Item Name", "Price", "Type", "Description");
            System.out.println("-------------------------------------------------------------");

            for (List<String> row : results) {
                System.out.printf("%-20s $%-9.2f %-10s %-30s\n", row.get(0), Double.parseDouble(row.get(1)), row.get(2), row.get(3));
            }
        }

    } catch (Exception e) {
        System.out.println("Error while browsing the menu. Please try again.");
    }
}

    // PLACE ORDER FUNCTION - Allows a logged-in user to place an order from a selected store
public static void placeOrder(PizzaStore esql) {
    try {
        // Step 1: Check if the user is logged in before placing an order
        if (authorisedUser == null) {
            System.out.println("\nError: No user logged in. Please log in first.");
            return;
        }

        // Step 2: Show available stores to the user
        System.out.println("\n=== Available Stores ===");
        esql.executeQueryAndPrintResult("SELECT storeID, address FROM Store");

        // Step 3: Ask the user which store they want to order from
        System.out.print("\nEnter Store ID: ");
        int storeID = Integer.parseInt(in.readLine().trim());

        // Step 4: Fetch the store's address
        String storeAddressQuery = "SELECT address FROM Store WHERE storeID = " + storeID;
        List<List<String>> storeResult = esql.executeQueryAndReturnResult(storeAddressQuery);

        if (storeResult.isEmpty()) {
            System.out.println("\nError: Store ID not found.");
            return;
        }

        String storeAddress = storeResult.get(0).get(0); // Retrieve the store's address

        // Step 5: Show menu items to the user
        System.out.println("\n=== Menu Items ===");
        esql.executeQueryAndPrintResult("SELECT itemName, price FROM Items");

        // Step 6: User selects items to order
        List<String> orderedItems = new ArrayList<>();
        List<Integer> itemQuantities = new ArrayList<>();
        double totalPrice = 0.0;

        while (true) {
            System.out.print("\nEnter item name (or type 'done' to finish): ");
            String itemName = in.readLine().trim();
            if (itemName.equalsIgnoreCase("done")) break; // Stop item selection

            System.out.print("Enter quantity: ");
            int quantity = Integer.parseInt(in.readLine().trim());

            // Check if the item exists and get its price
            String priceQuery = "SELECT price FROM Items WHERE itemName = '" + itemName + "'";
            List<List<String>> result = esql.executeQueryAndReturnResult(priceQuery);

            if (!result.isEmpty()) {
                double itemPrice = Double.parseDouble(result.get(0).get(0));
                totalPrice += itemPrice * quantity; // Update total price
                orderedItems.add(itemName);
                itemQuantities.add(quantity);
            } else {
                System.out.println("\nError: Item '" + itemName + "' not found in the menu.");
            }
        }

        // Step 7: Ensure the user selected at least one valid item
        if (orderedItems.isEmpty()) {
            System.out.println("\nError: No valid items selected. Order not placed.");
            return;
        }

        // Step 8: Insert order details into the FoodOrder table
        String insertOrderQuery = "INSERT INTO FoodOrder (login, storeID, totalPrice, orderStatus, orderTimestamp) " +
                                  "VALUES ('" + authorisedUser + "', " + storeID + ", " + totalPrice + ", 'Processing', NOW()) RETURNING orderID";
        List<List<String>> orderResult = esql.executeQueryAndReturnResult(insertOrderQuery);

        // If order creation failed
        if (orderResult.isEmpty()) {
            System.out.println("\nError: Order creation failed.");
            return;
        }

        // Get generated order ID from the database
        int orderID = Integer.parseInt(orderResult.get(0).get(0));
        
        // Step 9: Display order confirmation including store address
        System.out.println("\n✅ Order placed successfully!");
        System.out.println(" Order ID: " + orderID);
        System.out.println(" Store Location: " + storeAddress);
        System.out.println(" Total Price: $" + String.format("%.2f", totalPrice));

        // Step 10: Insert each ordered item into the ItemsInOrder table
        for (int i = 0; i < orderedItems.size(); i++) {
            String insertItemQuery = "INSERT INTO ItemsInOrder (orderID, itemName, quantity) " +
                                     "VALUES (" + orderID + ", '" + orderedItems.get(i) + "', " + itemQuantities.get(i) + ")";
            esql.executeUpdate(insertItemQuery);
            System.out.println("Added: " + orderedItems.get(i) + " | Quantity: " + itemQuantities.get(i));
        }

        System.out.println("\nAll items added to order!");

    } catch (Exception e) {
        System.out.println("\nError placing order. Please try again.");
    }
}


    // VIEW ALL ORDERS FUNCTION: Displays order history based on the user's role
public static void viewAllOrders(PizzaStore esql) {
    try {
        // Step 1: Ensure a user is logged in before accessing order history
        if (authorisedUser == null) {
            System.out.println("\nError: No user logged in. Please log in first.");
            return;
        }

        // Step 2: Retrieve the user's role from the database
        String roleQuery = "SELECT role FROM Users WHERE login = '" + authorisedUser + "'";
        List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);

        if (roleResult.isEmpty()) {
            System.out.println("\nError: User role not found.");
            return;
        }

        String userRole = roleResult.get(0).get(0).trim().toLowerCase(); // Get the user's role

        // Step 3: Define the SQL query based on user role
        String orderQuery;
        if (userRole.equals("manager") || userRole.equals("driver")) {
            System.out.println("\n=== All Orders (Manager/Driver View) ===");
            orderQuery = "SELECT orderID, login AS Customer, storeID, totalPrice, orderStatus, orderTimestamp FROM FoodOrder ORDER BY orderTimestamp DESC";
        } else {
            System.out.println("\n=== Your Order History ===");
            orderQuery = "SELECT orderID, storeID, totalPrice, orderStatus, orderTimestamp FROM FoodOrder WHERE login = '" + authorisedUser + "' ORDER BY orderTimestamp DESC";
        }

        // Step 4: Fetch and display the orders
        List<List<String>> orders = esql.executeQueryAndReturnResult(orderQuery);

        if (orders.isEmpty()) {
            System.out.println("\nNo orders found.");
            return;
        }

        // Step 5: Print orders in a formatted way
        if (userRole.equals("manager") || userRole.equals("driver")) {
            System.out.printf("%-10s %-15s %-10s %-12s %-15s %-20s\n", "OrderID", "Customer", "StoreID", "Total Price", "Order Status", "Timestamp");
        } else {
            System.out.printf("%-10s %-10s %-12s %-15s %-20s\n", "OrderID", "StoreID", "Total Price", "Order Status", "Timestamp");
        }
        System.out.println("------------------------------------------------------------");

        // Step 6: Display order details
        for (List<String> order : orders) {
            if (userRole.equals("manager") || userRole.equals("driver")) {
                System.out.printf("%-10s %-15s %-10s $%-11.2f %-15s %-20s\n",
                        order.get(0), order.get(1), order.get(2), Double.parseDouble(order.get(3)), order.get(4), order.get(5));
            } else {
                System.out.printf("%-10s %-10s $%-11.2f %-15s %-20s\n",
                        order.get(0), order.get(1), Double.parseDouble(order.get(2)), order.get(3), order.get(4));
            }
        }

    } catch (Exception e) {
        System.out.println("\nError fetching order history. Please try again.");
    }
}


    // VIEW RECENT 5 ORDERS - Displays the last 5 orders for customers and all users, including items in each order
public static void viewRecentOrders(PizzaStore esql) {
    try {
        // Ensure user is logged in
        if (authorisedUser == null) {
            System.out.println("\nError: No user logged in. Please log in first.");
            return;
        }

        // Step 1: Get user role
        String roleQuery = "SELECT role FROM Users WHERE login = '" + authorisedUser + "'";
        List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);

        if (roleResult.isEmpty()) {
            System.out.println("\nError: User role not found.");
            return;
        }

        String userRole = roleResult.get(0).get(0).trim().toLowerCase();

        // Step 2: Define SQL query based on user role
        String orderQuery = userRole.equals("manager") || userRole.equals("driver") ?
                "SELECT orderID, login AS Customer, storeID, totalPrice, orderStatus, orderTimestamp FROM FoodOrder ORDER BY orderTimestamp DESC LIMIT 5" :
                "SELECT orderID, storeID, totalPrice, orderStatus, orderTimestamp FROM FoodOrder WHERE login = '" + authorisedUser + "' ORDER BY orderTimestamp DESC LIMIT 5";

        System.out.println(userRole.equals("manager") || userRole.equals("driver") ?
                "\n=== 5 Most Recent Orders ===" : "\n=== Your 5 Most Recent Orders ===");

        // Step 3: Fetch orders from the database
        List<List<String>> orders = esql.executeQueryAndReturnResult(orderQuery);

        if (orders.isEmpty()) {
            System.out.println("\nNo recent orders found.");
            return;
        }

        // Step 4: Print order history
        System.out.printf(userRole.equals("manager") || userRole.equals("driver") ?
                "%-10s %-15s %-10s %-12s %-15s %-20s\n" :
                "%-10s %-10s %-12s %-15s %-20s\n",
                "OrderID", userRole.equals("manager") || userRole.equals("driver") ? "Customer" : "StoreID", "StoreID", "Total Price", "Order Status", "Timestamp");

        System.out.println("------------------------------------------------------------");

        for (List<String> order : orders) {
            if (userRole.equals("manager") || userRole.equals("driver")) {
                System.out.printf("%-10s %-15s %-10s $%-11.2f %-15s %-20s\n",
                        order.get(0), order.get(1), order.get(2), Double.parseDouble(order.get(3)), order.get(4), order.get(5));
            } else {
                System.out.printf("%-10s %-10s $%-11.2f %-15s %-20s\n",
                        order.get(0), order.get(1), Double.parseDouble(order.get(2)), order.get(3), order.get(4));
            }
        }

    } catch (Exception e) {
        System.out.println("\nError retrieving recent orders. Please try again.");
    }
}


    // VIEW ORDER INFO FUNCTION - Displays detailed information for a specific order (using the orderID)
public static void viewOrderInfo(PizzaStore esql) {
    try {
        // Ensure user is logged in
        if (authorisedUser == null) {
            System.out.println("\nError: No user logged in. Please log in first.");
            return;
        }

        // Step 1: Get user role
        String roleQuery = "SELECT role FROM Users WHERE login = '" + authorisedUser + "'";
        List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);

        if (roleResult.isEmpty()) {
            System.out.println("\nError: User role not found.");
            return;
        }

        String userRole = roleResult.get(0).get(0).trim().toLowerCase();

        // Step 2: Ask user for Order ID
        System.out.print("\nEnter the Order ID to view details: ");
        int orderID = Integer.parseInt(in.readLine().trim());

        // Step 3: Define SQL query based on user role
        String orderQuery = userRole.equals("manager") || userRole.equals("driver") ?
                "SELECT orderID, storeID, totalPrice, orderStatus, orderTimestamp, login AS Customer FROM FoodOrder WHERE orderID = " + orderID :
                "SELECT orderID, storeID, totalPrice, orderStatus, orderTimestamp FROM FoodOrder WHERE orderID = " + orderID + " AND login = '" + authorisedUser + "'";

        // Fetch order details
        List<List<String>> orderResult = esql.executeQueryAndReturnResult(orderQuery);

        if (orderResult.isEmpty()) {
            System.out.println("\nError: Order not found or you do not have permission to view it.");
            return;
        }

        // Step 4: Extract order details with correctly matched labels
        List<String> order = orderResult.get(0);

        System.out.println("\n=========================================");
        System.out.println(" Order ID:      " + order.get(0));
        System.out.println(" Store ID:      " + order.get(1));
        System.out.println(" Total Price:   $" + String.format("%.2f", Double.parseDouble(order.get(2))));
        System.out.println(" Status:        " + order.get(3));
        System.out.println(" Ordered On:    " + order.get(4));

        if (userRole.equals("manager") || userRole.equals("driver")) {
            System.out.println(" Customer:      " + order.get(5)); // Managers & Drivers can see customer name
        }
        System.out.println("=========================================");

        // Step 5: Fetch items in the order
        String itemQuery = "SELECT itemName, quantity FROM ItemsInOrder WHERE orderID = " + orderID;
        List<List<String>> itemResults = esql.executeQueryAndReturnResult(itemQuery);

        System.out.println("\nItems in Order:");
        if (itemResults.isEmpty()) {
            System.out.println(" - No items found.");
        } else {
            for (List<String> item : itemResults) {
                System.out.println(" - " + item.get(0) + " (x" + item.get(1) + ")");
            }
        }

        System.out.println("=========================================");

    } catch (Exception e) {
        System.out.println("\nError retrieving order details. Please try again.");
    }
}

    // VIEW STORES FUNCTION: Displays a list of all stores with their details
public static void viewStores(PizzaStore esql) {
    try {
        // Ensure user is logged in before accessing store information
        if (authorisedUser == null) {
            System.out.println("\nError: No user logged in. Please log in first.");
            return;
        }

        // Step 1: Fetch all stores from the database
        String storeQuery = "SELECT storeID, address, reviewScore, isOpen FROM Store ORDER BY storeID";
        List<List<String>> stores = esql.executeQueryAndReturnResult(storeQuery);

        // Step 2: Check if stores exist
        if (stores.isEmpty()) {
            System.out.println("\nNo stores available.");
            return;
        }

        // Step 3: Display store details in a simple list format
        System.out.println("\n================ STORES LIST ================");
        for (List<String> store : stores) {
            System.out.println("\nStore ID:      " + store.get(0));
            System.out.println("Address:       " + store.get(1));
            System.out.println("Review Score:  " + store.get(2));
            System.out.println("Open Status:   " + (store.get(3).equals("t") ? "Open" : "Closed"));
            System.out.println("=============================================");
        }

    } catch (Exception e) {
        System.out.println("\nError retrieving store information. Please try again.");
    }
}

    // UPDATE ORDER STATUS FUNCTION: Allows managers & drivers to update order status
public static void updateOrderStatus(PizzaStore esql) {
    try {
        // Step 1: Ensure user is logged in
        if (authorisedUser == null) {
            System.out.println("\nError: No user logged in. Please log in first.");
            return;
        }

        // Step 2: Verify that the user is either a manager or a driver
        String roleQuery = "SELECT role FROM Users WHERE login = '" + authorisedUser + "'";
        List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);

        if (roleResult.isEmpty()) {
            System.out.println("\nError: User role not found.");
            return;
        }

        String userRole = roleResult.get(0).get(0).trim().toLowerCase();

        if (!userRole.equals("manager") && !userRole.equals("driver")) {
            System.out.println("\nAccess Denied! Only managers or drivers can update order status.");
            return;
        }

        // Step 3: Display all existing orders
        System.out.println("\n========== ALL ORDERS ==========");
        String viewOrdersQuery = "SELECT orderID, login AS Customer, storeID, totalPrice, orderStatus FROM FoodOrder";
        esql.executeQueryAndPrintResult(viewOrdersQuery);

        // Step 4: Ask user for the Order ID to update
        System.out.print("\nEnter the Order ID to update: ");
        int orderID = Integer.parseInt(in.readLine().trim());

        // Step 5: Check if the entered order ID exists
        String checkOrderQuery = "SELECT orderStatus FROM FoodOrder WHERE orderID = " + orderID;
        List<List<String>> orderResult = esql.executeQueryAndReturnResult(checkOrderQuery);

        if (orderResult.isEmpty()) {
            System.out.println("\nError: Order ID not found.");
            return;
        }

        // Step 6: Show possible status options
        System.out.println("\nPossible Status Options:");
        System.out.println(" - Processing");
        System.out.println(" - Out for Delivery");
        System.out.println(" - Delivered");
        System.out.println(" - Cancelled");
        System.out.print("\nEnter the new order status: ");
        String newStatus = in.readLine().trim();

        // Step 7: Validate input status
        List<String> validStatuses = Arrays.asList("Processing", "Out for Delivery", "Delivered", "Cancelled");
        if (!validStatuses.contains(newStatus)) {
            System.out.println("\nError: Invalid status. Please enter a valid status.");
            return;
        }

        // Step 8: Update order status in the database
        String updateQuery = "UPDATE FoodOrder SET orderStatus = '" + newStatus + "' WHERE orderID = " + orderID;
        esql.executeUpdate(updateQuery);

        System.out.println("\n✅ Order status updated successfully!");

    } catch (NumberFormatException e) {
        System.out.println("\nError: Invalid Order ID format. Please enter a number.");
    } catch (Exception e) {
        System.out.println("\nError updating order status. Please try again.");
    }
}

    // UPDATE MENU FUNCTION: Allows managers to view, update, and add menu items
public static void updateMenu(PizzaStore esql) {
    try {
        // Step 1: Ensure user is logged in
        if (authorisedUser == null) {
            System.out.println("\nError: No user logged in. Please log in first.");
            return;
        }

        // Step 2: Verify that the user is a manager
        String roleQuery = "SELECT role FROM Users WHERE login = '" + authorisedUser + "'";
        List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);

        if (roleResult.isEmpty() || !roleResult.get(0).get(0).trim().equalsIgnoreCase("manager")) {
            System.out.println("\nAccess Denied! Only managers can modify the menu.");
            return;
        }

        while (true) {
            // Step 3: Display menu options for managers
            System.out.println("\n===== MENU MANAGEMENT =====");
            System.out.println("1. View Menu");
            System.out.println("2. Update Menu Item");
            System.out.println("3. Add New Menu Item");
            System.out.println("4. Go Back");
            System.out.print("Choose an option: ");
            int choice = readChoice();

            if (choice == 4) {
                System.out.println("\nReturning to main menu...");
                break;
            }

            switch (choice) {
                case 1:
                    // View all menu items
                    System.out.println("\nDisplaying menu...");
                    String viewQuery = "SELECT itemName, typeOfItem, price, description FROM Items";
                    esql.executeQueryAndPrintResult(viewQuery);
                    break;

                case 2:
                    // Update an existing menu item
                    System.out.print("\nEnter the name of the item to update: ");
                    String itemName = in.readLine().trim();

                    // Check if the item exists
                    String checkQuery = "SELECT * FROM Items WHERE itemName = '" + itemName + "'";
                    List<List<String>> itemCheck = esql.executeQueryAndReturnResult(checkQuery);

                    if (itemCheck.isEmpty()) {
                        System.out.println("\nError: Item not found in the menu.");
                        break;
                    }

                    // Ask what to update
                    System.out.println("\nWhat would you like to update?");
                    System.out.println("1. Price");
                    System.out.println("2. Ingredients");
                    System.out.println("3. Description");
                    System.out.print("Choose an option: ");
                    int updateChoice = readChoice();

                    String column = "", newValue = "";

                    switch (updateChoice) {
                        case 1:
                            System.out.print("\nEnter new price: ");
                            newValue = in.readLine().trim();
                            column = "price";
                            break;
                        case 2:
                            System.out.print("\nEnter new ingredients (comma-separated): ");
                            newValue = "'" + in.readLine().trim() + "'";
                            column = "ingredients";
                            break;
                        case 3:
                            System.out.print("\nEnter new description: ");
                            newValue = "'" + in.readLine().trim() + "'";
                            column = "description";
                            break;
                        default:
                            System.out.println("\nInvalid choice. Returning to menu.");
                            continue;
                    }

                    // Execute the update query
                    String updateQuery = "UPDATE Items SET " + column + " = " + newValue + " WHERE itemName = '" + itemName + "'";
                    esql.executeUpdate(updateQuery);
                    System.out.println("\n✅ Menu item updated successfully!");
                    break;

                case 3:
                    // Add a new menu item
                    System.out.print("\nEnter new item name: ");
                    String newItemName = in.readLine().trim();

                    // Check if item already exists
                    String checkNewItemQuery = "SELECT * FROM Items WHERE itemName = '" + newItemName + "'";
                    List<List<String>> newItemCheck = esql.executeQueryAndReturnResult(checkNewItemQuery);

                    if (!newItemCheck.isEmpty()) {
                        System.out.println("\nError: Item already exists in the menu.");
                        break;
                    }

                    // Gather details
                    System.out.print("Enter ingredients (comma-separated): ");
                    String ingredients = in.readLine().trim();
                    System.out.print("Enter type of item (Pizza, Burger, Drink, etc.): ");
                    String typeOfItem = in.readLine().trim();
                    System.out.print("Enter price: ");
                    double price = Double.parseDouble(in.readLine().trim());
                    System.out.print("Enter description: ");
                    String description = in.readLine().trim();

                    // Insert new item into the menu
                    String addItemQuery = "INSERT INTO Items (itemName, ingredients, typeOfItem, price, description) " +
                                          "VALUES ('" + newItemName + "', '" + ingredients + "', '" + typeOfItem + "', " + price + ", '" + description + "')";
                    esql.executeUpdate(addItemQuery);
                    System.out.println("\n✅ New menu item added successfully!");
                    break;

                default:
                    System.out.println("\nInvalid choice. Try again.");
            }
        }
    } catch (Exception e) {
        System.out.println("\nError updating menu. Please try again.");
    }
}


// UPDATE USER FUNCTION - Allows a manager to view users, update usernames, and change user roles
public static void updateUser(PizzaStore esql) {
    try {
        // Step 1: Ensure user is logged in
        if (authorisedUser == null) {
            System.out.println("\nError: No user logged in. Please log in first.");
            return;
        }

        // Step 2: Verify that the user is a manager
        String roleQuery = "SELECT role FROM Users WHERE login = '" + authorisedUser + "'";
        List<List<String>> roleResult = esql.executeQueryAndReturnResult(roleQuery);

        if (roleResult.isEmpty() || !roleResult.get(0).get(0).trim().equalsIgnoreCase("manager")) {
            System.out.println("\nAccess Denied! Only managers can update user information.");
            return;
        }

        while (true) {
            // Step 3: Display user management options
            System.out.println("\n===== USER MANAGEMENT =====");
            System.out.println("1. View All Users");
            System.out.println("2. Update a User’s Login ID");
            System.out.println("3. Change a User’s Role");
            System.out.println("4. Go Back");
            System.out.print("Choose an option: ");
            int choice = readChoice();

            if (choice == 4) {
                System.out.println("\nReturning to main menu...");
                break;
            }

            switch (choice) {
                case 1:
                    // View all users
                    System.out.println("\nDisplaying all users...");
                    String viewUsersQuery = "SELECT login, role, phonenum, favoriteitems FROM Users";
                    esql.executeQueryAndPrintResult(viewUsersQuery);
                    break;

                case 2:
                    // Update a user's login ID
                    System.out.print("\nEnter the current login ID: ");
                    String oldLogin = in.readLine().trim();

                    // Step 4: Check if the user exists
                    String checkUserQuery = "SELECT login FROM Users WHERE login = '" + oldLogin + "'";
                    List<List<String>> userCheck = esql.executeQueryAndReturnResult(checkUserQuery);

                    if (userCheck.isEmpty()) {
                        System.out.println("\nError: User not found.");
                        break;
                    }

                    System.out.print("Enter the new login ID: ");
                    String newLogin = in.readLine().trim();

                    // Step 5: Ensure the new login ID is unique
                    String checkNewUserQuery = "SELECT login FROM Users WHERE login = '" + newLogin + "'";
                    List<List<String>> newUserCheck = esql.executeQueryAndReturnResult(checkNewUserQuery);

                    if (!newUserCheck.isEmpty()) {
                        System.out.println("\nError: This login ID is already taken. Please choose another.");
                        break;
                    }

                    // Step 6: Update the login ID
                    String updateQuery = "UPDATE Users SET login = '" + newLogin + "' WHERE login = '" + oldLogin + "'";
                    esql.executeUpdate(updateQuery);
                    System.out.println("\n✅ Username successfully updated!");
                    break;

                case 3:
                    // Change a user's role
                    System.out.print("\nEnter the login ID of the user: ");
                    String userToChangeRole = in.readLine().trim();

                    // Step 7: Check if the user exists
                    String checkRoleUserQuery = "SELECT role FROM Users WHERE login = '" + userToChangeRole + "'";
                    List<List<String>> roleUserCheck = esql.executeQueryAndReturnResult(checkRoleUserQuery);

                    if (roleUserCheck.isEmpty()) {
                        System.out.println("\nError: User not found.");
                        break;
                    }

                    String currentRole = roleUserCheck.get(0).get(0).trim();

                    System.out.print("\nEnter the new role (customer, driver, manager): ");
                    String newRole = in.readLine().trim().toLowerCase();

                    // Step 8: Validate the new role
                    List<String> validRoles = Arrays.asList("customer", "driver", "manager");
                    if (!validRoles.contains(newRole)) {
                        System.out.println("\nError: Invalid role! Role must be 'customer', 'driver', or 'manager'.");
                        break;
                    }

                    if (currentRole.equalsIgnoreCase(newRole)) {
                        System.out.println("\nThe user is already assigned the role: " + newRole);
                        break;
                    }

                    // Step 9: Update the user's role
                    String updateRoleSQL = "UPDATE Users SET role = '" + newRole + "' WHERE login = '" + userToChangeRole + "'";
                    esql.executeUpdate(updateRoleSQL);
                    System.out.println("\n✅ User role successfully updated!");
                    break;

                default:
                    System.out.println("\nInvalid choice. Try again.");
            }
        }
    } catch (Exception e) {
        System.out.println("\nError updating user. Please try again.");
    }


}//end PizzaStore
}

