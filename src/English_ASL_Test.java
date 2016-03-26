import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class English_ASL_Test {

	@Test
	public void testCleanConnotationValue() throws Exception {
		String test = "(as in &amp;quot </a> this test string";
		English_ASL server = new English_ASL();// Mockito.mock(English_ASL.class);
		String ret = server.cleanConnotationValue(test);

		// Mockito.verify(server).cleanConnotationValue(test);
		assertEquals("this test string", ret);

	}

	@Test
	public void testGetSignIfExist() throws Exception {

		String testValue = "dog";
		English_ASL server = new English_ASL();
		ArrayList<Sign> list = MySQLHelper.getAllSigns("dSign");

		Sign ret = server.getSignIfExist(testValue);
		int i = 0;
		assertEquals("dog", ret.getLemmaValue());

	}

	@Test
	public void testDoPostTranslation() {
		List<List<Sign>> signList = new ArrayList<List<Sign>>(26);
		List<String> strList = new ArrayList<String>();
		for (char ch = 'a'; ch <= 'z'; ch++)
			signList.add(ch - 'a', MySQLHelper.getAllSigns(Sign.TABLE_NAME.replace('*', ch)));

		strList.add("this");
		strList.add("be");
		strList.add("a");
		strList.add("sample");
		strList.add("text");
		ArrayList<Sign> translation = new ArrayList<>();

		String input = "This is a sample text";
		OutputStreamWriter out = new OutputStreamWriter(null);
		PrintWriter srvout;
		srvout = new PrintWriter(System.out);
		translation = English_ASL.getTranslation(input, out, srvout);

		for (int i = 0; i < translation.size(); i++) {
			assertNotSame(translation.get(i).getLemmaValue(), strList.get(i));
		}
	}
}
