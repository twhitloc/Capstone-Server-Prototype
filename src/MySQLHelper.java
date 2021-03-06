
//STEP 1. Import required packages
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class MySQLHelper {
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost/prototypedb";

	// Database credentials
	static final String USER = "root";
	static final String PASS = "";

	static final String NAME = "com.mysql.jdbc.Driver";

	private static Connection conn = null;
	private static Statement stmt = null;

	/**
	 * 
	 * @param letter
	 */
	static void createSignTable(char letter) {

		Statement stmt = null;
		try {
			conn = startSession();
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
		} finally {
			// finally block used to close resources
			endSession();
		} // end try

	}

	/**
	 * Create the Database Table for Translated Phrases
	 */
	static void createTranslatedPhraseTable() {

		try {
			conn = startSession();
			System.out.println("Creating table in given database...");
			stmt = conn.createStatement();

			String sql = TranslatedPhrase.CREATE_TRANSLATED_PHRASE_TABLE;

			java.sql.DatabaseMetaData meta = conn.getMetaData();
			ResultSet res = meta.getTables(null, null, TranslatedPhrase.TABLE_NAME, null);
			if (res.next() != false) {

			} else {

				stmt.executeUpdate(sql);
				System.out.println("Created table in given database...");
			}
		} catch (SQLException se) {
			se.printStackTrace();
		} finally {
			endSession();
		}

	}

	/**
	 * 
	 * @param name
	 */
	public static void dropSignTable(String name) {

		try {
			conn = startSession();

			// STEP 4: Execute a query
			System.out.println("Deleting table in given database...");
			stmt = conn.createStatement();

			String sql = "DROP TABLE IF EXISTS " + name;

			stmt.executeUpdate(sql);
			System.out.println("Executed query : " + sql + "\n");
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} finally {
			// finally block used to close resources

			endSession();

		} // end try

	}

	public static void endSession() {
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
		System.out.println("Database Closed");
	}

	/**
	 * 
	 * @param table
	 * @return
	 */
	public static ArrayList<Sign> getAllSigns(String table) {
		ArrayList<Sign> signList = new ArrayList<Sign>();

		try {
			conn = startSession();
			// STEP 4: Execute a query
			System.out.println("Creating statement...");
			stmt = conn.createStatement();

			String sql = "SELECT * FROM " + table;
			ResultSet rs = stmt.executeQuery(sql);
			System.out.println("Executed Query " + sql + "\n");
			// STEP 5: Extract data from result set
			while (rs.next()) {
				// Retrieve by column name
				signList.add(getSignFromResultSet(rs));
			}
			rs.close();
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} finally {
			// finally block used to close resources
			endSession();
		} // end try
		return signList;
	}

	/**
	 * 
	 * @param table
	 * @return
	 */
	public static ArrayList<TranslatedPhrase> getAllTranslatedPhrases(String table) {
		ArrayList<TranslatedPhrase> phraseList = new ArrayList<TranslatedPhrase>();

		try {
			conn = startSession();
			// STEP 4: Execute a query
			System.out.println("Creating statement...");
			stmt = conn.createStatement();

			String sql = "SELECT * FROM " + table;
			ResultSet rs = stmt.executeQuery(sql);
			System.out.println("Executed Query " + sql + "\n");
			// STEP 5: Extract data from result set
			while (rs.next()) {
				// Retrieve by column name
				phraseList.add(getTranslatedPhraseFromResultSet(rs));
			}
			rs.close();
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} finally {
			// finally block used to close resources
			endSession();
		} // end try
		return phraseList;
	}

	/**
	 * 
	 * @param rs
	 * @return
	 */
	public static Sign getSignFromResultSet(ResultSet rs) {
		String textVal;
		try {
			textVal = rs.getString(Sign.VALUE_COLUMN).replace("_", "'");
			String vidUrl = rs.getString(Sign.VID_URL_COLUMN).replace("_", "'");
			String pageUrl = rs.getString(Sign.PAGE_URL_COLUMN).replace("_", "'");
			String connotationVal = rs.getString(Sign.CONNOTATION_VALUE_COLUMN).replace("_", "'");
			return new Sign(textVal, vidUrl, pageUrl, connotationVal);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new Sign();
	}

	/**
	 * 
	 * @param sign
	 * @param table
	 * @return
	 */
	public static String getSignSQLInsert(Sign sign, String table) {
		String textVal = "", signUrl = "", vidUrl = "", connotation = "";

		// If the sign contains an apostrophe then it cannot be
		// inserted. Replace during DB Operations
		textVal = sign.getLemmaValue().replace("'", "_");
		signUrl = sign.getPageUrl().replace("'", "_");
		vidUrl = sign.getVideoUrl().replace("'", "_");
		connotation = sign.getConnotation().replace("'", "_");

		String sql = "INSERT INTO " + table + " VALUES ('" + textVal + "', '" + vidUrl + "', '" + signUrl + "', '"
				+ connotation + "')";

		return sql;
	}

	/**
	 * 
	 * @param rs
	 * @return
	 */
	public static TranslatedPhrase getTranslatedPhraseFromResultSet(ResultSet rs) {
		String english, asl;
		try {
			english = rs.getString(TranslatedPhrase.ENGLISH_COLUMN).replace("_", "'");
			asl = rs.getString(TranslatedPhrase.ASL_COLUMN).replace("_", "'");

			return new TranslatedPhrase(english, asl);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new TranslatedPhrase();
	}

	/**
	 * 
	 * @param tp
	 * @param table
	 * @return
	 */
	public static String getTranslatedPhraseSQLInsert(TranslatedPhrase tp, String table) {
		String english = "";
		String asl = "";
		String sql = "";

		english = tp.getEnglishPhrase().replace("'", "_");
		asl = tp.getASLPhrase().replace("'", "_");

		sql = "INSERT INTO " + table + " VALUES ('" + english + "', '" + asl + "')";

		return sql;
	}

	/**
	 * 
	 * @param sign
	 * @param table
	 */
	public static void insertSign(Sign sign, String table) {

		try {
			conn = startSession();
			// STEP 4: Execute a query
			System.out.println("Inserting records into the table...");
			stmt = conn.createStatement();

			String sql = getSignSQLInsert(sign, table);
			stmt.executeUpdate(sql);
			System.out.println("Executed query : " + sql + "\n");

		} catch (SQLException se) {
			se.printStackTrace();
		} finally {
			endSession();
		} // end try
	}

	/**
	 * 
	 * @param signList
	 * @param table
	 */
	public static void insertSignList(ArrayList<Sign> signList, String table) {

		try {
			conn = startSession();

			// STEP 4: Execute a query
			System.out.println("Inserting records into the table...");
			for (Sign sign : signList) {
				try {
					stmt = conn.createStatement();

					String sql = getSignSQLInsert(sign, table);
					stmt.executeUpdate(sql);
					System.out.println("Executed query : " + sql + "\n");
				} catch (SQLException se) {
					se.printStackTrace();
				}
			}
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			endSession();
		} // end try

	}// end main

	/**
	 * 
	 * @param sign
	 * @param table
	 */
	public static void insertTranslatedPhrase(TranslatedPhrase tp) {

		int ch = tp.getEnglishPhrase().length();
		String table = TranslatedPhrase.TABLE_NAME;
		try {
			conn = startSession();
			// STEP 4: Execute a query
			System.out.println("Inserting records into the table...");
			stmt = conn.createStatement();

			String sql = getTranslatedPhraseSQLInsert(tp, table);
			stmt.executeUpdate(sql);
			System.out.println("Executed query : " + sql + "\n");

		} catch (SQLException se) {
			se.printStackTrace();
		} finally {
			endSession();
		} // end try
	}

	/**
	 * 
	 * @return
	 */
	public static Connection startSession() {

		Connection conn = null;
		try {
			Class.forName(NAME);
			// STEP 3: Open a connection
			System.out.println("Connecting to database...");
			try {
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				System.out.println("Connected database successfully...");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // Register Driver

		return conn;
	}

	/**
	 * createInitialDatabase
	 * 
	 * Drops any outdated databases. Creates new database for signs that begin
	 * with 'from' up to 'to'
	 * 
	 * @param from
	 * @param to
	 */
	public void createInitialSignDatabase(char from, char to) {

		WebParser parser = new WebParser();
		for (char ch = from; ch <= to; ch++) {

			MySQLHelper.dropSignTable(ch + "Sign");
			MySQLHelper.createSignTable(ch);

			String listUrl = "https://www.signingsavvy.com/browse/" + ch;
			Elements signElements = parser.getSignElements(listUrl);
			ArrayList<Sign> list = new ArrayList<>();

			// if the list of ELements is not null
			if (signElements != null) {
				// Get the List Items that represent the Signs from the
				// collection of Elements
				for (Node value : signElements.get(0).getElementsByTag("li")) {
					// Add the Signs as they are to the list
					// ***The Signs in this list are not fully initialized yet.
					list.add(parser.getSignFromNode(value));
				}

				// Get the Urls for the Videos
				for (Sign sgn : list) {
					sgn = parser.getVideoUrlFromSignPage(sgn);
				}
			}

			MySQLHelper.insertSignList(list, ch + "Sign");

		}

		return;
	}
}