
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

		for (char ch = 'a'; ch <= 'z'; ch++)
			signList.add(((int) ch - 'a'), MySQLHelper.getAllSigns(Sign.TABLE_NAME.replace('*', ch)));

		response.getWriter().append("Served at:").append(request.getContextPath()); // set
																					// up
																					// optional
																					// output
																					// files
		PrintWriter out;

		out = new PrintWriter(System.out);

		PrintWriter xmlOut = null;
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");

		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Annotation annotation;
		annotation = new Annotation("How many cups of water do you drink everyday?");
		pipeline.annotate(annotation);

		List<String> lemmas = new LinkedList<String>();
		ArrayList<Sign> matchSigns = new ArrayList<Sign>();

		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				lemmas.add(token.get(LemmaAnnotation.class));
			}
		}

		for (String lemma : lemmas) {
			if (!lemma.matches(".*[^a-z].*")) {
				if (getSignIfExist(lemma) != null) {
					matchSigns.add(getSignIfExist(lemma));
				} // else get the word spelled
			}
		}

		for (Sign sign : matchSigns) {
			out.write(sign.getVideoUrl() + "\n");
		}
		pipeline.prettyPrint(annotation, out);
		annotation = new Annotation("water cups you drink everyday how many?");
		pipeline.annotate(annotation);
		pipeline.prettyPrint(annotation, out);

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

	ArrayList<Sign> getSignsFromPage(char from, char to) {
		ArrayList<Sign> list = new ArrayList<Sign>();
		for (char ch = from; ch <= to; ch++) {

			String signDictionaryBrowseURL = "https://www.signingsavvy.com/browse/" + ch;
			org.jsoup.nodes.Document browseDoc = null;
			Elements browse = null;
			try {
				browseDoc = Jsoup.connect(signDictionaryBrowseURL).get();
				browse = browseDoc.getElementsByClass("search_results");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (browse != null) {
				for (Node value : browse.get(0).getElementsByTag("li")) {
					String signUrl = "";
					String signText = "";
					String connotation = "";
					int startCut, endCut;
					startCut = value.toString().indexOf("href");
					signUrl = value.toString().substring(startCut + 6);
					endCut = signUrl.indexOf("\">");
					signText = signUrl.substring(1);
					if (endCut != -1) {
						int endTag = signUrl.indexOf("</a>");
						signText = signUrl.substring(endCut + 2, endTag);
						signUrl = "https://www.signingsavvy.com/" + signUrl.substring(0, endCut);
					}

					org.jsoup.nodes.Document doc = null;
					Elements div = null;
					try {
						doc = Jsoup.connect(signUrl).get();
						div = doc.getElementsByClass("signing_body");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (div != null) {
						if (div.toString().contains("<source src=")) {
							int indexOfVideo = div.toString().indexOf("<source src=");
							String garble = div.toString().substring(indexOfVideo);
							int indexOfEnd = garble.indexOf(".mp4");
							String videoUrl = "https://www.signingsavvy.com/" + garble.substring(13, indexOfEnd + 4);
							Sign sign = new Sign();
							sign.setLemmaValue(signText);
							sign.setVideoUrl(videoUrl);
							sign.setPageUrl(signUrl);
							sign.setConnotation(connotation);
							list.add(sign);
						}
					}
				}
			}

		}
		return list;
	}

	// Creates a database and loads array for character range input
	void createInitialDatabase(char from, char to) {

		for (char ch = from; ch <= to; ch++) {

			MySQLHelper.dropTable(ch + "Sign");
			MySQLHelper.createTable(ch);

			String signDictionaryBrowseURL = "https://www.signingsavvy.com/browse/" + ch;
			org.jsoup.nodes.Document browseDoc = null;
			Elements browse = null;
			try {
				browseDoc = Jsoup.connect(signDictionaryBrowseURL).get();
				browse = browseDoc.getElementsByClass("search_results");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (browse != null) {
				for (Node value : browse.get(0).getElementsByTag("li")) {
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

						if (connotation_tag - 2 - (endTag + 5) > 0) {
							connotation = signUrl.substring(endTag + 5, connotation_tag - 2).toString()
									.replace("(as in &amp;quot", "").replace("</a>", "").replace("/a>", "")
									.replace(">", "").trim();

						} else
							connotation = "";
						signUrl = "https://www.signingsavvy.com/" + signUrl.substring(0, endCut);

					}

					org.jsoup.nodes.Document doc = null;
					Elements div = null;
					try {
						doc = Jsoup.connect(signUrl).get();
						div = doc.getElementsByClass("signing_body");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (div != null) {
						if (div.toString().contains("<source src=")) {
							int indexOfVideo = div.toString().indexOf("<source src=");
							String garble = div.toString().substring(indexOfVideo);
							int indexOfEnd = garble.indexOf(".mp4");
							String videoUrl = "https://www.signingsavvy.com/" + garble.substring(13, indexOfEnd + 4);
							Sign sign = new Sign();
							sign.setLemmaValue(signText);
							sign.setVideoUrl(videoUrl);
							sign.setPageUrl(signUrl);
							sign.setConnotation(connotation);
							MySQLHelper.insert(sign, ch + "Sign");
						}
					}
				}
			}

		}
		return;
	}

	Sign getSignIfExist(String strVal) {

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
