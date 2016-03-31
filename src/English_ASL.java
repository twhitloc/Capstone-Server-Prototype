
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class English_ASL
 */
@WebServlet("/English_ASL")
public class English_ASL extends HttpServlet {

	//
	private static final long serialVersionUID = 1L;
	//
	List<List<Sign>> signList;
	//
	ArrayList<Sign> responseList;
	//
	ArrayList<TranslatedPhrase> savedPhrases;
	private Translator translator;
	private int serverAction = 0;
	//

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public English_ASL() {
		super();

		signList = new ArrayList<List<Sign>>(26);
		responseList = new ArrayList<Sign>();
		savedPhrases = MySQLHelper.getAllTranslatedPhrases(TranslatedPhrase.TABLE_NAME);
		for (char ch = 'a'; ch <= 'z'; ch++)
			signList.add(ch - 'a', MySQLHelper.getAllSigns(Sign.TABLE_NAME.replace('*', ch)));
		// TODO Auto-generated constructor stub

		translator = new Translator(signList);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// TODO Auto-generated method stub
		// createInitialDatabase('a', 'z');

		// TODO Auto-generated method stub

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setStatus(HttpServletResponse.SC_OK);
		OutputStreamWriter responseStream = new OutputStreamWriter(response.getOutputStream());
		String input = getRequestInformation(request);

		input = getUserRequest(input);

		switch (serverAction) {

		// 1: Save Phrase
		case 1:
			input = input.substring(1, input.length() - 1);

			int beginIndex = 0;
			int endIndex = input.indexOf(" & ");

			String english = input.substring(beginIndex, endIndex);

			beginIndex = endIndex + 3;
			endIndex = input.length();

			String asl = input.substring(beginIndex);

			TranslatedPhrase tp = new TranslatedPhrase(english, asl);

			// Only creates if it does not exist
			MySQLHelper.createTranslatedPhraseTable();

			MySQLHelper.insertTranslatedPhrase(tp);

			// initiate a translator session
			translator.initiateSession(responseStream, asl);

			// This preserves the word order the input comes in
			translator.getSignedEnglish();

			// get the list of signs that represent that translation
			responseList = translator.responseList;

			// clear the session for the next request
			translator.closeSession();

			// write the results to the stream
			writeSignsToResponseStream(responseList, responseStream);

			break;
		// 2: Get Random Sign Information
		case 2:

			switch (Integer.parseInt(input.substring(0, 2))) {
			case 1:
				// initiate a translator session
				input = input.substring(2, input.length() - 1);
				translator.initiateSession(responseStream, input);

				// create a translation
				translator.createTranslation();

				// get the list of signs that represent that translation
				responseList = translator.responseList;

				// clear the session for the next request
				translator.closeSession();

				// write the results to the stream
				writeSignsToResponseStream(responseList, responseStream);

				break;
			case 2:

				// getRandomSign()
				// get the list of signs that represent that translation
				responseList = translator.responseList;

				// clear the session for the next request
				translator.closeSession();

				// write the results to the stream
				writeSignsToResponseStream(responseList, responseStream);

				break;
			}
			// if RANDOM then random img
			// if SIGN = then not
			// 1 single/list
			// 2 random
			break;
		// 3: Translate Phrase
		case 3:

			// initiate a translator session
			translator.initiateSession(responseStream, input);

			// create a translation
			translator.createTranslation();

			// get the list of signs that represent that translation
			responseList = translator.responseList;

			// clear the session for the next request
			translator.closeSession();

			// write the results to the stream
			writeSignsToResponseStream(responseList, responseStream);

			break;

		case 4:
			ArrayList<TranslatedPhrase> phrases = new ArrayList<TranslatedPhrase>();
			int currentNum = Integer.parseInt(input.substring(0, 2));
			if (currentNum + 10 < savedPhrases.size()) {
				for (int i = currentNum; i < currentNum + 10; i++) {
					phrases.add(savedPhrases.get(i));
				}
			} else
				for (int i = currentNum; i < savedPhrases.size(); i++) {
					phrases.add(savedPhrases.get(i));
				}
			writeTranslatedPhrasesToResponseStream(phrases, responseStream);
			break;
		}

		responseStream.flush();
		responseStream.close();
		responseList.clear();
	}

	/**
	 * getRequestInformation
	 * 
	 * Returns the string contained in the Request Stream
	 * 
	 * @param HttpServletRequest
	 *            request
	 * @return String requestArguments
	 */
	public String getRequestInformation(HttpServletRequest request) {
		byte[] inputBytes = new byte[request.getContentLength()];
		ServletInputStream inputStream;
		try {
			int c, count = 0;
			inputStream = request.getInputStream();
			while ((c = inputStream.read(inputBytes, count, inputBytes.length - count)) != -1) {
				count += c;
			}
			inputStream.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new String(inputBytes);
	}

	/**
	 * Determine which action the server should take based on what the value of
	 * the first index of the input String is.
	 * 
	 * 1: Save Phrase 2: Get Sign Information 3: Translate Phrase
	 * 
	 * @param input
	 * @return action-trimmed Input
	 */
	public String getUserRequest(String input) {
		String action = input.substring(0, 2);
		switch (Integer.parseInt(action)) {
		case 1:
			serverAction = 1;
			break;
		case 2:
			serverAction = 2;
			break;
		case 3:
			serverAction = 3;
			break;
		case 4:
			serverAction = 4;
			break;

		}
		return input.substring(2, input.length());
	}

	/**
	 * writeSignsToResponseStream
	 * 
	 * Writes the signs in the list to the response stream so that the client
	 * can parse the results
	 * 
	 * @param list
	 * @param out
	 */
	public void writeSignsToResponseStream(ArrayList<Sign> list, OutputStreamWriter out) {

		for (Sign sign : list) {

			try {
				out.write(sign.getLemmaValue() + " ");
				out.write(sign.getVideoUrl() + "|");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void writeTranslatedPhrasesToResponseStream(ArrayList<TranslatedPhrase> list, OutputStreamWriter out) {

		for (TranslatedPhrase tp : list) {
			try {
				out.write("[");
				out.write(tp.getEnglishPhrase() + " | ");
				out.write(tp.getASLPhrase() + "]");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
