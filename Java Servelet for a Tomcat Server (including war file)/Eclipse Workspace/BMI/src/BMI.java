import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * BMI Servlet class... this will take the data from the form and calculate and
 * display the data in a separate HTML page. It will be able to handle multiple 
 * sessions as it will store data based on the session id
 * 
 * @author Jamie Barresi
 * @version November 20, 2012
 */
public class BMI extends HttpServlet {
	private static final long serialVersionUID = 1L;
	HttpSession currentSession;
	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		int weight;
		int height;
		String weightUnit = new String();
		String heightUnit = new String();
		boolean details = false;
		boolean wantMail = false;
		String email = new String();
		String name = new String();
		double bmi;
		String sessionData = new String();
		
		// get the session id
		currentSession = request.getSession();
		
		// check to see if new session. If yes create top of table,
		// if no load session data
		if (currentSession.isNew()){
			sessionData = "<tr><td><b>Name</b</td><td><b>BMI Value</b></td></tr>\n";
		} else {
			sessionData = (String) currentSession.getAttribute("dataStore");
		}

		// Set the HTTP content type in the response header
		response.setContentType("text/html; charset=\"UTF-8\"");
		
		// Get the response writer
		PrintWriter serveletOut = response.getWriter();

		// Get data from request
		weight = Integer.parseInt(request.getParameter("weight"));
		height = Integer.parseInt(request.getParameter("height"));
		weightUnit = request.getParameter("weightUnit");
		heightUnit = request.getParameter("heightUnit");
		name = request.getParameter("name");
		
		// make sure we don't get null (it's checked)
		if (request.getParameter("details") != null) {
			details = true;
		}
		
		// make sure we don't get null (it's checked)
		if (request.getParameter("wantMail") != null) {
			wantMail = true;
			email = request.getParameter("email");
		}

		// calculateBMI
		bmi = calculateBMI(weight, height, weightUnit, heightUnit);
		
		
		// Add the result to the session data after bringing it to 2 decimal points
		DecimalFormat twoDPoints = new DecimalFormat("#.##");
		sessionData = sessionData + "<tr><td>" + name + "</td><td>" + Double.valueOf(twoDPoints.format(bmi)) + "</td></tr>\n";
		

		// formulateResponse and pass to the response
		serveletOut.println(formulateResponse(sessionData, details, bmi));
		serveletOut.close();
		
		// store session data
		currentSession.setAttribute("dataStore", sessionData);
	}

	private double calculateBMI(int weight, int height, String weightUnit,
			String heightUnit) {
		double result;
		double calcWeight;
		double calcHeight;

		// check units and convert if necessary
		if (weightUnit.equals("pounds")) {
			calcWeight = poundsToKilograms(weight);
		} else {
			calcWeight = weight;
		}

		if (heightUnit.equals("inches")) {
			calcHeight = inchesToCentimeters(height);
		} else {
			calcHeight = height;
		}

		// Convert height to meters
		calcHeight = calcHeight / 100;

		// Calculate result
		result = calcWeight / (calcHeight * calcHeight);

		return result;
	}

	/**
	 * Method to convert inches to centimeters (convenience)
	 * 
	 * @param inches
	 * @return value in centimeters (of type double)
	 */
	private double inchesToCentimeters(int inches) {
		// constant of centimeters per inch
		final double CM_PER_INCH = 2.54;
		// calculate and return
		return inches * CM_PER_INCH;
	}

	/**
	 * Method to convert pounds to kilograms (convenience)
	 * 
	 * @param pounds
	 * @return value in kilograms (of type double)
	 */
	private double poundsToKilograms(int pounds) {
		// constant of pounds per kilogram
		final double POUNDS_PER_KG = 2.2;
		// calculate and return
		return pounds / POUNDS_PER_KG;
	}
	
	/**
	 * Method to formulate the body of the HTTP response
	 * @param outputWriter
	 * @param dataTable
	 */
	private String formulateResponse(String dataTable, boolean details, double result){
		String responseData = new String();
		String detailString = new String();
		DecimalFormat twoDPoints = new DecimalFormat("#.##");
		
		if (details){
			if (result < 18.5){
				detailString = "Your BMI is " +  Double.valueOf(twoDPoints.format(result)) +  ". This suggests you are underweight.";
			} else if (result >= 18.5 && result < 25) {
				detailString = "Your BMI is " +  Double.valueOf(twoDPoints.format(result)) +  ". This suggests you have a reasonable weight.";
			} else if (result >= 25 && result < 29){
				detailString = "Your BMI is " +  Double.valueOf(twoDPoints.format(result)) +  ". This suggests you are overweight.";
			} else if (result >= 29){
				detailString = "Your BMI is " +  Double.valueOf(twoDPoints.format(result)) +  ". This suggests you may be obese.";
			}
		}
		// Formulate the response body
		if (details){
			responseData = "<!DOCTYPE html \n" +
				       " 	PUBLIC\"-//W3C//DTD XHTML 1.0 STRICT//EN\" \n" +
				       "	\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"> \n" +
	                   "<html xmlns=http'http://www.w3.org/1999/html'> \n" +
	                   "<html> \n" +
	                   "  <head> \n" +
	                   "   <title>BMI Application</title> \n" +
	                   "  </head> \n" +
	                   "  <body>" +
	                   "        <script>alert(\""+ detailString + "\")</script> \n" +
	                   "        <h1>Result Table (Names and BMI Values)</h1>" +
	                   "        <p>The most recent entry is posted at the bottom of the table</p>" +
	                   "        <p><table style=\"text-align:center;\" border=\"1\">" + dataTable + "</table></p>" +
	                   "        <p>Click <a href=\"/BMI/bmi.html\">here</a> to enter another set of information</p>" +
	                   "  </body> \n" +
	                   "</html> \n";
		} else {
			responseData = "<!DOCTYPE html \n" +
				       " 	PUBLIC\"-//W3C//DTD XHTML 1.0 STRICT//EN\" \n" +
				       "	\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"> \n" +
	                   "<html xmlns=http'http://www.w3.org/1999/html'> \n" +
	                   "<html> \n" +
	                   "  <head> \n" +
	                   "   <title>BMI Application</title> \n" +
	                   "  </head> \n" +
	                   "  <body><h1>Result Table (Names and BMI Values)</h1>" +
	                   "        <p>The most recent entry is posted at the bottom of the table</p>" +
	                   "        <p><table style=\"text-align:center;\" border=\"1\">" + dataTable + "</table></p>" +
	                   "        <p>Click <a href=\"/BMI/bmi.html\">here</a> to enter another set of information</p>" +
	                   "  </body> \n" +
	                   "</html> \n";
		}
		
		return responseData;
	}
	
	
}
