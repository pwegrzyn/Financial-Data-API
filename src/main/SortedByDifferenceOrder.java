package webapi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;

/**
 * Finds and prints information about N given currencies with the biggest difference of bid and ask price on a given date
 * @author Patryk Wegrzyn
 */
public class SortedByDifferenceOrder implements IOrderExecutable, Observer {

	/**
	 * The NBP API data source
	 */
	private NBPDataSource dataSource;
	/**
	 * Includes helper methods for numeric and statistical operations on Json objects
	 */
	private JsonNumericOperations operations;
	/**
	 * The date in question, if none is passed the current date is assumed
	 */
	private String date;
	/**
	 * Number of currencies to be found and printed
	 */
	private int numberOfCurrencies;
	/**
	 * Flag set when the format of the passed arguments is invalid
	 */
	private boolean wrongArgFormat = false;
	
	/**
	 * Normal constructor
	 * @param parameters User input CLI parameters
	 * @param dataSource The NBP Web API data source
	 * @param orderPerformer The subject to be observed
	 */
	public SortedByDifferenceOrder(String[] parameters, NBPDataSource dataSource, Subject orderPerformer) {
		if((parameters.length != 2 && parameters.length != 1) || !parameters[0].matches("[\\d]+"))
			this.wrongArgFormat = true; 
		if (!wrongArgFormat) {
			if (parameters.length == 1) {
				Date now = new Date(System.currentTimeMillis());
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				this.date = formatter.format(now);
			} else if (parameters.length == 2) {
				this.date = parameters[1];
				if(!parameters[1].matches("[\\d]{4}-\\d\\d-\\d\\d"))
					this.wrongArgFormat = true;
			}
			if (!parameters[0].equals("")) {
				this.numberOfCurrencies = Integer.parseInt(parameters[0]);
			}
			this.dataSource = dataSource;
			this.operations = JsonNumericOperations.getInstance();
			orderPerformer.register(this);
		}
	}

	/* (non-Javadoc)
	 * @see webapi.IOrderExecutable#execute()
	 */
	public void execute() throws MalformedURLException, IOException, WebApiException{
		System.out.println("Printing the " + this.numberOfCurrencies + " first currencies from "
				+ "table C sorted by the difference of their ask and bid price on a given date...");
		if(wrongArgFormat) {
			System.out.println("Error! The format of passed arguments is invalid");
			return;
		}
		if(this.numberOfCurrencies == 0) {
			System.out.println("Can't print 0 currencies!");
			return;
		}
		
		JsonObject object;
		JsonArray array, innerArray;
		
		try {
			array = dataSource.getJsonArrFromURL("exchangerates/tables/C/" + this.date);
			object = array.getJsonObject(0);
			innerArray = object.getJsonArray("rates");
			List<JsonObject> myList = operations.getNsortedByDiff(innerArray, numberOfCurrencies, "ask", "bid");
			System.out.println("The " + this.numberOfCurrencies + " first currencies for the date " + this.date + " are:");
			for(int i=0; i<myList.size(); i++) {
				System.out.println((i + 1) + ". " + myList.get(i).getString("code") + " (Difference: " + (myList.get(i).getJsonNumber("ask").doubleValue() - myList.get(i).getJsonNumber("bid").doubleValue()) + ")");
			}
			
		} catch (WebApiException e) {
			System.out.println("The list of currencies could not be retrieved for the date " + this.date + ": " + e.getMessage());
		} catch (javax.json.stream.JsonParsingException e) {
			System.out.println("The list of currencies could not be retrieved for the date " + this.date + ": an unidentified JavaScript source has been found during the parsing process");
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
