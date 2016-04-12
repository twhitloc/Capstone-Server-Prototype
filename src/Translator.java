import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.SemanticHeadFinder;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
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
				response = "complex";
				break;
			case "fragment":
				response = "fragment";
				break;
			case "simple":
				response = simpleSentenceTransformation(sentence);
				break;
			case "compound":
				response = "compound";
				break;
			case "simpleQuestion":
				response = simpleQuestionTransformation(sentence);

				break;
			}
		}
		lemmas = getLemmasFromCoreMap(sentences);
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

	private String simpleQuestionTransformation(Tree sentence) {
		// TODO Auto-generated method stub

		String translation = "";
		//
		Boolean isCopulaPhrase = false;
		//
		Boolean isQuestion = false;
		int numSubjects = 0;

		IndexedWord nominalSubjectActor = new IndexedWord();
		IndexedWord nominalSubjectAction = new IndexedWord();
		IndexedWord questionTopic = new IndexedWord();
		IndexedWord questionSubject = new IndexedWord();
		IndexedWord indirectObject = new IndexedWord();
		IndexedWord directObject = new IndexedWord();
		IndexedWord determiner = new IndexedWord();

		Boolean shortQuestion = false;
		if (sentence.getLeaves().toString().contains("?")) {
			isQuestion = true;
		}

		if (sentence.getLeaves().size() < 6 && isQuestion) {
			shortQuestion = true;
		}
		// get the language pack
		TreebankLanguagePack languagePack = new PennTreebankLanguagePack();
		// create a grammatical structure object using language pack
		GrammaticalStructure structure = languagePack.grammaticalStructureFactory().newGrammaticalStructure(sentence);

		Collection<TypedDependency> typedDeps = structure.typedDependenciesCollapsed();
		System.out.println("typedDeps ==>  " + typedDeps);

		for (TypedDependency td : typedDeps) {
			IndexedWord dependent = td.dep();
			IndexedWord governor = td.gov();
			String depStr = dependent.toString();
			String govStr = governor.toString();
			switch (td.reln().toString()) {

			// Nominal Subject
			case "nsubj":

				nominalSubjectActor = dependent;
				nominalSubjectAction = governor;
				break;

			// Sentence Root
			case "root":
				// rootVerb = dependent;
				break;

			// Determiner
			case "det":
				if (dependent.toString().contains("a") || dependent.toString().contains("an")
						|| dependent.toString().contains("the") && sentence.getLeaves().size() < 5) {
				} else {
					determiner = dependent;
				}
				break;

			case "nmod:poss":

				questionSubject = dependent;
				questionTopic = governor;
				break;
			// Indirect Object
			case "iobj":
				indirectObject = dependent;
				break;
			// Direct Object
			case "dobj":
				directObject = dependent;
				break;

			// Copula
			case "cop":
				isCopulaPhrase = true;
				directObject = governor;
				break;
			default:
				System.out.println(td);
				break;
			}
		}

		// if the sentence is a declaration statement * ie : "I am a student"
		if (shortQuestion && questionSubject.size() != 0 && questionTopic.size() != 0) {
			if (questionSubject.index() > questionTopic.index()) {
				translation = questionSubject.lemma().toString() + " " + questionTopic.lemma().toString();
			} else
				translation = questionTopic.lemma().toString() + " " + questionSubject.lemma().toString();

			// If there is an indirect object and direct object
		} else if (nominalSubjectActor.size() != 0 && indirectObject.size() != 0 && directObject.size() != 0
				&& nominalSubjectAction.size() != 0) {

			translation = nominalSubjectActor.lemma().toString() + " " + indirectObject.lemma().toString() + " "
					+ directObject.lemma().toString() + " " + nominalSubjectAction.lemma().toString();

			// if there is a direct object without an indirect object
		} else if (nominalSubjectActor.size() != 0 && directObject.size() != 0 && nominalSubjectAction.size() != 0) {
			translation = nominalSubjectActor.lemma().toString() + " " + nominalSubjectAction.lemma().toString() + " "
					+ directObject.lemma().toString();
		}

		if (isQuestion) {
			translation = translation + " questioning";
		}
		// SemanticGraph collDeps =
		// sentence.get(CollapsedDependenciesAnnotation.class);

		return translation;
	}

	private String simpleSentenceTransformation(Tree tree) {
		String translation = "";
		//
		Boolean isCopulaPhrase = false;
		//
		int numSubjects = 0;
		// Tree subjectTree;
		// Tree governorTree;
		// Tree dependentTree;
		// Tree rootTree;
		// Tree modifierTree;
		// Tree argumentTree;

		IndexedWord nominalSubjectActor = new IndexedWord();
		IndexedWord nominalSubjectAction = new IndexedWord();
		IndexedWord rootVerb = new IndexedWord();
		IndexedWord indirectObject = new IndexedWord();
		IndexedWord directObject = new IndexedWord();
		IndexedWord determiner = new IndexedWord();

		// get the language pack
		TreebankLanguagePack languagePack = new PennTreebankLanguagePack();
		// create a grammatical structure object using language pack
		GrammaticalStructure structure = languagePack.grammaticalStructureFactory().newGrammaticalStructure(tree);
		// find head dependency for the tree
		SemanticHeadFinder headFinder = new SemanticHeadFinder();
		headFinder.determineHead(tree);
		Collection<TypedDependency> typedDeps = structure.typedDependenciesCollapsed();
		System.out.println("typedDeps ==>  " + typedDeps);

		for (TypedDependency td : typedDeps) {
			IndexedWord dependent = td.dep();
			IndexedWord governor = td.gov();
			String depStr = dependent.toString();
			String govStr = governor.toString();
			switch (td.reln().toString()) {

			// Nominal Subject
			case "nsubj":

				nominalSubjectActor = dependent;
				nominalSubjectAction = governor;
				break;

			// Sentence Root
			case "root":
				// rootVerb = dependent;
				break;

			// Determiner
			case "det":
				if (dependent.toString().contains("a") || dependent.toString().contains("an")
						|| dependent.toString().contains("the") && tree.getLeaves().size() < 5) {
					// rootVerb = null;
					// for now just ignore the articles
					// RootVerb may be wrong in this case?
				} else {
					determiner = dependent;
				}
				break;

			// Indirect Object
			case "iobj":
				indirectObject = dependent;
				break;
			// Direct Object
			case "dobj":
				directObject = dependent;
				break;

			// Copula
			case "cop":
				isCopulaPhrase = true;
				directObject = governor;
				break;
			default:
				System.out.println("Sucks to be here");
				break;
			}
		}

		// if the sentence is a declaration statement * ie : "I am a student"
		if (isCopulaPhrase == true && nominalSubjectActor.size() != 0 && directObject.size() != 0) {

			translation = nominalSubjectActor.lemma().toString() + " " + directObject.lemma().toString();

			// If there is an indirect object and direct object
		} else if (nominalSubjectActor.size() != 0 && indirectObject.size() != 0 && directObject.size() != 0
				&& nominalSubjectAction.size() != 0) {

			translation = nominalSubjectActor.lemma().toString() + " " + indirectObject.lemma().toString() + " "
					+ directObject.lemma().toString() + " " + nominalSubjectAction.lemma().toString();

			// if there is a direct object without an indirect object
		} else if (nominalSubjectActor.size() != 0 && directObject.size() != 0 && nominalSubjectAction.size() != 0) {
			translation = nominalSubjectActor.lemma().toString() + " " + nominalSubjectAction.lemma().toString() + " "
					+ directObject.lemma().toString();
		}

		// SemanticGraph collDeps =
		// sentence.get(CollapsedDependenciesAnnotation.class);

		return translation;
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

	public void initiateSession(OutputStreamWriter out, String input) {
		outputStream = out;
		inputString = input;
	}
}
