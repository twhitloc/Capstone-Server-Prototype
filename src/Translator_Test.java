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

	@Test
	public void testTransformNounPhrase() {
		Translator translator = new Translator();
		// "John is looking for a man to fix the garage";
		String input = "John is looking for a book.";
		String output = translator.translate(input);
		assertEquals("john look for book.", output.toLowerCase());

		input = "John is looking for the book.";
		output = translator.translate(input);
		assertEquals("john look for book.", output.toLowerCase());
	}

	/**
	 * 
	 */
	@Test
	public void testSimpleWHQuestionTranslation() {

		Translator translator = new Translator();

		String input = "What is your name?";
		String output = translator.translate(input);
		assertEquals("you name what questioning", output.toLowerCase());

	}

	@Test
	public void testSimpleAREQuestionTranslation() {

		Translator translator = new Translator();

		String input = "Are you deaf?";
		String output = translator.translate(input);
		assertEquals("deaf you questioning", output.toLowerCase());
	}

	@Test
	public void testIdentifyComplexByClauseInformation() {
		Translator translator = new Translator();

		String input = "Because Mary and Samantha arrived at the bus station before noon, I did not see them at the station.";
		String output = translator.identifyByClauseInformation(input);
		assertEquals("complex", output);

		input = "While he waited at the train station, Joe realized that the train was late.";
		output = translator.identifyByClauseInformation(input);
		assertEquals("complex", output);

		input = "After they left on the bus, Mary and Samantha realized that Joe was waiting at the train station.";
		output = translator.identifyByClauseInformation(input);
		assertEquals("complex", output);

		input = "I did not see them at the station because Mary and Samantha arrived at the bus station before noon.";
		output = translator.identifyByClauseInformation(input);
		assertEquals("complex", output);

		input = "Joe realized that the train was late while he waited at the train station.";
		output = translator.identifyByClauseInformation(input);
		assertEquals("complex", output);

		input = "Mary and Samantha realized that Joe was waiting at the train station after they left on the bus.";
		output = translator.identifyByClauseInformation(input);
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

		String input;
		String output;

		input = "John bought some new shoes and wore them to a party.";
		output = translator.identifyByClauseInformation(input); //
		assertEquals("compound", output);

		input = "Joe waited for the train, but the train was late.";
		output = translator.identifyByClauseInformation(input);
		assertEquals("compound", output);

		input = "Lydia liked her new house but not the front yard.";
		output = translator.identifyByClauseInformation(input);
		assertEquals("compound", output);

		input = "We can go see a movie or get something to eat.";
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
