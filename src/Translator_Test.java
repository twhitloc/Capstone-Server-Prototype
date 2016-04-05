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
	public void testSimpleQuestionTranslation() {

		Translator translator = new Translator();

		String input = "What is your name?";
		String output = translator.translate(input);
		assertEquals("name you?", output.toLowerCase());

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