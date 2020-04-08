
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;




public class DeIdentify extends HttpServlet {
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DeIdentify() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

		JsonObject reqJson = null;
		JsonObject data = null;
		
		StringBuffer jb = new StringBuffer();
		  String line = null;
		  try {
		    BufferedReader reader = request.getReader();
		    while ((line = reader.readLine()) != null)
		      jb.append(line);
		  } catch (Exception e) { /*report an error*/ }

		System.out.println("doGet");
		System.out.println(jb.toString());
		String dataString = jb.toString();
		

		if (!dataString.isEmpty())
		{
			JsonReader jsonReader = Json.createReader(new StringReader(dataString));
			reqJson = jsonReader.readObject();
			jsonReader.close();
			data = ConvertData(reqJson);
		}
	
		PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.print(data);
        out.flush();   
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	
	private JsonObject ConvertData(JsonObject record)
	{
		JsonObject data = null;

		String age = "";
		String zipCode = "";
		int admissionYear = 0;
		int dischargeYear = 0;
		String notes = ""; 
	
	    String birthDate = record.getString("birthDate");
		zipCode = record.getString("zipCode");
		String admissionDate = record.getString("admissionDate");
		String dischargeDate = record.getString("dischargeDate");
		notes = record.getString("notes");
		
		// convert age
		try {
			LocalDate dateNow = LocalDate.now();
			LocalDate d = LocalDate.parse(birthDate);
			int yearsOld = Period.between(d, dateNow).getYears();
			if (yearsOld > 89) 
					age = "90+"; 
			else 
					age = Integer.toString(yearsOld);

		}
		catch (DateTimeParseException e)
		{
			e.printStackTrace();
		}
		
		try {
			admissionYear = LocalDate.parse(admissionDate).getYear();
		}
		catch (DateTimeParseException e)
		{
			e.printStackTrace();
		}
		try {
			dischargeYear = LocalDate.parse(dischargeDate).getYear();
		}
		catch (DateTimeParseException e)
		{
			e.printStackTrace();
		}
		
		//ZIP code
		zipCode = convertZip(zipCode);

	    // parse notes		
		String[] words = notes.split(" ");
		for (int i = 0; i<words.length; i++)
		{
			// hide email, SS#, Phone #
			if (isEmail(words[i]) || isSocial(words[i]) || isPhone(words[i]))
			{
				words[i] = words[i].replaceAll("[a-zA-Z_0-9]", "X");
			}
			
		}
		
		notes = String.join(" ", words);	
		
		JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
		jsonBuilder
	     .add("age", age)
	     .add("zipCode", zipCode)
	     .add("admissionYear", Integer.toString(admissionYear))
	     .add("dischargeYear", Integer.toString(dischargeYear))
	     .add("notes", notes);

	     data = jsonBuilder.build();
		 
		return data;
		
	}
	
	
	private boolean isEmail(String str) {
	    String pattern = "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
	    Pattern p = Pattern.compile(pattern);
	    Matcher m = p.matcher(str);
	    return m.matches();
	}
	
	private boolean isSocial(String str) {
	    String pattern = "^(?!000|666)[0-8][0-9]{2}-(?!00)[0-9]{2}-(?!0000)[0-9]{4}$";
	    Pattern p = Pattern.compile(pattern);
	    Matcher m = p.matcher(str);
	    return m.matches();
	}
	
	private boolean isPhone(String str) {
		String pattern = "^\\(?([0-9]{3})\\)?[-. ]?([0-9]{3})[-. ]?([0-9]{4})$";
	    Pattern p = Pattern.compile(pattern);
	    Matcher m = p.matcher(str);
	    return m.matches();
	}

	
	private String convertZip(String zipCode)
	{
		String zip = zipCode;
		
        BufferedReader fileReader = null;
        final String delim = ",";
        try
        {
            String line = "";
            String path = getServletContext().getRealPath("/")+"/";

            fileReader = new BufferedReader(new FileReader(path + "WEB-INF/population_by_zcta_2010.csv"));
      
            while ((line = fileReader.readLine()) != null) 
            {
                String[] tokens = line.split(delim);
                if (tokens[0].equals(zipCode))
                {
                	if (Integer.parseInt(tokens[1]) < 20000)
                	{
                		zip = "00000";
                	}
                	else
                	{
                		zip = zipCode.substring(0,3) + "00";
                	}
                }
            }
        } 
        catch (Exception e) {
            e.printStackTrace();
        } 
        finally
        {
            try {
                fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return zip;
	}

}


