package webapi;


/**
 * The first extension to the abstract data source class, represents a particular Web API data source,
 * in this case it is the Web API provided by Narodowy Bank Polski - NBP
 * @author Patryk Wegrzyn
 */
public class NBPDataSource extends AbstractDataSource {
	
	/**
	 * The data source has two limits for the size of a single query so an additinal field is required
	 */
	private int maxPeriodPerQuery2;
	/**
	 * Represents the amount of currencies in table A
	 */
	private int currenciesInTableA;
	
	//Singleton Design Pattern
	/**
	 * Static field, which holds the only instance of this class during the lifetime of an application run
	 */
	private static NBPDataSource firstInstance = null;
	
	/**
	 * Private constructor because we only allow to create this object be using the getInstance method
	 */
	private NBPDataSource() {
		
		this.urlStart = ("http://api.nbp.pl/api/");
		this.urlEnd = ("/?format=json");
		this.maxPeriodPerQuery = 367;
		this.maxPeriodPerQuery2 = 93;
		this.currenciesInTableA = 35;
	
	}
	
	/**
	 * Static method used as a factory for this class, if an object of this class already exists - it returns it, 
	 * otherwise it returns a newly created object and saves it for further requests. This way only one instance of this class
	 * will be present at any given time. The essence of the Singleton Design Pattern
	 * @return The only available instance of this class
	 */
	public static NBPDataSource getInstance() {
		
		if(firstInstance == null) {
			
			firstInstance = new NBPDataSource();
			
		}
		
		return firstInstance;
	}

	/**
	 * Retrieves the current value of the maxPeriodPerQuery2 field
	 * @return the maxPeriodPerQuery2 The current value of the field
	 */
	public int getMaxPeriodPerQuery2() {
		return maxPeriodPerQuery2;
	}

	/**
	 * Sets the value of the maxPeriodPerQuery2 field to new value
	 * @param maxPeriodPerQuery2 the maxPeriodPerQuery2 to be set
	 */
	public void setMaxPeriodPerQuery2(int maxPeriodPerQuery2) {
		this.maxPeriodPerQuery2 = maxPeriodPerQuery2;
	}

	/**
	 * Retrieves the current value of this field
	 * @return the currenciesInTableA field value
	 */
	public int getCurrenciesInTableA() {
		return currenciesInTableA;
	}

	/**
	 * Sets the value of the field to a new value
	 * @param currenciesInTableA the currenciesInTableA to be set
	 */
	public void setCurrenciesInTableA(int currenciesInTableA) {
		this.currenciesInTableA = currenciesInTableA;
	}

}
