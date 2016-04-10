import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

public class Translator_Test {

	/**
	 *
	 */
	@Test
	public void testTranslator() {

		Translator translator = new Translator();

		String input = "She drives a car";
		String output = translator.translate(input);
		assertEquals("she drive car", output);
	}

	/**
	 * 
	 */
	@Test
	public void testSimpleSentenceDirectAndIndirectObjects() {

		Translator translator = new Translator();

		String input = "I give the teacher apples";
		String output = translator.translate(input);
		assertEquals("i teacher apple give", output.toLowerCase());
	}

	/**
	 * 
	 */
	@Test
	public void testSimpleCopulaTranslation() {

		Translator translator = new Translator();

		String input = "I am a student";
		String output = translator.translate(input);
		assertEquals("i student", output.toLowerCase());

		input = "He is a student";
		output = translator.translate(input);
		assertEquals("he student", output.toLowerCase());

	}

	/**
	 * 
	 */
	@Test
	public void testSimpleWHQuestionTranslation() {

		Translator translator = new Translator();

		String input = "What is your name?";
		String output;// = translator.translate(input);
		// assertEquals("you name questioning", output.toLowerCase());

		input = "Your name is what?";
		output = translator.translate(input);
		assertEquals("name you questioning", output);
	}

	@Test
	public void testSimpleAREQuestionTranslation() {

		Translator translator = new Translator();

		String input = "Are you deaf?";
		String output = translator.translate(input);
		assertEquals("deaf you quesitoning", output.toLowerCase());
	}

	@Test
	public void testIdentifyComplexByClauseInformation() {
		Translator translator = new Translator();

		String input = "Because my coffee was too cold, I heated it in the microwave.";
		String output = translator.identifyByClauseInformation(input);
		assertEquals("complex", output);

		input = "Though he was very rich, he was still very unhappy.";
		output = translator.identifyByClauseInformation(input);
		assertEquals("complex", output);

		input = "She returned the computer after she noticed it was damaged";
		output = translator.identifyByClauseInformation(input);
		assertEquals("complex", output);

		input = "When the cost goes up, customers buy less clothing.";
		output = translator.identifyByClauseInformation(input);
		assertEquals("complex", output);

		input = "As she was bright and ambitious, she became manager in no time.";
		output = translator.identifyByClauseInformation(input);
		assertEquals("complex", output);

		input = "The movie, though very long, was still very enjoyable.";
		output = translator.identifyByClauseInformation(input);
		assertEquals("complex", output);

		input = "The actor was happy he got a part in a movie although the part was a small one.";
		output = translator.identifyByClauseInformation(input);
		assertEquals("complex", output);

		input = "When she was younger, she believed in fairy tales.";
		output = translator.identifyByClauseInformation(input);
		assertEquals("complex", output);
	}

	@Test
	public void testIdentifyCompoundByClauseInformation() {
		Translator translator = new Translator();

		String input = "She did not cheat on the test, for it was not the right thing to do.";
		String output = translator.identifyByClauseInformation(input);
		assertEquals("compound", output);

		input = "I really want to go to work, but I am too sick to drive.";
		output = translator.identifyByClauseInformation(input);
		assertEquals("compound", output);

		input = "There was no ice cream in the freezer, nor did they have money to go to the store.";
		output = translator.identifyByClauseInformation(input);
		assertEquals("compound", output);

		input = "Should we start class now, or should we wait for everyone to get here?";
		output = translator.identifyByClauseInformation(input);
		assertEquals("compound", output);

		input = "He didn’t want to go to the dentist, yet he went anyway."; //
		output = translator.identifyByClauseInformation(input); //
		assertEquals("compound", output);

		input = "I am counting my calories, yet I really want dessert.";
		output = translator.identifyByClauseInformation(input);
		assertEquals("compound", output);

		input = "Malls are great places to shop; I can find everything I need under one roof.";
		output = translator.identifyByClauseInformation(input);
		assertEquals("compound", output);

		input = "The sky is clear; the stars are twinkling.";
		output = translator.identifyByClauseInformation(input);
		assertEquals("compound", output);

		input = "She only paints with bold colors; she does not like pastels.";
		output = translator.identifyByClauseInformation(input);
		assertEquals("compound", output);

	}

	/**
	 * 
	 */
	@Test
	public void testGetSignedEnglish() {
		English_ASL servlet = new English_ASL();
		String input = "I love playing with dogs";
		Translator translator = new Translator(servlet.signList);
		ArrayList<Sign> output = translator.getSignedEnglish(input);

		assertEquals(output.get(0).getLemmaValue().toLowerCase(), "i");
		assertEquals(output.get(1).getLemmaValue().toLowerCase(), "love");
		assertEquals(output.get(2).getLemmaValue().toLowerCase(), "play");
		assertEquals(output.get(3).getLemmaValue().toLowerCase(), "with");
		assertEquals(output.get(4).getLemmaValue().toLowerCase(), "dog");

	}

	@Test
	public void testDetectSentenceType() {
		English_ASL servlet = new English_ASL();
		String input = "This is a simple sentence";
		Translator translator = new Translator(servlet.signList);
		// translator.detectSentenceType(sentence)

	}
}
