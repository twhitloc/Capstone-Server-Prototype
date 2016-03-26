import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class WebParser {

	/**
	 * cleanConnotationValue
	 * 
	 * Cleans the html and special characters out of the Connotation value of
	 * the Sign
	 * 
	 * @param connotation
	 * @return sanitizedConnotation
	 */
	public String cleanConnotationValue(String connotation) {

		CharSequence target = "(as in &amp;quot";
		CharSequence replacement = "";
		// To make sure there is no html or invalid characters in the
		// connotation field replace them
		connotation = connotation.replace(target, replacement);

		target = "</a>";
		connotation = connotation.replace(target, replacement);

		target = "/a>";
		connotation = connotation.replace(target, replacement);

		target = ">";
		connotation = connotation.replace(target, replacement).trim();
		return connotation;
	}

	/**
	 * getSignElements
	 * 
	 * Gets the Elements from the html page that contains the Signs in a list.
	 * 
	 * @param browsePageUrl
	 * @return
	 */
	public Elements getSignElements(String browsePageUrl) {
		org.jsoup.nodes.Document resultPage = null;
		Elements browse = null;
		try {
			resultPage = Jsoup.connect(browsePageUrl).get();
			browse = resultPage.getElementsByClass("search_results");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return browse;
	}

	/**
	 * Returns a Sign that contains PageUrl, Connotation and Lemma Values from a
	 * given Node
	 * 
	 * @param value
	 * @return
	 */
	public Sign getSignFromNode(Node value) {
		Sign newSign = new Sign();
		String signUrl = "";
		String connotation = "";
		String signText = "";

		int startCut, endCut;

		startCut = value.toString().indexOf("href");
		signUrl = value.toString().substring(startCut + 6);
		endCut = signUrl.indexOf("\">");
		signText = signUrl.substring(1);

		if (endCut != -1) {
			int endTag = signUrl.indexOf("</a>");
			signText = signUrl.substring(endCut + 2, endTag);
			int connotation_tag = signUrl.indexOf("</li>");

			// @TODO: See about the need for the replace statements
			if (connotation_tag - 2 - (endTag + 5) > 0) {
				connotation = signUrl.substring(endTag + 5, connotation_tag - 2).toString();

				// Sanitize the connotation value

			} else {
				connotation = "";
			}
			signUrl = "https://www.signingsavvy.com/" + signUrl.substring(0, endCut);

		}
		// Sanitize the connotation value
		connotation = cleanConnotationValue(connotation);
		newSign.setConnotation(connotation);

		newSign.setPageUrl(signUrl);
		newSign.setLemmaValue(signText);
		return newSign;
	}

	/**
	 * getSignsFromPage
	 * 
	 * Load all Signs from the web page after parsing data.
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public List<List<Sign>> getSignsFromPage(char from, char to) {
		// Creates a database and loads array for character range input
		List<List<Sign>> signList = new ArrayList<List<Sign>>();
		for (char ch = from; ch <= to; ch++) {

			MySQLHelper.dropTable(ch + "Sign");
			MySQLHelper.createTable(ch);

			String listUrl = "https://www.signingsavvy.com/browse/" + ch;
			Elements signElements = getSignElements(listUrl);
			ArrayList<Sign> list = new ArrayList<>();

			// if the list of ELements is not null
			if (signElements != null) {
				// Get the List Items that represent the Signs from the
				// collection of Elements
				for (Node value : signElements.get(0).getElementsByTag("li")) {
					// Add the Signs as they are to the list
					// ***The Signs in this list are not fully initialized yet.
					list.add(getSignFromNode(value));
				}

				// Get the Urls for the Videos
				for (Sign sgn : list) {
					sgn = getVideoUrlFromSignPage(sgn);
				}
			}

			MySQLHelper.insert(list, ch + "Sign");
			signList.add(list);
		}

		return signList;
	}

	/**
	 * 
	 * @param sgn
	 * @return
	 */
	public Sign getVideoUrlFromSignPage(Sign sgn) {
		org.jsoup.nodes.Document doc = null;
		Elements div = null;
		try {
			doc = Jsoup.connect(sgn.getPageUrl()).get();
			div = doc.getElementsByClass("signing_body");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (div != null) {
			if (div.toString().contains("<source src=")) {
				int indexOfVideo = div.toString().indexOf("<source src=");
				String div_substring = div.toString().substring(indexOfVideo);
				int indexOfEnd = div_substring.indexOf(".mp4");
				String videoUrl = "https://www.signingsavvy.com/" + div_substring.substring(13, indexOfEnd + 4);
				sgn.setVideoUrl(videoUrl);
			}
		}
		return sgn;
	}
}