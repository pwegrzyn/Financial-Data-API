package webapi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.json.JsonArray;
import javax.json.JsonObject;

/**
 * Finds the peak values of a given currency in the whole available time period
 * @author Patryk Wegrzyn
 */
public class WhenLowestHighestOrder implements IOrderExecutable, Observer {

	/**
	 * The NBP Web API data source
	 */
	private NBPDataSource dataSource;
	/**
	 * Provides basic numeric and statistical operations on Json Objects
	 */
	private JsonNumericOperations operations;
	/**
	 * The currency in question
	 */
	private String currency;
	/**
	 * The start of the time period - 2002-01-02
	 */
	private String start;
	/**
	 * The end of the time period, the current date is assumed
	 */
	private String end;
	/**
	 * Flag set when passed arguments have an invalid format
	 */
	private boolean wrongArgFormat = false;
	
	/**
	 * Provides basic constructor functionality
	 * @param parameter User input CLI parameter
	 * @param dataSource The NBP Web API data source
	 * @param orderPerformer The subject to be observed
	 */
	public WhenLowestHighestOrder(String parameter, NBPDataSource dataSource, Subject orderPerformer) {
		if(parameter == null || !parameter.matches("[\\w]{3}"))
			this.wrongArgFormat = true;
		if (!wrongArgFormat) {
			this.dataSource = dataSource;
			this.currency = parameter;
			this.operations = JsonNumericOperations.getInstance();
			this.start = "2002-01-02";
			Date now = new Date(System.currentTimeMillis());
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			this.end = formatter.format(now);
		}
		orderPerformer.register(this);
	}

	/* (non-Javadoc)
	 * @see webapi.IOrderExecutable#execute()
	 */
	public void execute() throws MalformedURLException, IOException, WebApiException {
		System.out.println("Finding the dates on which the currency " + this.currency + " was the cheapest and the most expensive...");
		if(wrongArgFormat) {
			System.out.println("Error! The format of passed arguments is invalid");
			return;
		}
		if(this.currency.equals("") || this.currency.length() != 3) {
			System.out.println("The provided currency code is invalid!");
			return;
		}
		
		String originalEnd = this.end;
		JsonObject min, max;
		JsonArray array;
		JsonObject object;
		double globalMin = Double.MAX_VALUE;
		double globalMax = Double.MIN_VALUE;
		String whenGlobalMin = "";
		String whenGlobalMax = "";
		try {
			while((int) dataSource.getDateDiff(this.start, originalEnd, TimeUnit.DAYS) >= dataSource.getMaxPeriodPerQuery()) {
				Date endDate = this.dataSource.addDays(this.start, this.dataSource.getMaxPeriodPerQuery() - 1);
				Date endDateNext = this.dataSource.addDays(this.start, this.dataSource.getMaxPeriodPerQuery());
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				this.end = formatter.format(endDate);
				String nextEnd = formatter.format(endDateNext);
				try {
					object = dataSource.getJsonObjFromURL("exchangerates/rates/A/" + this.currency + "/" + this.start + "/" + this.end);
					array = object.getJsonArray("rates");
					min = operations.getMinOfArrJsonObj(array, "mid");
					max = operations.getMaxOfArrJsonObj(array, "mid");
					if(min.getJsonNumber("mid").doubleValue() < globalMin) {
						globalMin = min.getJsonNumber("mid").doubleValue();
						whenGlobalMin = min.getString("effectiveDate");
					}
					if(max.getJsonNumber("mid").doubleValue() > globalMax) {
						globalMax = max.getJsonNumber("mid").doubleValue();
						whenGlobalMax = max.getString("effectiveDate");
					}
				} catch (WebApiException e) {
					if(!e.getMessage().equals("Not Found - Brak danych"))
						throw new WebApiException(e.getMessage());
				}
				this.start = nextEnd;
			}
			try {
				object = dataSource.getJsonObjFromURL("exchangerates/rates/A/" + this.currency + "/" + this.start + "/" + originalEnd);
				array = object.getJsonArray("rates");
				min = operations.getMinOfArrJsonObj(array, "mid");
				max = operations.getMaxOfArrJsonObj(array, "mid");
				if(min.getJsonNumber("mid").doubleValue() < globalMin) {
					globalMin = min.getJsonNumber("mid").doubleValue();
					whenGlobalMin = min.getString("effectiveDate");
				}
				if(max.getJsonNumber("mid").doubleValue() > globalMax) {
					globalMax = max.getJsonNumber("mid").doubleValue();
					whenGlobalMax = max.getString("effectiveDate");
				}
			} catch (WebApiException e) {
				if(!e.getMessage().equals("Not Found - Brak danych"))
					throw new WebApiException(e.getMessage());
			}
			if(whenGlobalMin.equals("") || whenGlobalMax.equals(""))
				throw new WebApiException("Not Found - Brak danych");
			System.out.println("Minimum price of " + this.currency + " was " + globalMin + " on " + whenGlobalMin);
			System.out.println("Maximum price of " + this.currency + " was " + globalMax + " on " + whenGlobalMax);
		} catch (WebApiException e) {
			System.out.println("The peaks of the price of " + this.currency + " could not be found: " + e.getMessage());
		} catch (javax.json.stream.JsonParsingException e) {
			System.out.println("The peaks of the price of " + this.currency + " could not be found: an unidentified JavaScript source has been found during the parsing process");
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
