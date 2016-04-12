import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.util.CoreMap;

public class SentenceAnalyzer {

	private static String[] subordinatingConjunctionWords = { "after", "although", "as", "because", "before", "even",
			"even if", "even though", "if", "lest", "now", "once", "provided", "supposing", "rather", "since", "that",
			"than", "though", "til", "unless", "until", "which", "when", "whenever", "where", "whereas", "whether",
			"who", "whoever", "why", "wherever", "while",

	};

	/**
	 * Use the number of clauses and sentence information to detect what type of
	 * sentence has been input. This affects the routine that will be called to
	 * translate.
	 * 
	 * @param sentence
	 * @return
	 */
	public static String detectSentenceType(Tree tree) {
		String response = "";
		int numberOfClauses = 0;
		boolean commaHasChildPrep = false;
		numberOfClauses = getNumberOfClauses(tree);
		String matchStr;
		ArrayList<Tree> clauseList = new ArrayList<Tree>();
		ArrayList<Tree> completeClauses = new ArrayList<Tree>();
		ArrayList<Tree> coordinatingConjunctions = new ArrayList<Tree>();
		ArrayList<Tree> nounPhrases = new ArrayList<Tree>();
		ArrayList<Tree> verbPhrases = new ArrayList<Tree>();
		Tree x;
		Tree y;
		TregexPattern pat = TregexPattern.compile("@S");
		TregexMatcher matcher = pat.matcher(tree.firstChild());
		TregexPattern subjunctiveAdverb;

		completeClauses = getCompleteClauses(tree.firstChild());
		clauseList = getClauseList(tree.firstChild());

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

			int numNP = 0;
			pat = TregexPattern.compile("@NP");
			matcher = pat.matcher(tree);
			if (matcher.find()) {
				nounPhrases.add(matcher.getMatch());
				while (matcher.findNextMatchingNode()) {
					nounPhrases.add(matcher.getMatch());
				}
			}

			pat = TregexPattern.compile("@VP");
			matcher = pat.matcher(tree);
			if (matcher.find()) {
				verbPhrases.add(matcher.getMatch());
				while (matcher.findNextMatchingNode()) {
					verbPhrases.add(matcher.getMatch());
				}
			}

			boolean hasFrag = false;
			boolean hasCoordinating = false;

			if (tree.getLeaves().toString().contains(";")) {
				String treeString = tree.getLeaves().toString();
				if (treeString.indexOf(";") != treeString.length() - 2)
					return "compound";
			}

			pat = TregexPattern.compile("@CONJP");
			matcher = pat.matcher(tree);
			if (matcher.find()) {
				return "compound";
			}

			pat = TregexPattern.compile("@PP");
			matcher = pat.matcher(tree);
			if (matcher.find()) {
				pat = TregexPattern.compile("@IN");
				x = matcher.getMatch();
				matcher = pat.matcher(x);
				if (matcher.find()) {
					x = matcher.getMatch();

					boolean containedInNP = false;
					boolean containedInVP = false;
					for (Tree nPhrase : nounPhrases) {
						if (nPhrase.contains(x)) {
							containedInNP = true;
						}
					}
					for (Tree vPhrase : verbPhrases) {
						if (vPhrase.contains(x)) {
							containedInNP = true;
						}
					}
					if (!containedInNP && !containedInVP) {
						matchStr = x.firstChild().value().toLowerCase();

						for (String str : subordinatingConjunctionWords) {
							if (matchStr.contains(str)) {
								return "complex";
							}
						}
					}
				}

				subjunctiveAdverb = TregexPattern.compile("@WHADVP");
				matcher = subjunctiveAdverb.matcher(x);
				if (matcher.find()) {
					x = matcher.getMatch();
					if (x.firstChild().value().toString() != "") {
						matchStr = x.firstChild().firstChild().value().toLowerCase();
						for (String str : subordinatingConjunctionWords) {
							if (matchStr.contains(str)) {
								return "complex";
							}
						}
					}
				}
			}

			pat = TregexPattern.compile("@SBAR");
			matcher = pat.matcher(tree);
			if (matcher.find()) {
				// pattern preposition
				x = matcher.getMatch();
				pat = TregexPattern.compile("@IN");
				matcher = pat.matcher(x);
				if (matcher.find()) {
					y = matcher.getMatch();

					boolean containedInNP = false;
					boolean containedInVP = false;
					for (Tree nPhrase : nounPhrases) {
						if (nPhrase.contains(y)) {
							containedInNP = true;
						}
					}
					for (Tree vPhrase : verbPhrases) {
						if (vPhrase.contains(y)) {

							containedInNP = true;
						}
					}
					if (!containedInNP && !containedInVP) {
						matchStr = y.firstChild().value().toLowerCase();

						for (String str : subordinatingConjunctionWords) {
							if (matchStr.contains(str)) {
								return "complex";
							}
						}
					} else if (containedInNP || containedInVP) {
						int j = 0;
						if (x.size() > (j = x.objectIndexOf(y))) {
							matchStr = x.getChild(++j).label().value().toString();
							if (matchStr.contains("S")) {
								return "complex";
							}
						}
					}
				}
				subjunctiveAdverb = TregexPattern.compile("@WHADVP");
				matcher = subjunctiveAdverb.matcher(x);
				if (matcher.find()) {
					x = matcher.getMatch();
					if (x.firstChild().value().toString() != "") {
						matchStr = x.firstChild().firstChild().value().toLowerCase();
						for (String str : subordinatingConjunctionWords) {
							if (matchStr.contains(str)) {
								return "complex";
							}
						}
					}
				}

			}

			pat = TregexPattern.compile("@/,/");
			matcher = pat.matcher(tree);
			if (matcher.find()) {
				boolean commaInClause = false;
				boolean prepInClause = false;
				x = matcher.getMatch();
				int indexX = tree.firstChild().objectIndexOf(x);
				if (indexX < tree.firstChild().size()) {

					y = tree.firstChild().getChild(indexX + 1);
					pat = TregexPattern.compile("@IN");
					matcher = pat.matcher(y);
					if (matcher.find()) {
						for (Tree clause : clauseList) {
							if (clause.contains(x)) {
								commaInClause = true;
							}
							if (clause.contains(y)) {
								prepInClause = true;
							}
						}

						if (y == matcher.getMatch() && (!commaInClause && !prepInClause)) {
							response = "compound";
							commaHasChildPrep = true;
						}
					}
				}
			}

			pat = TregexPattern.compile("@CC");
			matcher = pat.matcher(tree);
			if (matcher.find()) {
				boolean isEmbeddedInClause = false;
				coordinatingConjunctions.add(matcher.getMatch());
				while (matcher.findNextMatchingNode()) {
					coordinatingConjunctions.add(matcher.getMatch());
				}

				int numConjunctions = coordinatingConjunctions.size(), counter = 0, tracker = 0,
						numConjunctionsInClauses = 0;

				for (int i = 0; i < nounPhrases.size(); i++) {
					Tree temp = nounPhrases.get(i);

					for (counter = 0; counter < coordinatingConjunctions.size(); counter++) {
						if (temp.contains(coordinatingConjunctions.get(counter))) {
							numConjunctionsInClauses++;
							coordinatingConjunctions.remove(counter);
						}

					}
				}

				if ((numConjunctions - numConjunctionsInClauses) > 0 && coordinatingConjunctions.size() > 0
						|| commaHasChildPrep) {
					return "compound";
				}
				// see if cases like "and" are used as coordinating conjunctions
				// in compound sentences

			}

			if (numberOfClauses >= 2) {

				// if coordination has not been detected, perhaps it is because
				// "yet" does not always classify as CC
				// Check to see if yet is being used as CC
				hasCoordinating = hasYetCoordination(tree);
				if (hasCoordinating) {
					return "compound";
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
		if (response == "") {
			response = "compound complex";
		}
		return response;

	}

	/**
	 * Returns the number of clauses in the CoreMap object.
	 * 
	 * @param sentence
	 * @return
	 */
	public static int getNumberOfClauses(Tree sentence) {
		int numClauses = 0;
		for (Tree tree : sentence) {
			// Identify Clause Level Tags!

			if (tree.label().value().equals("S") && (!(tree.firstChild().label().value().equals("SBAR")
					|| tree.firstChild().label().value().equals("S")))) {
				numClauses++;

			} else if (tree.label().value().equals("SINV")) {
				numClauses++;
			}
		}
		return numClauses;

	}

	private static boolean hasYetCoordination(Tree tree) {
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

	private static ArrayList<Tree> getClauseList(Tree sentence) {

		ArrayList<Tree> t = new ArrayList<Tree>();
		for (Tree tree : sentence) {
			// Identify Clause Level Tags!

			if (tree.label().value().equals("S") && (!(tree.firstChild().label().value().equals("SBAR")
					|| tree.firstChild().label().value().equals("S")))) {
				t.add(tree);

			} else if (tree.label().value().equals("SINV")) {
				t.add(tree);
			}
		}
		return t;
	}

	private static ArrayList<Tree> getCompleteClauses(Tree sentence) {

		ArrayList<Tree> t = new ArrayList<Tree>();
		for (Tree tree : sentence) {
			// Identify Clause Level Tags!

			if (tree.label().value().equals("S") && (!(tree.firstChild().label().value().equals("SBAR")
					|| tree.firstChild().label().value().equals("S")))) {
				t.add(tree);
			}
		}
		return t;
	}

	private static boolean isSimpleWHQuestion(Tree tree) {
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

	public static Boolean isSimple(Tree tree) {
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
}