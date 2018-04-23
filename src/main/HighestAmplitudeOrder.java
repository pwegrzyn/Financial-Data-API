package webapi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.json.JsonArray;

/**
 * Finds the currency with the biggest price fluctuation in period of time starting with a given date
 * @author Patryk Wegrzyn
 */
public class HighestAmplitudeOrder implements IOrderExecutable, Observer {

	/**
	 * The NBP API data source
	 */
	private NBPDataSource dataSource;
	/**
	 * Includes helper methods for numeric and statistical operations
	 */
	private JsonNumericOperations operations;
	/**
	 * Start of the period
	 */
	private String start;
	/**
	 * End of the period, the current date is automatically assumed
	 */
	private String end;
	/**
	 * Duration of the period in days
	 */
	private int dayDifference;
	/**
	 * Flag set when the given arguments have an invalid format
	 */
	private boolean wrongArgFormat = false;
	
	/**
	 * Normal constructor
	 * @param parameter Used input CLI parameters
	 * @param dataSource The NBP API data source
	 * @param orderPerformer The subject to be observed
	 */
	public HighestAmplitudeOrder(String parameter, NBPDataSource dataSource, Subject orderPerformer) {
		if(!parameter.matches("[\\d]{4}-\\d\\d-\\d\\d"))
			this.wrongArgFormat = true;
		this.start = parameter;
		Date now = new Date(System.currentTimeMillis());
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		this.end = formatter.format(now);
		this.dataSource = dataSource;
		this.operations = JsonNumericOperations.getInstance();
		if(!wrongArgFormat)
			this.dayDifference = (int) dataSource.getDateDiff(this.start, this.end, TimeUnit.DAYS);
		orderPerformer.register(this);
	}

