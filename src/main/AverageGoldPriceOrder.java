package webapi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.json.JsonArray;
import javax.json.JsonObject;

/**
 * Order for calculating the average price of gold in a given period of time
 * @author Patryk Wegrzyn
 */
public class AverageGoldPriceOrder implements IOrderExecutable,Observer {
	
	/**
	 * Represents the data source of the API
	 */
	private NBPDataSource dataSource;
	/**
	 * Includes helper methods for numeric and statistical operations
	 */
	private JsonNumericOperations operations;
	/**
	 * Start of the time period
	 */
	private String start;
	/**
	 * End of the time period, if none is provided the current date is assumed
	 */
	private String end;
	/**
	 * Duration of the period
	 */
	private int dayDifference;
	/**
	 * Sum of days found in server response Json
	 */
	private int effectiveDays;
	/**
	 * Flag set when the passed argumets have an invalid format
	 */
	private boolean wrongArgFormat = false;

	/**
	 * Normal constructor
	 * @param parameters Passed CLI arguments
	 * @param dataSource The NBP data source
	 * @param orderPerformer Subject to be observed
	 */
	public AverageGoldPriceOrder(String[] parameters, NBPDataSource dataSource, Subject orderPerformer) {
		if((parameters.length != 2 && parameters.length != 1) || !parameters[0].matches("[\\d]{4}-\\d\\d-\\d\\d"))
			this.wrongArgFormat = true;
		if (!wrongArgFormat) {
			if (parameters.length == 1) {
				Date now = new Date(System.currentTimeMillis());
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				this.end = formatter.format(now);
			} else if (parameters.length == 2) {
				this.end = parameters[1];
				if(!parameters[1].matches("[\\d]{4}-\\d\\d-\\d\\d"))
					this.wrongArgFormat = true;
			}
			this.start = parameters[0];
			this.dataSource = dataSource;
			if (!wrongArgFormat)
				this.dayDifference = (int) dataSource.getDateDiff(this.start, this.end, TimeUnit.DAYS);
			this.effectiveDays = 0;
			this.operations = JsonNumericOperations.getInstance();
			orderPerformer.register(this);
		}
	}

	/* (non-Javadoc)
	 * @see webapi.IOrderExecutable#execute()
	 */
	public void execute() throws MalformedURLException, IOException, WebApiException {
		System.out.println("Finding the average price of gold in a given period of time...");
		if(wrongArgFormat) {
			System.out.println("Error! The format of passed arguments is invalid");
			return;
		}
		
		JsonArray array;
		JsonObject arrayObject;
		double sum = 0;
		
		if(this.dayDifference < this.dataSource.getMaxPeriodPerQuery()) {
			if(this.start.equals(this.end)) {
				try {
					array = dataSource.getJsonArrFromURL("cenyzlota/" + this.start);
					arrayObject = array.getJsonObject(0);
					System.out.println("The average price of gold on " + this.start + " was " + arrayObject.get("cena"));
				} catch (WebApiException e) {
					System.out.println("The average price of gold on " + this.start + " could not be retrieved: " + e.getMessage());
				}
			}
			else {
				try {
					array = dataSource.getJsonArrFromURL("cenyzlota/" + this.start + "/" + this.end);
					System.out.println("The average price of gold from " + this.start + " to " + this.end + " was " + operations.getAvgOfArr(array, "cena"));
				} catch (WebApiException e) {
					System.out.println("The average price of gold from " + this.start + " to " + this.end + " could not be retrieved: " + e.getMessage());
				} catch (javax.json.stream.JsonParsingException e) {
					System.out.println("The average price of gold from " + this.start + " to " + this.end + " could not be retrieved: an unidentified JavaScript source has been found during the parsing process");
				}
			}
		} else {
			String originalStart = this.start;
			String originalEnd = this.end;
			try {
				while((int) dataSource.getDateDiff(this.start, originalEnd, TimeUnit.DAYS) >= dataSource.getMaxPeriodPerQuery()) {
					Date endDate = this.dataSource.addDays(this.start, this.dataSource.getMaxPeriodPerQuery() - 1);
					Date endDateNext = this.dataSource.addDays(this.start, this.dataSource.getMaxPeriodPerQuery());
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
					this.end = formatter.format(endDate);
					String nextEnd = formatter.format(endDateNext);
					sum += getSumForShortPeriod(this.start, this.end);
					this.start = nextEnd;
				}
				sum += getSumForShortPeriod(this.start, originalEnd);
				System.out.println("The average price of gold from " + originalStart + " to " + originalEnd + " was " + sum/this.effectiveDays);
			} catch (WebApiException e) {
				System.out.println("The average price of gold from " + originalStart + " to " + originalEnd + " could not be retrieved: " + e.getMessage());
			} catch (javax.json.stream.JsonParsingException e) {
				System.out.println("The average price of gold from " + originalStart + " to " + originalEnd + " could not be retrieved: an unidentified JavaScript source has been found during the parsing process");
			}
		}
	}
	
	//assumes that the period is short
	/**
	 * Helper method, used to calculate the sum of price of gold for a short period of time
	 * @param startDate The beginning of the time period
	 * @param endDate The end of the time period
	 * @return Calculated sum
	 * @throws MalformedURLException thrown when an invalid URL was encountered
	 * @throws WebApiException thrown when no data has been found for this query, or the query was invalid (wrong format or exceeded limit)
	 * @throws IOException thrown when an IO error was encountered during the process of fetching data from the server
	 */
	private double getSumForShortPeriod(String startDate, String endDate) throws MalformedURLException, WebApiException, IOException {
		JsonArray array;
		array = dataSource.getJsonArrFromURL("cenyzlota/" + startDate + "/" + endDate);
		this.effectiveDays += array.size();
		return operations.getSumOfArr(array, "cena");
	}

	/* (non-Javadoc)
	 * @see webapi.Observer#update(webapi.AbstractDataSource)
	 */
	@Override
	public void update(AbstractDataSource dataSource) {
		this.dataSource = (NBPDataSource) dataSource;
		
	}

}
