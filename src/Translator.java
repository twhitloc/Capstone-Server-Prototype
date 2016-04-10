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
			Tree sentence = sentences.get(0).get(TreeCoreAnnotations.TreeAnnotation.class);
			String sentenceType = detectSentenceType(sentence);

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
		response = detectSentenceType(sentence);
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

	/**
	 * Use the number of clauses and sentence information to detect what type of
	 * sentence has been input. This affects the routine that will be called to
	 * translate.
	 * 
	 * @param sentence
	 * @return
	 */
	public String detectSentenceType(Tree tree) {
		String response = "";
		int numberOfClauses = 0;
		numberOfClauses = getNumberOfClauses(tree);

		if (numberOfClauses == 0) {
			response = "fragment";
		} else if (numberOfClauses == 1) {

			if (isSimpleWHQuestion(tree)) {
				response = "simpleQuestion";
			} else if (isSimple(tree)) {
				response = "simple";
			}

		}

		if (numberOfClauses >= 1) {

			Tree subtree = tree;
			boolean hasSubordinating = false;
			boolean hasFrag = false;
			boolean hasCoordinating = false;
			String leaves = tree.getLeaves().toString();

			if (tree.getLeaves().toString().contains(";")) {
				String treeString = tree.getLeaves().toString();
				if (treeString.indexOf(";") != treeString.length() - 2)
					return "compound";
			}

			TregexPattern pat = TregexPattern.compile("@CONJP");
			TregexMatcher matcher = pat.matcher(tree);
			if (matcher.find()) {
				return "compound";
			}

			pat = TregexPattern.compile("@CC");
			matcher = pat.matcher(tree);
			if (matcher.find()) {
				return "compound";
			}

			pat = TregexPattern.compile("@PP");
			matcher = pat.matcher(tree);
			if (matcher.find()) {
				pat = TregexPattern.compile("@IN");
				matcher = pat.matcher(matcher.getMatch());
				if (matcher.find()) {
					hasSubordinating = true;
				}
			}

			pat = TregexPattern.compile("@SBAR");
			matcher = pat.matcher(tree);
			if (matcher.find()) {
				pat = TregexPattern.compile("@IN");
				matcher = pat.matcher(tree);
				if (matcher.find()) {
					return "compound";
				}
			}

			if (numberOfClauses >= 2) {

				// if coordination has not been detected, perhaps it is because
				// "yet" does not always classify as CC
				// Check to see if yet is being used as CC
				hasCoordinating = hasYetCoordination(tree);
				if (hasCoordinating) {
					return "compound";
				}

				int numNP = 0;
				pat = TregexPattern.compile("@NP");
				matcher = pat.matcher(tree);
				if (matcher.find()) {
					numNP++;
					if (matcher.findNextMatchingNode()) {
						numNP++;
					}
				}

				pat = TregexPattern.compile("@FRAG");
				matcher = pat.matcher(tree);
				if (matcher.find()) {
					hasFrag = true;
				}

				if (numNP > 1 || hasFrag) {
					response = "complex";
				}
			}

		}
		return response;

	}

	private boolean hasYetCoordination(Tree tree) {
		TregexPattern pat;
		TregexMatcher matcher;
		Tree subtree;
		ArrayList<Tree> clauses = new ArrayList<Tree>();
		List<Tree> children = tree.firstChild().getChildrenAsList();
		boolean ret = false;

		pat = TregexPattern.compile("@S");
		matcher = pat.matcher(tree);
		if (matcher.find()) {
			subtree = matcher.getMatch();
			matcher = pat.matcher(subtree.firstChild());
			if (!matcher.findNextMatchingNode()) {
				clauses.add(subtree);
			}
			matcher = pat.matcher(tree);
			while (matcher.findNextMatchingNode()) {
				subtree = matcher.getMatch();
				matcher = pat.matcher(subtree.firstChild());
				if (!matcher.find()) {
					clauses.add(subtree);
				}
			}
		}
		if (tree.getLeaves().toString().contains("yet")) {
			boolean emptyCommaTree = false;
			for (Tree child : children) {
				String childLabel = child.label().value().toString();
				if (childLabel.contains(",")) {
					if (child.getChildrenAsList().size() == 1) {
						emptyCommaTree = true;
					}
				} else if (emptyCommaTree) {

					if (child.firstChild().getLeaves().toString().contains("yet")) {
						ret = true;
					}
					emptyCommaTree = false;
				}
			}

		}
		for (Tree clause : clauses) {
			if (clause.getLeaves().toString().contains("yet")) {
				ret = false;
			}
		}
		return ret;
	}

	private boolean isSimpleWHQuestion(Tree tree) {
		// TODO Auto-generated method stub
		Boolean isWHQuestion = false;
		Boolean hasSBARQ = false;
		Boolean hasWHNP = false;
		Boolean isCopular = false;
		// Nominal Subject
		Boolean hasNSUBJ = false;
		Boolean hasRoot = false;
		Boolean isQuestion = false;

		// SQ - Inverted yes/no question, or main clause of a wh-question,
		// following the wh-phrase in SBARQ.
		Boolean hasSQ = false;

		if (tree.getLeaves().toString().contains("?")) {
			isQuestion = true;

		}

		TregexPattern pat = TregexPattern.compile("@SBARQ");
		TregexMatcher matcher = pat.matcher(tree);
		Tree subtree = tree;
		if (matcher.find()) {
			hasSBARQ = true;
			subtree = matcher.getMatch();
		}
		pat = TregexPattern.compile("@SQ");
		matcher = pat.matcher(subtree);
		if (matcher.find()) {
			hasSQ = true;
		}

		pat = TregexPattern.compile("@WHNP");
		matcher = pat.matcher(subtree);
		if (matcher.find()) {
			subtree = matcher.getMatch();
			hasWHNP = true;
		}

		int x = 1;
		x++;

		TreebankLanguagePack languagePack = new PennTreebankLanguagePack();

		// create a grammatical structure object using language pack
		GrammaticalStructure structure = languagePack.grammaticalStructureFactory().newGrammaticalStructure(tree);

		// get typed dependencies
		Collection<TypedDependency> typedDeps = structure.typedDependenciesCollapsed();

		for (TypedDependency td : typedDeps) {

			if (td.reln().toString().equals(EnglishGrammaticalRelations.NOMINAL_SUBJECT.toString())
					|| td.reln().toString().equals(EnglishGrammaticalRelations.NOMINAL_PASSIVE_SUBJECT.toString())) {
				hasNSUBJ = true;

			}
			// if there is a coordinating conjunction it cannot be a simple
			// sentence
			if (td.reln().toString().equals(EnglishGrammaticalRelations.COORDINATION.toString())) {
				return false;
			}

			if (td.reln().toString().equals(EnglishGrammaticalRelations.COPULA.toString())) {
				isCopular = true;
			}

			if (td.reln().toString().equals(EnglishGrammaticalRelations.DIRECT_OBJECT.toString())) {

			}

			if (td.reln().toString().contains("root")) {
				hasRoot = true;
			}
			// ??
			if (td.reln().toString().equals("punct")) {

			}
		}

		if (isQuestion && hasNSUBJ && hasRoot) {
			return true;
		}

		// nested if stuff

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

			if (tree.label().value().equals("S") && !(tree.firstChild().label().value().equals("SBAR"))) {
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
		Boolean hasIndirectObject = false;
		Boolean hasDirectObject = false;
		Boolean hasNounPhrase = false;
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

	public Boolean isCompound(Tree tree) {

		return false;
	}

	public Boolean isComplex(Tree tree) {
		// must have independent >1 and dependent clause >=0
		return false;
	}
}
