import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

public class ClauseTransformer {

	private static String toBeVerbs = "is are were am be been being";

	private static int findIndexedWordNodeNumber(Tree tree, IndexedWord word) {
		String indexedWordValue = word.value();
		int index = 0;
		for (Tree t : tree) {
			if (t.firstChild() != null) {
				if (t.firstChild().value().toString().contains(indexedWordValue)) {
					index = t.nodeNumber(tree);
					break;
				}
			}
		}
		return index;
	}

	private static String manipulateDependencies(Tree sentence, String translation) {

		Boolean isCopulaPhrase = false;
		//
		int numSubjects = 0;

		String words[] = translation.split(" ");

		IndexedWord nominalSubjectActor = new IndexedWord();
		IndexedWord nominalSubjectAction = new IndexedWord();
		IndexedWord rootVerb = new IndexedWord();
		IndexedWord indirectObject = new IndexedWord();
		IndexedWord directObject = new IndexedWord();
		IndexedWord determiner = new IndexedWord();

		TreebankLanguagePack languagePack = new PennTreebankLanguagePack();
		// create a grammatical structure object using language pack
		GrammaticalStructure structure = languagePack.grammaticalStructureFactory().newGrammaticalStructure(sentence);
		// find head dependency for the tree

		Collection<TypedDependency> typedDeps = structure.typedDependenciesCollapsed();
		System.out.println("typedDeps ==>  " + typedDeps);

		for (TypedDependency td : typedDeps) {

			IndexedWord dependent = td.dep();
			IndexedWord governor = td.gov();
			String depStr = dependent.toString();
			String govStr = governor.toString();

			for (String w : words) {

				if (depStr.contains(w)) {
					translation = translation.replace(w, dependent.lemma().toString());
				} else if (govStr.contains(w)) {
					translation = translation.replace(w, governor.lemma().toString());
				}
			}

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
				break;
			}
		}
		return translation;
	}

	private static String removeSpaceBeforePunctuation(String translation) {
		translation = translation.replace(" .", ".");
		translation = translation.replace(" ?", " questioning");
		translation = translation.replace(" !", "!");
		translation = translation.replace(" ,", ",");
		translation = translation.replace(" ;", ";");
		translation = translation.replace(" -", "-");
		translation = translation.replace(" '", "'");
		return translation;
	}

	private static Tree removeToBeVerbs(Tree vP) {

		for (Tree v : vP) {
			if (v.label().value().contains("VB") && toBeVerbs.contains(v.firstChild().value().toLowerCase())) {
				Tree parent = v.parent(vP);
				int index = v.nodeNumber(parent);
				if (index >= 0) {
					parent.remove(v);
					parent.removeChild(index);

				}
			} else if (v.label().value().contains("VBZ") && toBeVerbs.contains(v.firstChild().value().toLowerCase())) {
				Tree parent = v.parent(vP);
				int index = v.nodeNumber(parent);
				if (index >= 0) {
					parent.removeChild(index);
				}
			} else if (v.label().value().contains("VBD") && toBeVerbs.contains(v.firstChild().value().toLowerCase())) {
				Tree parent = v.parent(vP);
				int index = v.nodeNumber(parent);
				if (index >= 0) {
					parent.removeChild(index);

				}
			} else if (v.label().value().contains("VBN") && toBeVerbs.contains(v.firstChild().value().toLowerCase())) {
				Tree parent = v.parent(vP);
				int index = v.nodeNumber(parent);
				if (index >= 0) {
					parent.removeChild(index);

				}
			} else if (v.label().value().contains("VBG") && toBeVerbs.contains(v.firstChild().value().toLowerCase())) {
				Tree parent = v.parent(vP);
				int index = v.nodeNumber(parent);
				if (index >= 0) {
					parent.removeChild(index);

				}
			} else if (v.label().value().contains("VBP") && toBeVerbs.contains(v.firstChild().value().toLowerCase())) {
				Tree parent = v.parent(vP);
				int index = v.nodeNumber(parent);
				if (index >= 0) {
					parent.removeChild(index);
				}
			}
		}
		return vP;

	}

	private static Tree replaceNode(Tree temp, Tree newTree, Tree sentence, TregexPattern pat) {
		// TODO Auto-generated method stub

		TregexMatcher matcher = pat.matcher(sentence);
		Tree parent = temp.parent(sentence);
		int index = parent.objectIndexOf(temp);
		if (index >= 0) {
			parent.setChild(index, temp);
		}
		while (parent != sentence) {
			Tree t = parent;
			parent = t.parent(sentence);
			index = parent.objectIndexOf(t);
			if (index >= 0) {
				parent.setChild(index, t);
			}
			if (parent == sentence) {
				sentence = parent;
			}
		}

		return sentence;
	}

	static String simpleQuestionTransformation(Tree sentence) {

		Tree temp, newTree;
		String translation = "";
		boolean isQuestion = false;

		boolean shortQuestion = false;
		if (sentence.getLeaves().toString().contains("?")) {
			isQuestion = true;
		}

		TregexPattern pat;

		ArrayList<Tree> nounPhrases = SentenceAnalyzer.getPhraseListByTag(sentence, "NP");
		if (nounPhrases.size() != 0) {
			for (int i = nounPhrases.size() - 1; i >= 0; i--) {
				temp = nounPhrases.get(i);
				newTree = transformNounPhrase(temp);
				pat = TregexPattern.compile("@NP");
				sentence = replaceNode(temp, newTree, sentence, pat);
			}
		}

		ArrayList<Tree> sqPhrases = SentenceAnalyzer.getPhraseListByTag(sentence, "SQ");
		if (sqPhrases.size() > 0) {
			// Since SQ can represent inverted Are questions (yes/no answerable)
			// We know that this is technically a VP so transform it accordingly
			for (int i = sqPhrases.size() - 1; i >= 0; i--) {
				temp = sqPhrases.get(i);
				newTree = transformVerbPhrase(temp);
				pat = TregexPattern.compile("@SQ");
				sentence = replaceNode(temp, newTree, sentence, pat);
			}
		}

		ArrayList<Tree> WHNPhrases = SentenceAnalyzer.getPhraseListByTag(sentence, "WHNP");
		if (WHNPhrases.size() > 0) {
			// Since SQ can represent inverted Are questions (yes/no answerable)
			// We know that this is technically a VP so transform it accordingly
			for (int i = WHNPhrases.size() - 1; i >= 0; i--) {
				temp = WHNPhrases.get(i);
				newTree = transformWHNPQuestion(temp, sentence);
				pat = TregexPattern.compile("@WHNP");
				sentence = replaceNode(temp, newTree, sentence, pat);
			}
		}

		ArrayList<Tree> verbPhrases = SentenceAnalyzer.getPhraseListByTag(sentence, "VP");
		if (verbPhrases.size() > 0) {
			// Since SQ can represent inverted Are questions (yes/no answerable)
			// We know that this is technically a VP so transform it accordingly
			for (int i = verbPhrases.size() - 1; i >= 0; i--) {
				temp = verbPhrases.get(i);
				newTree = transformVerbPhrase(temp);
				pat = TregexPattern.compile("@VP");
				sentence = replaceNode(temp, newTree, sentence, pat);

			}
		}

		ArrayList<Tree> SBARPhrases = SentenceAnalyzer.getPhraseListByTag(sentence, "SBAR");
		if (SBARPhrases.size() > 0) {
			// Since SQ can represent inverted Are questions (yes/no answerable)
			// We know that this is technically a VP so transform it accordingly
			for (int i = SBARPhrases.size() - 1; i >= 0; i--) {
				temp = SBARPhrases.get(i);
				newTree = transformVerbPhrase(temp);
				pat = TregexPattern.compile("@SBAR");
				sentence = replaceNode(temp, newTree, sentence, pat);

			}
		}

		ArrayList<Tree> SBARQPhrases = SentenceAnalyzer.getPhraseListByTag(sentence, "SBARQ");
		if (SBARQPhrases.size() > 0) {
			// Since SQ can represent inverted Are questions (yes/no answerable)
			// We know that this is technically a VP so transform it accordingly
			for (int i = SBARQPhrases.size() - 1; i >= 0; i--) {
				temp = SBARQPhrases.get(i);
				newTree = transformSBARQPhrase(temp, sentence);
				pat = TregexPattern.compile("@SBARQ");
				sentence = replaceNode(temp, newTree, sentence, pat);

			}
		}

		List<Tree> list = sentence.getLeaves();
		int i = 0;
		for (Tree t : list) {
			if (!toBeVerbs.contains(t.value().toString().toLowerCase()) || t.value().toUpperCase().equals("I")) {
				if (i < list.size() - 1) {
					translation += t.value() + " ";
				} else {
					translation += t.value();
				}
			}
			i++;
		}
		translation = removeSpaceBeforePunctuation(translation);

		// get the language pack

		// SemanticGraph collDeps =
		// sentence.get(CollapsedDependenciesAnnotation.class);

		return translation.trim();
	}

	static String simpleSentenceTransformation(Tree sentence) {
		String translation = "";
		Tree temp, newTree;
		//
		TregexPattern pat;

		ArrayList<Tree> nounPhrases = SentenceAnalyzer.getPhraseListByTag(sentence, "NP");
		ArrayList<Tree> verbPhrases = new ArrayList<Tree>();
		if (nounPhrases.size() != 0) {
			for (int i = nounPhrases.size() - 1; i >= 0; i--) {
				temp = nounPhrases.get(i);
				newTree = transformNounPhrase(temp);
				pat = TregexPattern.compile("@NP");
				sentence = replaceNode(temp, newTree, sentence, pat);

			}
		}
		verbPhrases = SentenceAnalyzer.getPhraseListByTag(sentence, "VP");
		if (verbPhrases.size() != 0) {
			for (int i = verbPhrases.size() - 1; i >= 0; i--) {
				temp = verbPhrases.get(i);
				newTree = transformVerbPhrase(temp);
				pat = TregexPattern.compile("@VP");
				sentence = replaceNode(temp, newTree, sentence, pat);
			}
		}

		ArrayList<Tree> SBARPhrases = SentenceAnalyzer.getPhraseListByTag(sentence, "SBAR");
		if (SBARPhrases.size() > 0) {
			// Since SQ can represent inverted Are questions (yes/no answerable)
			// We know that this is technically a VP so transform it accordingly
			for (int i = SBARPhrases.size() - 1; i >= 0; i--) {
				temp = SBARPhrases.get(i);
				newTree = transformVerbPhrase(temp);
				pat = TregexPattern.compile("@SBAR");
				sentence = replaceNode(temp, newTree, sentence, pat);
			}
		}

		ArrayList<Tree> SBARQPhrases = SentenceAnalyzer.getPhraseListByTag(sentence, "SBARQ");
		if (SBARQPhrases.size() > 0) {
			// Since SQ can represent inverted Are questions (yes/no answerable)
			// We know that this is technically a VP so transform it accordingly
			for (int i = SBARQPhrases.size() - 1; i >= 0; i--) {
				temp = SBARQPhrases.get(i);
				newTree = transformSBARQPhrase(temp, sentence);
				pat = TregexPattern.compile("@SBARQ");
				sentence = replaceNode(temp, newTree, sentence, pat);
			}
		}

		ArrayList<Tree> sqPhrases = SentenceAnalyzer.getPhraseListByTag(sentence, "SQ");
		if (sqPhrases.size() > 0) {
			// Since SQ can represent inverted Are questions (yes/no answerable)
			// We know that this is technically a VP so transform it accordingly
			for (int i = sqPhrases.size() - 1; i >= 0; i--) {
				temp = sqPhrases.get(i);
				newTree = transformVerbPhrase(temp);
				pat = TregexPattern.compile("@SQ");
				sentence = replaceNode(temp, newTree, sentence, pat);
			}
		}

		List<Tree> list = sentence.getLeaves();
		int i = 0;
		for (Tree t : list) {
			if (!toBeVerbs.contains(t.value().toString().toLowerCase()) || t.value().toUpperCase().equals("I")) {
				if (i++ < list.size() - 1) {
					translation += t.value() + " ";
				} else {
					translation += t.value();
				}
			}
		}
		translation = removeSpaceBeforePunctuation(translation);

		translation = manipulateDependencies(sentence, translation);

		// get the language pack

		// SemanticGraph collDeps =
		// sentence.get(CollapsedDependenciesAnnotation.class);

		return translation.trim();
	}

	private static Tree swapChildSubTrees(Tree target, int indexA, int indexB) {
		int maxIndex = target.children().length;
		Tree a = null, b = null;
		if (indexA < maxIndex && indexB < maxIndex) {
			a = target.getChild(indexA);
			b = target.getChild(indexB);
			target.setChild(indexA, b);
			target.setChild(indexB, a);

		} else {
			Tree parentA = null;
			Tree parentB = null;
			// if the nodes are contained inside the children Trees passed,
			// change the parents of these nodes
			// So, if NP is the parent, that needs to get moved where the child
			// goes
			if (target.children().length == 1 && target.label().toString().contains("ROOT")) {
				target = target.firstChild();
			}

			Tree[] children = target.children();
			a = target.getNodeNumber(indexA);
			b = target.getNodeNumber(indexB);

			for (Tree child : children) {
				if (parentB != null && parentA != null) {
					break;
				}
				if (child.contains(a)) {
					parentA = child;
					indexA = target.objectIndexOf(parentA);
				}
				if (child.contains(b)) {
					parentB = child;
					indexB = target.objectIndexOf(parentB);
				}
			}
			if (parentB != null && parentA != null) {
				target.setChild(indexA, parentB);
				target.setChild(indexB, parentA);
			}

		}
		return target;
	}

	private static Tree swapChildSubTrees(Tree target, int directDep, int indirectObj, int directObj) {
		int maxIndex = target.children().length;
		Tree dep = null, indir = null, dir = null;
		if (directDep < maxIndex && indirectObj < maxIndex && directObj < maxIndex) {
			dep = target.getChild(directDep);
			indir = target.getChild(indirectObj);
			dir = target.getChild(directObj);
			target.setChild(directDep, indir);
			target.setChild(indirectObj, dir);
			target.setChild(directObj, dep);

		} else {
			Tree parentDep = null;
			Tree parentIn = null;
			Tree parentDir = null;
			// if the nodes are contained inside the children Trees passed,
			// change the parents of these nodes
			// So, if NP is the parent, that needs to get moved where the child
			// goes
			if (target.children().length == 1 && target.label().toString().contains("ROOT")) {
				target = target.firstChild();
			}

			Tree[] children = target.children();
			dep = target.getNodeNumber(directDep);
			indir = target.getNodeNumber(indirectObj);
			dir = target.getNodeNumber(directObj);

			for (Tree child : children) {
				if (child.contains(dep)) {
					parentDep = child;
					directDep = target.objectIndexOf(parentDep);
				} else if (child.contains(indir)) {
					parentIn = child;
					indirectObj = target.objectIndexOf(parentIn);
				} else if (child.contains(dir)) {
					parentDir = child;
					directObj = target.objectIndexOf(parentDir);
				}
			}
			if (parentDep != null && parentIn != null && parentDir != null) {
				target.setChild(directDep, parentIn);
				target.setChild(indirectObj, parentDir);
				target.setChild(directObj, parentDep);
			}

		}
		return target;
	}

	public static Tree transformNounPhrase(Tree nP) {

		if (nP.size() != 1) {
			// get the language pack
			TreebankLanguagePack languagePack = new PennTreebankLanguagePack();
			// create a grammatical structure object using language pack
			GrammaticalStructure structure = languagePack.grammaticalStructureFactory().newGrammaticalStructure(nP);

			Collection<TypedDependency> typedDeps = structure.typedDependenciesCollapsed();
			System.out.println("typedDeps ==>  " + typedDeps);

			for (Tree temp : nP) {

				TregexPattern pat = TregexPattern.compile("@DT");
				TregexMatcher matcher = pat.matcher(temp);

				if (matcher.find()) {
					int index = nP.objectIndexOf(matcher.getMatch());
					if (index >= 0) {
						nP.removeChild(index);
					}
				}

			}
			// SemanticGraph collDeps =
			// sentence.get(CollapsedDependenciesAnnotation.class);
		}

		return nP;

	}

	public static Tree transformSBARQPhrase(Tree sbqP, Tree sentence) {
		if (sbqP.size() > 0) {
			TreebankLanguagePack languagePack = new PennTreebankLanguagePack();
			// create a grammatical structure object using language pack
			GrammaticalStructure structure = languagePack.grammaticalStructureFactory()
					.newGrammaticalStructure(sentence);

			Collection<TypedDependency> typedDeps = structure.typedDependenciesCollapsed();
			System.out.println("typedDeps ==>  " + typedDeps);
			Tree[] children = sbqP.children();
			IndexedWord questWord = null;

			TregexPattern pat = TregexPattern.compile("@WP");
			TregexMatcher matcher = pat.matcher(sentence);
			if (matcher.find()) {
				Tree whnp = matcher.getMatch();
				int indexWH = whnp.nodeNumber(sentence);
				int indexSQ = sbqP.nodeNumber(sentence);
				if (indexSQ > indexWH) {
					sentence = swapChildSubTrees(sentence, indexSQ, indexWH);
				}
			}
		}
		return sentence;
	}

	public static Tree transformVerbPhrase(Tree vP) {

		if (vP.size() != 1) {

			Tree firstChild = vP.firstChild();
			IndexedWord determiner = new IndexedWord();
			IndexedWord nSubj = null;
			IndexedWord inObjDep = null;
			IndexedWord inObjGov = null;
			IndexedWord dirObjDep = null;
			IndexedWord dirObjGov = null;
			IndexedWord advClauseModDep = null;
			IndexedWord advClauseModGov = null;
			IndexedWord possModGov = null;
			IndexedWord possModDep = null;
			IndexedWord questMod = null;
			ArrayList<Tree> nP = SentenceAnalyzer.getPhraseListByTag(vP, "NP");

			// get the language pack
			TreebankLanguagePack languagePack = new PennTreebankLanguagePack();
			// create a grammatical structure object using language pack
			GrammaticalStructure structure = languagePack.grammaticalStructureFactory().newGrammaticalStructure(vP);

			Collection<TypedDependency> typedDeps = structure.typedDependenciesCollapsed();
			System.out.println("typedDeps ==>  " + typedDeps);
			Tree[] children = vP.children();
			IndexedWord phraseRoot = null;

			for (TypedDependency td : typedDeps) {
				String depStr = td.dep().toString();
				String govStr = td.gov().toString();
				switch (td.reln().toString()) {
				// Nominal Subject
				case "nsubj":
					nSubj = td.dep();
					break;
				// Sentence Root
				case "root":
					phraseRoot = td.dep();
					break;
				// Determiner
				case "det":
					break;
				case "punct":
					questMod = td.gov();
					break;
				// Indirect Object
				case "iobj":
					inObjDep = td.dep();
					inObjGov = td.gov();
					break;
				// Direct Object
				case "nmod:poss":
					possModGov = td.gov();
					possModDep = td.dep();
				case "dobj":
					dirObjDep = td.dep();
					dirObjGov = td.gov();
					break;
				// Copula
				case "cop":
					break;
				case "advcl":
					advClauseModDep = td.dep();
					advClauseModGov = td.gov();
					break;
				default:
					break;
				}
			}

			// vP = removeToBeVerbs(vP);
			String pRootVal = phraseRoot.value().toString();

			if (phraseRoot != null) {
				if (firstChild.getLeaves().toString().contains(pRootVal)) {

					if (dirObjDep != null && inObjDep != null) {
						int indexOfDirectObj = findIndexedWordNodeNumber(vP, dirObjDep);

						int indexOfIndirectObj = findIndexedWordNodeNumber(vP, inObjDep);

						int temp = 0;
						int indexOfDirectDependency = findIndexedWordNodeNumber(vP, dirObjGov);
						int indexOfIndirectDependency = findIndexedWordNodeNumber(vP, inObjGov);

						if (indexOfDirectDependency == indexOfIndirectDependency) {
							vP = swapChildSubTrees(vP, indexOfDirectDependency, indexOfIndirectObj, indexOfDirectObj);

						}

					} else if (advClauseModGov != null && nSubj != null) {
						if (advClauseModGov.value().contains(phraseRoot.value())) {

							int indexOfNSubj = findIndexedWordNodeNumber(vP, nSubj);

							int indexOfAdvDep = findIndexedWordNodeNumber(vP, advClauseModDep);
							// rearrange so that subject comes after topic
							vP = swapChildSubTrees(vP, indexOfNSubj, indexOfAdvDep);

						}
					} else if (possModGov != null && nSubj != null) {
						// int indexOfPossModGov = findIndexedWordNodeNumber(vP,
						// possModGov);
						// int indexOfNSubj = findIndexedWordNodeNumber(vP,
						// nSubj);
						// int indexOfQuestMod = findIndexedWordNodeNumber(vP,
						// nSubj);

					}
					// the root of the varb phrase is a verb and needs to be
					// moved.
				} else {

				}
			}
			// SemanticGraph collDeps =
			// sentence.get(CollapsedDependenciesAnnotation.class);
		}

		return vP;

	}

	public static Tree transformWHNPQuestion(Tree wP, Tree sentence) {

		TreebankLanguagePack languagePack = new PennTreebankLanguagePack();
		// create a grammatical structure object using language pack
		GrammaticalStructure structure = languagePack.grammaticalStructureFactory().newGrammaticalStructure(sentence);

		Collection<TypedDependency> typedDeps = structure.typedDependenciesCollapsed();
		System.out.println("typedDeps ==>  " + typedDeps);
		Tree[] children = wP.children();
		IndexedWord questWord = null;

		TregexPattern pat = TregexPattern.compile("@SQ");
		TregexMatcher matcher = pat.matcher(sentence);
		if (matcher.find()) {
			Tree sq = matcher.getMatch();
			int indexSQ = sq.nodeNumber(sentence);
			int indexWH = wP.firstChild().nodeNumber(sentence);
			sentence = swapChildSubTrees(sentence, indexSQ, indexWH);
		}
		return sentence;
	}
}