
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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
	List<List<Sign>> signList = new ArrayList<List<Sign>>(26);
	//
	private Translator translator;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public English_ASL() {
		super();
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

		translator.initiateSession(responseStream, input);
		// TODO Auto-generated method stub

		translator.createTranslation();
		ArrayList<Sign> responseList = translator.responseList;
		translator.closeSession();
		writeSignsToResponseStream(responseList, responseStream);
		responseStream.flush();
		responseStream.close();
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
	 * writeSignsToResponseStream
	 * 
	 * Writes the signs in the list to the response stream so that the client
	 * can parse the results
	 * 
	 * @param list
	 * @param out
	 */
	public void writeSignsToResponseStream(ArrayList<Sign> list, OutputStreamWriter out) {
		PrintWriter srvout;
		srvout = new PrintWriter(System.out);
		for (Sign sign : list) {
			srvout.write(sign.getVideoUrl());
			try {
				out.write(sign.getLemmaValue() + " ");
				out.write(sign.getVideoUrl() + "|");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
