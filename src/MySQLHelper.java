
//STEP 1. Import required packages
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class MySQLHelper {
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost/prototypedb";

	// Database credentials
	static final String USER = "root";
	static final String PASS = "";

	public static void createTable(char letter) {
		Connection conn = null;
		Statement stmt = null;
		try {
			// STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			// STEP 3: Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			System.out.println("Connected database successfully...");

			// STEP 4: Execute a query
			System.out.println("Creating table in given database...");
			stmt = conn.createStatement();

			String sql = Sign.CREATE_SIGN_TABLE;
			sql = sql.replace("*", letter + "");
			stmt.executeUpdate(sql);
			System.out.println("Created table in given database...");
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					conn.close();
			} catch (SQLException se) {
			} // do nothing
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try
		System.out.println("Goodbye!");
	}

	public static void dropTable(String name) {
		Connection conn = null;
		Statement stmt = null;
		try {
			// STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			// STEP 3: Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			System.out.println("Connected database successfully...");

			// STEP 4: Execute a query
			System.out.println("Deleting table in given database...");
			stmt = conn.createStatement();

			String sql = "DROP TABLE IF EXISTS " + name;

			stmt.executeUpdate(sql);
			System.out.println("Executed query : " + sql + "\n");
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					conn.close();
			} catch (SQLException se) {
			} // do nothing
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try

	}

	public static void insert(ArrayList<Sign> signList, String table) {
		Connection conn = null;
		Statement stmt = null;
		try {
			// STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			// STEP 3: Open a connection
			System.out.println("Connecting to a selected database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			System.out.println("Connected database successfully...");

			// STEP 4: Execute a query
			System.out.println("Inserting records into the table...");
			for (Sign sign : signList) {
				try {
					stmt = conn.createStatement();

					String textVal = "", signUrl = "", vidUrl = "", connotation = "";

					// If the sign contains an apostrophe then it cannot be
					// inserted. Replace during DB Operations
					textVal = sign.getLemmaValue().replace("'", "_");
					signUrl = sign.getPageUrl().replace("'", "_");
					vidUrl = sign.getVideoUrl().replace("'", "_");
					connotation = sign.getConnotation().replace("'", "_");

					String sql = "INSERT INTO" + " " + table + " " + "VALUES ('" + textVal.replace("'", "_") + "', '"
							+ vidUrl + "', '" + signUrl + "', '" + connotation + "')";
					stmt.executeUpdate(sql);
					System.out.println("Executed query : " + sql + "\n");
				} catch (SQLException se) {
					se.printStackTrace();
				}
			}

		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					conn.close();
			} catch (SQLException se) {
			} // do nothing
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try

	}// end main

	public static void insert(Sign sign, String table) {
		Connection conn = null;
		Statement stmt = null;
		try {
			// STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			// STEP 3: Open a connection
			System.out.println("Connecting to a selected database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			System.out.println("Connected database successfully...");

			// STEP 4: Execute a query
			System.out.println("Inserting records into the table...");
			stmt = conn.createStatement();

			String textVal = "", signUrl = "", vidUrl = "", connotation = "";

			// If the sign contains an apostrophe then it cannot be inserted.
			// Replace during DB Operations
			textVal = sign.getLemmaValue().replace("'", "_");
			signUrl = sign.getPageUrl().replace("'", "_");
			vidUrl = sign.getVideoUrl().replace("'", "_");
			connotation = sign.getConnotation().replace("'", "_");

			String sql = "INSERT INTO" + " " + table + " " + "VALUES ('" + textVal.replace("'", "_") + "', '" + vidUrl
					+ "', '" + signUrl + "', '" + connotation + "')";
			stmt.executeUpdate(sql);
			System.out.println("Executed query : " + sql + "\n");

		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					conn.close();
			} catch (SQLException se) {
			} // do nothing
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try
	}

	public static ArrayList<Sign> getAllSigns(String table) {
		ArrayList<Sign> signList = new ArrayList<Sign>();
		Connection conn = null;
		Statement stmt = null;
		try {
			// STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			// STEP 3: Open a connection
			System.out.println("Connecting to a selected database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			System.out.println("Connected database successfully...");

			// STEP 4: Execute a query
			System.out.println("Creating statement...");
			stmt = conn.createStatement();

			String sql = "SELECT * FROM " + table;
			ResultSet rs = stmt.executeQuery(sql);
			System.out.println("Executed Query " + sql + "\n");
			// STEP 5: Extract data from result set
			while (rs.next()) {
				// Retrieve by column name
				// Replace any "_" with the correct "'"
				String textVal = rs.getString(Sign.VALUE_COLUMN).replace("_", "'");
				String vidUrl = rs.getString(Sign.VID_URL_COLUMN).replace("_", "'");
				String pageUrl = rs.getString(Sign.PAGE_URL_COLUMN).replace("_", "'");
				String connotationVal = rs.getString(Sign.CONNOTATION_VALUE_COLUMN).replace("_", "'");

				signList.add(new Sign(textVal, vidUrl, pageUrl, connotationVal));

			}
			rs.close();
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					conn.close();
			} catch (SQLException se) {
			} // do nothing
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try
		return signList;
	}
}