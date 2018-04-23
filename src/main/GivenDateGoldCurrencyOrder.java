package webapi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.json.JsonArray;
import javax.json.JsonObject;

/**
 * Retrieves the price of gold and given currency on a given date
 * @author Patryk Wegrzyn
 */
public class GivenDateGoldCurrencyOrder implements IOrderExecutable, Observer {

	/**
	 * The currency in question
	 */
	private String currency;
	/**
	 * The date in question, if none is provided the current date is assumed
	 */
	private String date;
	/**
	 * The NBP data source API
	 */
	private NBPDataSource dataSource;
	/**
	 * Flag set when the passed arguments have and invalid format
	 */
	private boolean wrongArgFormat = false;

	/**
	 * Typical constructor
	 * @param parameters User input CLI parameters
	 * @param dataSource The given NBP data source
	 * @param orderPerformer The subject to be observed
	 */
	public GivenDateGoldCurrencyOrder(String[] parameters, NBPDataSource dataSource, Subject orderPerformer) {
		if((parameters.length != 2 && parameters.length != 1) || !parameters[0].matches("[\\w]{3}"))
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
			this.currency = parameters[0];
			this.dataSource = dataSource;
			orderPerformer.register(this);
		}
	}

	/* (non-Javadoc)
	 * @see webapi.IOrderExecutable#execute()
	 */
	@Override
	public void execute() throws MalformedURLException, IOException, WebApiException  {
		System.out.println("Price of gold and given currency in a given day...");
		if(wrongArgFormat) {
			System.out.println("Error! The format of passed arguments is invalid");
			return;
		}
		
		JsonObject object, arrayObject;
		JsonArray array;
		
		try {
			object = dataSource.getJsonObjFromURL("exchangerates/rates/A/" + this.currency + "/" + this.date);
			array = object.get("rates").asJsonArray();
			arrayObject = array.getJsonObject(0);
			System.out.println("The price of " + object.getString("code") + " on " + this.date + " was " + arrayObject.get("mid"));
		} catch (WebApiException e) {
			System.out.println("The price of " + this.currency + " on " + this.date + " could not be retrieved: " + e.getMessage());
		} catch (javax.json.stream.JsonParsingException e) {
			System.out.println("The price of " + this.currency + " on " + this.date + " could not be retrieved: an unidentified JavaScript source has been found during the parsing process");
		}
		
		try {
			array = dataSource.getJsonArrFromURL("cenyzlota/" + this.date);
			arrayObject = array.getJsonObject(0);
			System.out.println("The price of gold on " + this.date + " was " + arrayObject.get("cena"));
		} catch (WebApiException e) {
			System.out.println("The price of gold on " + this.date + " could not be retrieved: " + e.getMessage());
		} catch (javax.json.stream.JsonParsingException e) {
			System.out.println("The price of gold on " + this.date + " could not be retrieved: an unidentified JavaScript source has been found during the parsing process");
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