	/* (non-Javadoc)
	 * @see webapi.IOrderExecutable#execute()
	 */
	public void execute() throws MalformedURLException, IOException, WebApiException {
		System.out.println("Finding the currency from table A which had the highest price amplitude"
				+ " starting from a given date...");
		if(wrongArgFormat) {
			System.out.println("Error! The format of passed arguments is invalid");
			return;
		}
		
		if(this.dayDifference < this.dataSource.getMaxPeriodPerQuery2()) {
			try {
				JsonArray outerArray = dataSource.getJsonArrFromURL("exchangerates/tables/A/" + this.start + "/" + this.end);
				Map<String, Object> result = operations.findMaxAmpOfArr(outerArray, "rates", "code", "mid", "effectiveDate");
				System.out.println("Found currency: " + (String) result.get("object"));
				System.out.println("Minimum price: " + result.get("min") + " (" + (String) result.get("whenMin") + ")");
				System.out.println("Maximum price: " + result.get("max") + " (" + (String) result.get("whenMax") + ")");
				System.out.println("Amplitude: " + (double) result.get("amplitude"));
			} catch (WebApiException e) {
				System.out.println("The currency for the period from " + this.start + " to " + this.end + " could not be found: " + e.getMessage());
			} catch (javax.json.stream.JsonParsingException e) {
				System.out.println("The currency for the period from " + this.start + " to " + this.end + " could not be found: an unidentified JavaScript source has been found during the parsing process");
			}
		} else {
			String originalStart = this.start;
			String originalEnd = this.end;
			Map<String, Map<String, Object>> minMaxs;
			JsonArray array;
			Map<String,Double> globalMin = new HashMap<>();
			Map<String,Double> globalMax = new HashMap<>();
			Map<String,String> whenGlobalMin = new HashMap<>();
			Map<String,String> whenGlobalMax = new HashMap<>();
			try {
				while((int) dataSource.getDateDiff(this.start, originalEnd, TimeUnit.DAYS) >= dataSource.getMaxPeriodPerQuery2()) {
					Date endDate = this.dataSource.addDays(this.start, this.dataSource.getMaxPeriodPerQuery2() - 1);
					Date endDateNext = this.dataSource.addDays(this.start, this.dataSource.getMaxPeriodPerQuery2());
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
					this.end = formatter.format(endDate);
					String nextEnd = formatter.format(endDateNext);
					array = dataSource.getJsonArrFromURL("exchangerates/tables/A/" + this.start + "/" + this.end);
					minMaxs = operations.getMinMaxArrays(array, "rates", "code", "mid", "effectiveDate");
					for(Map.Entry<String, Object> entry : minMaxs.get("min").entrySet()) {
						if (!globalMin.containsKey(entry.getKey())) {
							globalMin.put(entry.getKey(), Double.MAX_VALUE);
							globalMax.put(entry.getKey(), Double.MIN_VALUE);
							whenGlobalMin.put(entry.getKey(), "");
							whenGlobalMax.put(entry.getKey(), "");
						}
					}
					for(Map.Entry<String, Object> entry : minMaxs.get("min").entrySet()) {
						if((Double)(minMaxs.get("min").get(entry.getKey())) < globalMin.get(entry.getKey())) {
							globalMin.put(entry.getKey(), (Double)(minMaxs.get("min").get(entry.getKey())));
							whenGlobalMin.put(entry.getKey(), (String)minMaxs.get("whenMin").get(entry.getKey()));
						}
						if((Double)(minMaxs.get("max").get(entry.getKey())) > globalMax.get(entry.getKey())) {
							globalMax.put(entry.getKey(), (Double)(minMaxs.get("max").get(entry.getKey())));
							whenGlobalMax.put(entry.getKey(), (String)minMaxs.get("whenMax").get(entry.getKey()));
						}
					}
					this.start = nextEnd;
				}
				array = dataSource.getJsonArrFromURL("exchangerates/tables/A/" + this.start + "/" + originalEnd);
				minMaxs = operations.getMinMaxArrays(array, "rates", "code", "mid", "effectiveDate");
				for(Map.Entry<String, Object> entry : minMaxs.get("min").entrySet()) {
					if((Double)(minMaxs.get("min").get(entry.getKey())) < globalMin.get(entry.getKey())) {
						globalMin.put(entry.getKey(), (Double)(minMaxs.get("min").get(entry.getKey())));
						whenGlobalMin.put(entry.getKey(), (String)minMaxs.get("whenMin").get(entry.getKey()));
					}
					if((Double)(minMaxs.get("max").get(entry.getKey())) > globalMax.get(entry.getKey())) {
						globalMax.put(entry.getKey(), (Double)(minMaxs.get("max").get(entry.getKey())));
						whenGlobalMax.put(entry.getKey(), (String)minMaxs.get("whenMax").get(entry.getKey()));
					}
				}
				String whichCurrency = "";
				String whenMin = "";
				String whenMax = "";
				double maxAmp = Double.MIN_VALUE;
				double minVal = Double.MAX_VALUE;
				double maxVal = Double.MIN_VALUE;
				for(Map.Entry<String, Double> entry : globalMin.entrySet()) {
					if(globalMax.get(entry.getKey()) - globalMin.get(entry.getKey()) > maxAmp) {
						whichCurrency = entry.getKey();
						maxAmp = globalMax.get(entry.getKey()) - globalMin.get(entry.getKey());
						minVal = globalMin.get(entry.getKey());
						maxVal = globalMax.get(entry.getKey());
						whenMin = whenGlobalMin.get(entry.getKey());
						whenMax = whenGlobalMax.get(entry.getKey());
					}
				}
				
				System.out.println("Found currency: " + whichCurrency);
				System.out.println("Minimum price: " + minVal + " (" + whenMin + ")");
				System.out.println("Maximum price: " + maxVal + " (" + whenMax + ")");
				System.out.println("Amplitude: " + maxAmp);
				
			} catch (WebApiException e) {
				System.out.println("The currency for the period from " + originalStart + " to " + originalEnd + " could not be found: " + e.getMessage());
			} catch (javax.json.stream.JsonParsingException e) {
				System.out.println("The currency for the period from " + originalStart + " to " + originalEnd + " could not be found: an unidentified JavaScript source has been found during the parsing process");
			}
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
