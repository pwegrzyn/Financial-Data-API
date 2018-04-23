package webapi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.json.JsonArray;
import javax.json.JsonObject;

/**
 * Prints the week based histogram of the change of price of a given currency in a given period of time
 * @author Patryk Wegrzyn
 */
public class WeeklyBarGraphOrder implements IOrderExecutable, Observer {

	/**
	 * The NBP Web API data source
	 */
	private NBPDataSource dataSource;
	/**
	 * The start of the time period in question
	 */
	private String start;
	/**
	 * The end of the time period in question
	 */
	private String end;
	/**
	 * Same as start only in LocalDate format
	 */
	private LocalDate startLD;
	/**
	 * Same as end only in LocalDate format
	 */
	private LocalDate endLD;
	/**
	 * The currency in question
	 */
	private String currency;
	/**
	 * Flag set when the passed arguments have an invalid format
	 */
	private boolean wrongArgFormat = false;
	/**
	 * Duration of the time period in days
	 */
	private int dayDifference;

	/**
	 * Provides basic constructor functionality
	 * @param parameters User input CLI parameters
	 * @param dataSource The NBP Web API data source
	 * @param orderPerformer The subject to be observed
	 */
	public WeeklyBarGraphOrder(String[] parameters, NBPDataSource dataSource, Subject orderPerformer) {
		if(parameters.length != 3 || !parameters[0].matches("[\\w]{3}") || !parameters[1].matches("[\\d]{4},\\d\\d,\\d") || !parameters[2].matches("[\\d]{4},\\d\\d,\\d"))
			this.wrongArgFormat = true;
		this.currency = parameters[0];
		this.dataSource = dataSource;
		if (!wrongArgFormat) {
			this.startLD = LocalDate.parse(parameters[1] + ",1", DateTimeFormatter.ofPattern("yyyy,MM,W,e"));
			this.start = this.startLD.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			this.endLD = LocalDate.parse(parameters[2] + ",5", DateTimeFormatter.ofPattern("yyyy,MM,W,e"));
			this.end = this.endLD.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			this.dayDifference = ((int) dataSource.getDateDiff(this.start, this.end, TimeUnit.DAYS)) + 1;
		}
		orderPerformer.register(this);
	}

	/* (non-Javadoc)
	 * @see webapi.IOrderExecutable#execute()
	 */
	public void execute() throws MalformedURLException, IOException, WebApiException {
		System.out.println("Print a week-based ASCII graph presenting the relative change of value of a given currency during a given period...");
		if(wrongArgFormat) {
			System.out.println("Error! The format of passed arguments is invalid");
			return;
		}
		
		double values[] = new double[this.dayDifference];
		
		if(this.dayDifference < this.dataSource.getMaxPeriodPerQuery()) {
			try {
				values = fetchPricesForSinglePeriod(this.start, this.end, this.currency);
				printHistogram(values, this.start, this.end, this.currency);
			} catch (WebApiException e) {
				System.out.println("The histogram for the period from " + this.start + " to " + this.end + " could not be created: " + e.getMessage());
			} catch(ArrayIndexOutOfBoundsException | javax.json.stream.JsonParsingException e) {
				System.out.println("The histogram for the period from " + this.start + " to " + this.end + " could not be created: an unidentified JavaScript source has been found during the parsing process");
			}
		} else {
			
			String originalStart = this.start;
			String originalEnd = this.end;
			double temp[] = new double[dataSource.getMaxPeriodPerQuery()];
			int dayIter = 0;
			try {
				
				while((int) dataSource.getDateDiff(this.start, originalEnd, TimeUnit.DAYS) >= dataSource.getMaxPeriodPerQuery()) {
					Date endDate = this.dataSource.addDays(this.start, this.dataSource.getMaxPeriodPerQuery() - 1);
					Date endDateNext = this.dataSource.addDays(this.start, this.dataSource.getMaxPeriodPerQuery());
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
					this.end = formatter.format(endDate);
					String nextEnd = formatter.format(endDateNext);
					try {
						temp = fetchPricesForSinglePeriod(this.start, this.end, this.currency);
						for(int i=0; i<temp.length; dayIter++, i++) {
							values[dayIter] = temp[i];
						}
					} catch (WebApiException e) {
						if(!e.getMessage().equals("Not Found - Brak danych"))
							throw new WebApiException(e.getMessage());
						else {
							for(int i=0; i<temp.length; dayIter++, i++) {
								values[dayIter] = -1;
							}
						}
					}
					this.start = nextEnd;
				}
				double []temp2 = new double[((int) dataSource.getDateDiff(this.start, originalEnd, TimeUnit.DAYS)) + 1];
				try {
					temp2 = fetchPricesForSinglePeriod(this.start, originalEnd, this.currency);
					for(int i=0; i<temp2.length; dayIter++, i++) {
						values[dayIter] = temp2[i];
					}
				} catch (WebApiException e) {
					if(!e.getMessage().equals("Not Found - Brak danych"))
						throw new WebApiException(e.getMessage());
					else {
						for(int i=0; i<temp2.length; dayIter++, i++) {
							values[dayIter] = -1;
						}
					}
				}
				if(values[0] < 0 && values[1] < 0 && values[2] < 0 && values[3] < 0)
					throw new WebApiException("Not Found - Brak danych");
				
				printHistogram(values, originalStart, originalEnd, this.currency);
				
			} catch(WebApiException e) {
				System.out.println("The histogram for the period from " + originalStart + " to " + originalEnd + " could not be created: " + e.getMessage());
			} catch(ArrayIndexOutOfBoundsException | javax.json.stream.JsonParsingException e) {
				System.out.println("The histogram for the period from " + originalStart + " to " + originalEnd + " could not be created: an unidentified JavaScript source has been found during the parsing process");
			}
		}
	}
	
