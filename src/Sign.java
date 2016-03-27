
/*Tyler Whitlock
 * English-ASL Translation Agent
 * 
 * 
 * 
 * 
 * 
 */

import java.io.Serializable;

/**
 * 
 * @author tyler
 *
 */
public class Sign implements Serializable {

	//
	public static final String TABLE_NAME = "*Sign";
	//
	public static final String VALUE_COLUMN = "textValue";
	//
	public static final String VID_URL_COLUMN = "videoUrl";
	//
	public static final String PAGE_URL_COLUMN = "pageUrl";
	//
	public static final String CONNOTATION_VALUE_COLUMN = "connotationValue";

	//
	private String lemmaValue;
	//
	private String videoUrl;
	//
	private String pageUrl;
	//
	private String connotationValue;

	//
	public static final String[] TABLE_COLUMNS = { VALUE_COLUMN, VID_URL_COLUMN, PAGE_URL_COLUMN,
			CONNOTATION_VALUE_COLUMN };

	//
	public static final String CREATE_SIGN_TABLE = "CREATE TABLE " + Sign.TABLE_NAME + " (" + Sign.VALUE_COLUMN
			+ " varchar(255) not NULL, " + Sign.VID_URL_COLUMN + " varchar(255) not NULL, " + Sign.PAGE_URL_COLUMN
			+ " varchar(255) not NULL, " + Sign.CONNOTATION_VALUE_COLUMN + " varchar(255), "
			+ "CONSTRAINT signkey PRIMARY KEY (" + Sign.VALUE_COLUMN + ", " + Sign.CONNOTATION_VALUE_COLUMN + "))";

	/**
	 * 
	 */
	Sign() {
		lemmaValue = "";
		videoUrl = "";
		pageUrl = "";
		connotationValue = "";
	}

	/**
	 * 
	 * @param txt
	 * @param vid
	 * @param url
	 * @param connotation
	 */
	Sign(String txt, String vid, String url, String connotation) {
		lemmaValue = txt;
		videoUrl = vid;
		pageUrl = url;
		connotationValue = connotation;
	}

	/**
	 * 
	 * @param txt
	 */
	Sign(String txt) {
		lemmaValue = txt;
	}

	/**
	 * 
	 * @return
	 */
	String getConnotation() {
		return connotationValue;
	}

	/**
	 * 
	 * @return
	 */
	String getLemmaValue() {
		return lemmaValue;
	}

	/**
	 * 
	 * @return
	 */
	String getVideoUrl() {
		return videoUrl;
	}

	/**
	 * 
	 * @return
	 */
	String getPageUrl() {
		return pageUrl;
	}

	/**
	 * 
	 * @param conn
	 */
	void setConnotation(String conn) {
		connotationValue = conn;
	}

	/**
	 * 
	 * @param txt
	 */
	void setLemmaValue(String txt) {
		lemmaValue = txt;
	}

	/**
	 * 
	 * @param url
	 */
	void setVideoUrl(String url) {
		videoUrl = url;
	}

	/**
	 * 
	 * @param url
	 */
	void setPageUrl(String url) {
		pageUrl = url;
	}

}