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
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

public class Translator {

	private List<List<Sign>> signList;
	public ArrayList<Sign> responseList;
	private String inputString;
	private String translatedString;
	private OutputStreamWriter outputStream;
	private Properties props;
	private StanfordCoreNLP pipeline;
	private ArrayList<String> lemmas;

	public Translator(List<List<Sign>> list) {
		if (list != null) {
			signList = list;
		}
		props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		pipeline = new StanfordCoreNLP(props);
	}

	/**
	 * This is for testing purposes
	 */
	public Translator() {
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

		String translatedString = translate(inputString);

		PrintWriter srvout;
		srvout = new PrintWriter(System.out);

		ArrayList<Sign> matchSigns = getSignListFromLemmas(translatedString);

		Annotation annotation;
		annotation = new Annotation(translatedString);
		pipeline.annotate(annotation);
		pipeline.prettyPrint(annotation, srvout);

		responseList = matchSigns;
	}

	/**
	 * 
	 */
	public void getSignedEnglish() {

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

	/**
	 * 
	 */
	public ArrayList<Sign> getSignedEnglish(String input) {

		Annotation annotation;
		/*
		 * input = input.replace(" i ", " me "); if (input.charAt(0) == 'i') {
		 * if (input.charAt(1) == ' ') { input = input.replace("i ", "me "); } }
		 */
		annotation = new Annotation(input);
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
		return responseList;
	}

	public String translate(String input) {
		String response = "";

		Annotation annotation;
		annotation = new Annotation(input);
		pipeline.annotate(annotation);

		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
		if (sentences.size() > 1) {
			// please just try one!
		} else {
			pipeline.prettyPrint(annotation, System.out);
			Tree sentence = sentences.get(0).get(TreeCoreAnnotations.TreeAnnotation.class);
			String sentenceType = SentenceAnalyzer.detectSentenceType(sentence);

			switch (sentenceType) {

			case "complex":
				response = ClauseTransformer.simpleSentenceTransformation(sentence);
				break;
			case "fragment":
				response = ClauseTransformer.simpleSentenceTransformation(sentence);
				break;
			case "simple":
				response = ClauseTransformer.simpleSentenceTransformation(sentence);
				break;
			case "compound":
				response = ClauseTransformer.simpleSentenceTransformation(sentence);
				break;
			case "simpleQuestion":
				response = ClauseTransformer.simpleQuestionTransformation(sentence);
				break;
			default:
				response = ClauseTransformer.simpleSentenceTransformation(sentence);
				break;
			}
		}
		return response;
	}

	public String identifyByClauseInformation(String input) {
		String response = "";
		Annotation annotation;
		annotation = new Annotation(input);
		pipeline.annotate(annotation);

		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);

		pipeline.prettyPrint(annotation, System.out);
		Tree sentence = sentences.get(0).get(TreeCoreAnnotations.TreeAnnotation.class);
		response = SentenceAnalyzer.detectSentenceType(sentence);
		return response;
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
			String signTextLower = sign.getLemmaValue().toLowerCase();
			String signText = sign.getLemmaValue();
			if (signTextLower == strVal) {
				return sign;
			} else if (signText.equals(strVal)) {
				return sign;
			} else if (signTextLower.equals(strVal.toLowerCase())) {
				return sign;
			} else if (signText.toUpperCase().equals(strVal)) {
				return sign;
			} else if (sign.getConnotation() != null) {
				if (sign.getConnotation() == strVal) {
					return sign;
				}
				if (sign.getConnotation().toUpperCase().equals(strVal.toUpperCase())) {
					return sign;
				}
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
			if (!lemma.matches(".*[^A-za-z].*")) {
				if (getSignIfExist(lemma) != null) {
					matchSigns.add(getSignIfExist(lemma));
				}
				// else get the word spelled out
			}
		}
		return matchSigns;
	}

	public ArrayList<Sign> getSignListFromLemmas(String sentence) {
		ArrayList<Sign> matchSigns = new ArrayList<Sign>();
		for (String lemma : sentence.split(" ")) {
			if (!lemma.matches(".*[^A-za-z].*")) {
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
