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
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.SemanticHeadFinder;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
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
			CoreMap sentence = sentences.get(0);
			String sentenceType = detectSentenceType(sentence);

			switch (sentenceType) {

			case "complex":

				break;
			case "fragment":

				break;
			case "simple":
				response = simpleSentenceTransformation(sentence);
				break;
			case "compound":

				break;
			}
		}
		lemmas = getLemmasFromCoreMap(sentences);
		return response;
	}

	private String simpleSentenceTransformation(CoreMap sentence) {
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

		// get the tree that represents the sentence as a whole
		Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
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
				System.out.println(td);
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
						|| dependent.toString().contains("the") && sentence.size() < 5) {
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

		SemanticGraph collDeps = sentence.get(CollapsedDependenciesAnnotation.class);

		/*
		 * Tree prev = tree; int y = 0; // go through the tree one level at a
		 * time for (int i = 0; i < tree.depth(); i++) { Tree temp; temp =
		 * prev.firstChild(); List<Tree> list = prev.getChildrenAsList();
		 * List<Tree> nodei = tree.getLeaves(); switch (temp.nodeString()) {
		 * case "nsubj": System.out.println("Here"); break; case "S":
		 * System.out.println("Here"); numSubjects = list.size(); break; }
		 * String str = temp.nodeString(); Tree[] child = temp.children(); prev
		 * = temp; y++; }
		 */

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

	/**
	 * Use the number of clauses and sentence information to detect what type of
	 * sentence has been input. This affects the routine that will be called to
	 * translate.
	 * 
	 * @param sentence
	 * @return
	 */
	public String detectSentenceType(CoreMap sentence) {

		Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);

		String response = "";
		int numberOfClauses = 0;
		numberOfClauses = getNumberOfClauses(tree);

		if (numberOfClauses < 2) {
			if (isSimple(sentence)) {
				response = "simple";
			}
			if (isSimpleWHQuestion(sentence)) {
				response = "simpleQuestion";
			}
		} else if (numberOfClauses > 2) {
			if (isCompound(sentence)) {

				response = "compound";
			} else if (isComplex(sentence)) {

				response = "complex";
			}
		} else {
			response = "fragment";
		}
		return response;
	}

	private boolean isSimpleWHQuestion(CoreMap sentence) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Returns true if the sentence is a fragment clause
	 * 
	 * @param sentence
	 * @return
	 */
	public Boolean isFragment(CoreMap sentence) {
		// Has no full independent clause
		// May have subject
		return false;
	}

	/**
	 * Returns the number of clauses in the CoreMap object.
	 * 
	 * @param sentence
	 * @return
	 */
	public int getNumberOfClauses(Tree sentence) {
		int numClauses = 0;

		for (Tree tree : sentence) {
			// Identify Clause Level Tags!

			if (tree.label().value().equals("S")) {
				numClauses++;
			} else if (tree.label().value().equals("SBAR")) {
				numClauses++;
			} else if (tree.label().value().equals("SBARQ")) {
				numClauses++;
			} else if (tree.label().value().equals("SINV")) {
				numClauses++;
			}
		}
		return numClauses;
	}

	public Boolean isSimple(Tree tree) {
		// Independent clause and no dependent clause
		// One subject and one predicate
		// Predicate is a verb phrase that consists of >1 word
		// Can have compound predicate consisting of more than one VBP

		// Nominal subject is a noun phrase (NP) which is the subject of the
		// clause
		Boolean hasNominalSubject = false;

		Boolean hasPersonalPronoun = false;

		Boolean hasCopula = false;
		// grammatical root of the sentence
		Boolean hasRoot = false;

		// relation of the head of a noun phrase (NP) and its determiner
		Boolean hasDeterminer = false;

		//
		Boolean hasIndirectObject = false;

		//
		Boolean hasDirectObject = false;

		//
		Boolean hasNounPhrase = false;

		//
		Boolean hasVerbPhrase = false;

		Boolean hasCoordinatingConjunction = false;

		// get the language pack
		TregexPattern pat = TregexPattern.compile("@S");
		TregexMatcher matcher = pat.matcher(tree.firstChild());
		if (matcher.find()) {
			Boolean additionalClauses = matcher.findNextMatchingNode();
			if (additionalClauses)
				return false;
		}

		TreebankLanguagePack languagePack = new PennTreebankLanguagePack();

		// create a grammatical structure object using language pack
		GrammaticalStructure structure = languagePack.grammaticalStructureFactory().newGrammaticalStructure(tree);

		// get typed dependencies
		Collection<TypedDependency> typedDeps = structure.typedDependenciesCollapsed();

		for (TypedDependency td : typedDeps) {

			if (td.reln().toString().equals(EnglishGrammaticalRelations.NOMINAL_SUBJECT.toString())
					|| td.reln().toString().equals(EnglishGrammaticalRelations.NOMINAL_PASSIVE_SUBJECT.toString())) {
				hasNominalSubject = true;
				System.out.println(td);
			}
			// if there is a coordinating conjunction it cannot be a simple
			// sentence
			if (td.reln().toString().equals(EnglishGrammaticalRelations.COORDINATION.toString())) {
				return false;
			}

			if (td.reln().toString().equals(EnglishGrammaticalRelations.COPULA.toString())) {
				hasCopula = true;
			}

			if (td.reln().toString().equals(EnglishGrammaticalRelations.DIRECT_OBJECT.toString())) {
				hasDirectObject = true;
			}

			if (td.reln().toString().contains("root")) {
				hasRoot = true;
			}
		}
		pat = TregexPattern.compile("@NP");
		matcher = pat.matcher(tree);
		if (matcher.find()) {
			hasNounPhrase = true;
		}

		pat = TregexPattern.compile("@VP");
		if (matcher.find()) {
			hasVerbPhrase = true;
		}

		pat = TregexPattern.compile("@PRP");
		if (matcher.find()) {
			hasPersonalPronoun = true;
		}

		pat = TregexPattern.compile("@S");
		if (matcher.find()) {
			hasNominalSubject = true;
		}
		if (hasRoot) {
			if (hasNominalSubject) {
				if (hasNounPhrase) {
					if (hasVerbPhrase) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public Boolean isCompound(CoreMap sentence) {

		return false;
	}

	public Boolean isComplex(CoreMap sentence) {
		// must have independent >1 and dependent clause >=0
		return false;
	}
}

/**
 * Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
 * CollinsHeadFinder headFinder = new CollinsHeadFinder();
 * headFinder.determineHead(tree); SemanticGraph basicDeps =
 * sentence.get(BasicDependenciesAnnotation.class); Collection
 * <TypedDependency> typedDeps = basicDeps.typedDependencies();
 * System.out.println("typedDeps ==>  " + typedDeps);
 * 
 * SemanticGraph collDeps = sentence.get(CollapsedDependenciesAnnotation.class);
 * SemanticGraph collCCDeps =
 * sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
 * 
 * List<CoreMap> numerizedTokens =
 * sentence.get(NumerizedTokensAnnotation.class); List<CoreLabel> tokens =
 * sentence.get(CoreAnnotations.TokensAnnotation.class);
 * 
 * Tree sentenceTree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
 * 
 * // find nounPhrases in sentence TregexPattern pat =
 * TregexPattern.compile("@NP"); TregexMatcher matcher =
 * pat.matcher(sentenceTree); while (matcher.find()) { Tree nounPhraseTree =
 * matcher.getMatch(); System.out.println("Found noun phrase " +
 * nounPhraseTree);
 * 
 * nounPhraseTree.percolateHeads(headFinder); Tree head =
 * nounPhraseTree.headTerminal(headFinder); CoreLabel headLabel = (CoreLabel)
 * head.label();
 * 
 * System.out.println("tokens.contains(headLabel)" +
 * tokens.contains(headLabel));
 * 
 * System.out.println(""); System.out.println("Iterating over typed deps"); for
 * (TypedDependency typedDependency : typedDeps) {
 * System.out.println(typedDependency.gov().backingLabel()); System.out.println(
 * "gov pos " + typedDependency.gov() + " - " + typedDependency.gov().index());
 * System.out.println("dep pos " + typedDependency.dep() + " - " +
 * typedDependency.dep().index());
 * 
 * if (typedDependency.gov().index() == headLabel.index()) {
 * 
 * System.out.println( " !!!!!!!!!!!!!!!!!!!!!  HIT ON " + headLabel + " == " +
 * typedDependency.gov()); } } }
 */