	/**
	 * Helper method, basically provides the same functionality as the main execute() methods, only this one works for a single period of time
	 * @param start The start of the time period, format yyyy-MM-dd
	 * @param end The end of the time period, format yyyy-MM-dd
	 * @param currency The currency in question code
	 * @return The found tables of prices in the given period, -1 when to data has been found for a date
	 * @throws WebApiException thrown when no data has been found for this query, or the query was invalid (wrong format or exceeded limit)
	 * @throws MalformedURLException thrown when an invalid URL was encountered
	 * @throws IOException thrown when an IO error was encountered during the process of fetching data from the server
	 */
	private double[] fetchPricesForSinglePeriod(String start, String end, String currency) throws WebApiException, MalformedURLException, IOException {
		
		int dayDifferenceLocal = ((int) dataSource.getDateDiff(start, end, TimeUnit.DAYS)) + 1;
		double values[] = new double[dayDifferenceLocal];
		JsonObject object;
		JsonArray array;
		String dateIter;
		int dayIter = 0;
		int JsonArrIter = 0;
		
		object = this.dataSource.getJsonObjFromURL("exchangerates/rates/A/" + currency + "/" + start + "/" + end);
		array = object.getJsonArray("rates");
		int temp = dataSource.getDateDiffDays(start, array.getJsonObject(JsonArrIter).getString("effectiveDate"));
		for(; dayIter < temp; dayIter++) {
			values[dayIter] = -1;
		}
		dateIter =  array.getJsonObject(JsonArrIter).getString("effectiveDate");
		while(dataSource.getDateDiffDays(dateIter, end) > 0) {
			while(JsonArrIter < array.size() && dataSource.getDateDiffDays(dateIter, array.getJsonObject(JsonArrIter).getString("effectiveDate")) == 0) {
				dateIter = dataSource.addDaysStr(dateIter, 1);
				values[dayIter++] = array.getJsonObject(JsonArrIter++).getJsonNumber("mid").doubleValue();
			}
			while(JsonArrIter < array.size() - 1  && dataSource.getDateDiffDays(dateIter, array.getJsonObject(JsonArrIter).getString("effectiveDate")) > 0) {
				values[dayIter++] = -1;
				dateIter = dataSource.addDaysStr(dateIter, 1);
			}
		}
		for(int i=0; i<values.length; i++) {
			if(values[i] == 0) values[i] = -1;
		}
		return values;
	}
	
	/**
	 * Helper method, prints the histogram to std.out according to the passed parameters
	 * @param values The table of prices
	 * @param start The start of the time period
	 * @param end The end of the time period
	 * @param currency The currency in question code
	 */
	private void printHistogram(double values[], String start, String end, String currency) {
		System.out.println("Printing the week-based histogram of the price of " + currency + " during the period " + start + " - " + end + ":");
		Map<Integer, Double> maxs = new HashMap<>();
		for(int i=0; i<5; i++)
			maxs.put(i, Double.MIN_VALUE);
		for(int i=0; i<values.length;) {
			for(int j=0; j<7; j++, i++) {
				if(j == 5 || j == 6) continue;
				if(values[i] >= 0 && values[i] > maxs.get(j))
					maxs.put(j, values[i]);
			}
		}
		double globalMax = Double.MIN_VALUE;
		double globalMin = Double.MAX_VALUE;
		for(int i=0; i<values.length; i++) {
			if(values[i] <= 0) continue;
			if(values[i] > globalMax)
				globalMax = values[i];
			if(values[i] < globalMin)
				globalMin = values[i];
		}
		String days[] = {"Mon", "Tue", "Wed", "Thu", "Fri"};
		int week = 0;
		for(int i=0; i<7; i++) {
			week = 0;
			if(i == 5 || i == 6) continue;
			for(int j=i; j<values.length; j+=7) {
				week++;
				System.out.printf("[%s%03d]", days[i], week);
				if(values[j] < 0) System.out.println("-- No data --");
				else { 
					printBar((int)(((values[j] - globalMin) / (globalMax - globalMin))*20), '#');
					System.out.print(" (" + values[j] + ")");
					System.out.println();
				}
			}
			System.out.println();
			System.out.println();
		}
	}
	
	/**
	 * Helper methods used for printing the histogram bars
	 * @param counter The length of the bar
	 * @param character The character used for creating the bar
	 */
	private void printBar(int counter, char character) {
		for(int i=0; i<counter; i++) {
			System.out.print(character);
		}
	}

	/* (non-Javadoc)
	 * @see webapi.Observer#update(webapi.AbstractDataSource)
	 */
	@Override
	public void update(AbstractDataSource dataSource) {
		this.dataSource = (NBPDataSource) dataSource;
		
	}

}
