package webapi;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;


/**
 * Represents an abstract data source of a Web API, can be later extended to fit a particular API,
 * for example the KRS system or any other JSON based Web API
 * @author Patryk Wegrzyn
 */
public abstract class AbstractDataSource {

	/**
	 * Represents the maximum number of data records a single query to the data source can handle
	 */
	protected int maxPeriodPerQuery;
	/**
	 * Used to save the beginning of the URL of the Web API, since it will be the same for every query
	 */
	protected String urlStart;
	/**
	 * Analogous to the urlStart
	 */
	protected String urlEnd;
	
	/**
	 * A shared method, used by all Data Sources, it can receive the inner part of a URL to a specific data source
	 * and automatically fetch from the server the data and then process it with a JSON parser to eventually
	 * return a single JsonObject object
	 * @param innerURL The inner most part of the url to a web API data source
	 * @return the JsonObject object that represents the contents of the URL
	 * @throws WebApiException thrown when no data has been found for this query, or the query was invalid (wrong format or exceeded limit)
	 * @throws MalformedURLException thrown when an invalid URL was encountered
	 * @throws IOException thrown when an IO error was encountered during the process of fetching data from the server
	 */
	public JsonObject getJsonObjFromURL(String innerURL) throws WebApiException, MalformedURLException, IOException {
		String urlNew = this.urlStart + innerURL + this.urlEnd;
		URL urlObj = new URL(urlNew);
		HttpURLConnection connection = (HttpURLConnection)urlObj.openConnection();
		connection.setRequestMethod("GET");
		connection.connect();
		int code = connection.getResponseCode();
		if(code == 400 || code == 404)
			throw new WebApiException(connection.getResponseMessage());
		InputStream stream = new URL(urlNew).openStream();
		JsonReader reader = Json.createReader(stream);
		JsonObject object = reader.readObject();
		reader.close();
		stream.close();
		return object;
	}

	/**
	 * A very similar method to the described above getJsonObjFromURL method, only this one is able to fetch an JsonArray
	 * and not a JsonObject, so one must know which element is desired
	 * @param innerURL The inner most part of the url to a web API data source
	 * @return the JsonArray object that represents the contents of the URL
	 * @throws WebApiException thrown when no data has been found for this query, or the query was invalid (wrong format or exceeded limit)
	 * @throws MalformedURLException thrown when an invalid URL was encountered
	 * @throws IOException thrown when an IO error was encountered during the process of fetching data from the server
	 */
	public JsonArray getJsonArrFromURL(String innerURL) throws WebApiException, MalformedURLException, IOException {
		String urlNew = this.urlStart + innerURL + this.urlEnd;
		URL urlObj = new URL(urlNew);
		HttpURLConnection connection = (HttpURLConnection)urlObj.openConnection();
		connection.setRequestMethod("GET");
		connection.connect();
		int code = connection.getResponseCode();
		if(code == 400 || code == 404)
			throw new WebApiException(connection.getResponseMessage());
		InputStream stream = new URL(urlNew).openStream();
		JsonReader reader = Json.createReader(stream);
		JsonArray array = reader.readArray();
		reader.close();
		stream.close();
		return array;
	}
	
	/**
	 * Helper method for all data source family of objects, is able to count the amount of units of time
	 * in between to particular dates provided in a right format
	 * @param date1 a String representing the date which is the beginning of the period, format: yyyy-MM-dd
	 * @param date2 a String representing the date which is the end of the period, format: yyyy-MM-dd
	 * @param timeUnit unit of time in which the result will be returned
	 * @return the duration of the provided period in timeUnit units
	 */
	public long getDateDiff(String date1, String date2, TimeUnit timeUnit) {
		Date start, end;
		long diffInMillies = 0;
		try {
			start = new SimpleDateFormat("yyyy-MM-dd").parse(date1);
			end = new SimpleDateFormat("yyyy-MM-dd").parse(date2);
			diffInMillies = end.getTime() - start.getTime();
		} catch (ParseException e) {
			System.err.println("Date formatting error!");
			e.printStackTrace();
		}
	    return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Helper method for all data source object, very similar in functionality to the getDateDiff method, only
	 * this on always returns the full amount of days in the provided period
	 * @param date1 a String representing the date which is the beginning of the period, format: yyyy-MM-dd
	 * @param date2 a String representing the date which is the end of the period, format: yyyy-MM-dd
	 * @return the duration of the provided period in full days
	 */
	public int getDateDiffDays(String date1, String date2) {
		LocalDate ld1 = LocalDate.parse(date1, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		LocalDate ld2 = LocalDate.parse(date2, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		long res = ChronoUnit.DAYS.between(ld1, ld2);
		if (res > 0) return ((int) res);
		else if (res < 0) return ((int) res);
		else return 0;
	}
	
	/**
	 * Yet another helper function for all data sources, this one is able to receive a String representing a date Start
	 * and a integer N representing the number of days and returns the Date which is equal to the date N-days after the Start date
	 * @param inDate a String representing the initial date, format yyyy-MM-dd
	 * @param days number of days to add
	 * @return the resulting date after adding days to inDate in Date format
	 */
	public Date addDays(String inDate, int days) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date date;
		Calendar cal = Calendar.getInstance();
		try {
			date = format.parse(inDate);
			cal.setTime(date);
			cal.add(Calendar.DATE, days);
		} catch (ParseException e) {
			System.err.println("Date formatting error!");
			e.printStackTrace();
		}
		return cal.getTime();
	}
	
	/**
	 * Very similar in functionality to addDays, only this one returns the date in a String, not a Date object,
	 * kind of a wrapper method
	 * @param inDate a String representing the initial date, format yyyy-MM-dd
	 * @param days number of days to add
	 * @return the resulting date after adding days to inDate in String format
	 */
	public String addDaysStr(String inDate, int days) {
		Date result = addDays(inDate, days);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		return formatter.format(result);
	}
	
	/**
	 * Retrieves the urlStart field value
	 * @return value of the urlStart field
	 */
	public String getUrlStart() {
		return urlStart;
	}

	/**
	 * Sets the urlStart field value to a particular value
	 * @param url new value for the urlStart field
	 */
	public void setUrlStart(String url) {
		this.urlStart = url;
	}

	/**
	 * Retrieves the value of the maxPeriodPerQuery field
	 * @return the current value of the maxPeriodPerQuery field
	 */
	public int getMaxPeriodPerQuery() {
		return maxPeriodPerQuery;
	}
	
	/**
	 * Sets the maxPeriodPerQuery field to a particular value
	 * @param newMax new value of the maxPeriodPerQuery field
	 */
	public void setMaxPeriodPerQuery(int newMax) {
		this.maxPeriodPerQuery = newMax;
	}
	/**
	 * Retrieves the urlEnd field value
	 * @return value of the urlEnd field
	 */
	public String getUrlEnd() {
		return urlEnd;
	}
	/**
	 * Sets the urlEnd field value to a particular value
	 * @param urlEnd the urlEnd to set
	 */
	public void setUrlEnd(String urlEnd) {
		this.urlEnd = urlEnd;
	}

}
