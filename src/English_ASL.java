
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 * Servlet implementation class English_ASL
 */
@WebServlet("/English_ASL")
public class English_ASL extends HttpServlet {
	private static final long serialVersionUID = 1L;
	List<List<Sign>> signList = new ArrayList<List<Sign>>(26);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public English_ASL() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// TODO Auto-generated method stub
		createInitialDatabase('a', 'z');

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int i = 0;
		for (char ch = 'a'; ch <= 'z'; ch++)
			signList.add(i++, MySQLHelper.getAllSigns(Sign.TABLE_NAME.replace('*', ch)));

		// TODO Auto-generated method stub
		try {
			int length = request.getContentLength();
			byte[] input = new byte[length];
			ServletInputStream sin = request.getInputStream();
			int c, count = 0;
			while ((c = sin.read(input, count, input.length - count)) != -1) {
				count += c;
			}

			sin.close();
			String recievedString = new String(input);
			response.setStatus(HttpServletResponse.SC_OK);
			OutputStreamWriter out = new OutputStreamWriter(response.getOutputStream());

			// PrintWriter xmlOut = null;
			Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");

			StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
			Annotation annotation;
			annotation = new Annotation(recievedString);
			pipeline.annotate(annotation);

			PrintWriter srvout;
			srvout = new PrintWriter(System.out);
			ArrayList<Sign> matchSigns = new ArrayList<Sign>();
			List<String> lemmas = new LinkedList<String>();

			List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);

			for (CoreMap sentence : sentences) {
				for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
					lemmas.add(token.get(LemmaAnnotation.class));
				}
				/**
				 * @TODO: More sentence stuff here!
				 */
			}

			for (String lemma : lemmas) {
				if (!lemma.matches(".*[^a-z].*")) {
					if (getSignIfExist(lemma) != null) {
						matchSigns.add(getSignIfExist(lemma));
					}
					// else get the word spelled out
				}
			}

			for (Sign sign : matchSigns) {
				srvout.write(sign.getVideoUrl());
				out.write(sign.getLemmaValue() + " ");
				out.write(sign.getVideoUrl() + "|");
				i = 0;
			}

			out.flush();
			out.close();
			pipeline.prettyPrint(annotation, srvout);

		} catch (IOException e) {

			try {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().print(e.getMessage());
				response.getWriter().close();
			} catch (IOException ioe) {
			}
		}
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
	List<List<Sign>> getSignsFromPage(char from, char to) {
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

	// Creates a database and loads array for character range input
	public void createInitialDatabase(char from, char to) {

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

		}

		return;
	}

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
				connotation = cleanConnotationValue(connotation);

			} else
				connotation = "";
			signUrl = "https://www.signingsavvy.com/" + signUrl.substring(0, endCut);

		}

		newSign.setConnotation(connotation);
		newSign.setPageUrl(signUrl);
		newSign.setLemmaValue(signText);
		return newSign;
	}

	/**
	 * cleanConnotationValue
	 * 
	 * Cleans the html and special characters out of the Connotation value of
	 * the Sign
	 * 
	 * @param connotation
	 * @return
	 */
	public String cleanConnotationValue(String connotation) {

		// To make sure there is no html or invalid characters in the
		// connotation field replace them
		connotation.replace("(as in &amp;quot", "");
		connotation.replace("</a>", "");
		connotation.replace("/a>", "");
		connotation.replace(">", "").trim();
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

	public Sign getSignIfExist(String strVal) {

		char firstCh = strVal.toLowerCase().charAt(0);
		int firstChAsInt = ((int) firstCh - 'a');
		List<Sign> selectedList = signList.get(firstChAsInt);

		for (Sign sign : selectedList) {
			String signText = sign.getLemmaValue().toLowerCase();
			if (signText == strVal) {
				return sign;
			}
			if (signText.equals(strVal)) {
				return sign;
			}
		}
		return null;
	}

}
