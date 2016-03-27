import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class Translator {

	private List<List<Sign>> signList;
	public ArrayList<Sign> responseList;
	private String inputString;
	private String translatedString;
	private OutputStreamWriter outputStream;
	private Properties props;
	private StanfordCoreNLP pipeline;

	public Translator(List<List<Sign>> list) {
		if (list != null) {
			signList = list;
		}
		props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		pipeline = new StanfordCoreNLP(props);
	}

	/**
	 * closeSession
	 * 
	 * Will terminate the current session by clearing session variables.
	 * Necessary to start a new translation session.
	 */
	public void closeSession() {
		responseList = null;
		inputString = null;
		translatedString = null;
		outputStream = null;
	}

	/**
	 * 
	 * @param input
	 * @return
	 */
	public void createTranslation() {

		Annotation annotation;
		annotation = new Annotation(inputString);
		pipeline.annotate(annotation);

		PrintWriter srvout;
		srvout = new PrintWriter(System.out);

		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		ArrayList<String> lemmas = getLemmasFromCoreMap(sentences);
		ArrayList<Sign> matchSigns = getSignListFromLemmas(lemmas);

		pipeline.prettyPrint(annotation, srvout);

		// @TODO this is not correct!
		translatedString = lemmas.toString();

		responseList = matchSigns;
	}

	public ArrayList<String> getLemmasFromCoreMap(List<CoreMap> sentences) {
		int i = 0;
		ArrayList<String> lemmas = new ArrayList<String>();
		for (CoreMap sentence : sentences) {

			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				lemmas.add(token.get(LemmaAnnotation.class));
			}
			i++;
			/**
			 * @TODO: More sentence stuff here!
			 */
		}

		if (i > 1) {
			// problems too many input sentences
		}
		return lemmas;
	}

	/**
	 * getSignIfExist
	 * 
	 * returns the Sign for which the Lemma is the input if there is one
	 * 
	 * @param strVal
	 * @return
	 */
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

	/**
	 * getSignListFromLemmas
	 * 
	 * get a list of Signs that match with Lemma list
	 * 
	 * @param lemmas
	 * @return matchingSignList
	 */
	public ArrayList<Sign> getSignListFromLemmas(List<String> lemmas) {
		ArrayList<Sign> matchSigns = new ArrayList<Sign>();
		for (String lemma : lemmas) {
			if (!lemma.matches(".*[^a-z].*")) {
				if (getSignIfExist(lemma) != null) {
					matchSigns.add(getSignIfExist(lemma));
				}
				// else get the word spelled out
			}
		}
		return matchSigns;
	}

	public void initiateSession(OutputStreamWriter out, String input) {
		outputStream = out;
		inputString = input;

	}
}