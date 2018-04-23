package webapi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;

/**
 * Finds the currency with the lowest bid price on a given date
 * @author Patryk Wegrzyn
 */
public class LowestPriceCurrencyOrder implements IOrderExecutable, Observer {

	/**
	 * Date in question, if none is passed the current date is assumed
	 */
	private String date;
	/**
	 * The NBP API data source 
	 */
	private NBPDataSource dataSource;
	/**
	 * Included helper methods for numeric and statistical operations on Json objects
	 */
	private JsonNumericOperations operations;
	/**
	 * Flag set when the format of the passed parameters is invalid
	 */
	private boolean wrongArgFormat = false;

	/**
	 * Normal constructor
	 * @param parameter User input CLI parameters
	 * @param dataSource The NBP API data source
	 * @param orderPerformer The subject to be observed
	 */
	public LowestPriceCurrencyOrder(String parameter, NBPDataSource dataSource, Subject orderPerformer) {
		if(parameter.equals("")) {
			Date now = new Date(System.currentTimeMillis());
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			this.date = formatter.format(now);
		} else {
			this.date = parameter;
			if(!parameter.matches("[\\d]{4}-\\d\\d-\\d\\d"))
				wrongArgFormat = true;
		}
		this.dataSource = dataSource;
		this.operations = JsonNumericOperations.getInstance();
		orderPerformer.register(this);
	}

	/* (non-Javadoc)
	 * @see webapi.IOrderExecutable#execute()
	 */
	public void execute() throws MalformedURLException, IOException, WebApiException {
		System.out.println("Finding the currency from table C which was the cheapest to buy on a given date... ");
		if(wrongArgFormat) {
			System.out.println("Error! The format of passed arguments is invalid");
			return;
		}
		
		JsonObject object;
		JsonArray array, innerArray;
		
		try {
			array = dataSource.getJsonArrFromURL("exchangerates/tables/C/" + this.date);
			object = array.getJsonObject(0);
			innerArray = object.getJsonArray("rates");
			Map<String, Object> result = operations.getMinOfArr(innerArray, "bid", "code");
			System.out.println("Found currency: " + (String) result.get("name"));
			System.out.println("Date: " + object.getString("effectiveDate"));
			System.out.println("Bid price: " + (Double) result.get("min"));
		} catch (WebApiException e) {
			System.out.println("The cheapest currency could not be found for the date " + this.date + ": " + e.getMessage());
		} catch (javax.json.stream.JsonParsingException e) {
			System.out.println("The cheapest currency could not be found for the date " + this.date + ": an unidentified JavaScript source has been found during the parsing process");
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
