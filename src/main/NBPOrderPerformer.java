package webapi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

/**
 * The implementation of the OrderPerformer interface associated with the Web API provided by Narodowy Bank Polski
 * @author Patryk Wegrzyn
 */
public class NBPOrderPerformer implements IOrderPerformer,Subject {

	/**
	 * Represents the user input for the program - the set order flags and associated with them values of passed arguments
	 */
	private CommandLine configs;
	/**
	 * Represents the data source of the API, in this case its the API provided by Narodoway Bank Polski
	 */
	private NBPDataSource dataSource;
	/**
	 * List of all observers of this subjects, element of the Observer design pattern
	 */
	private ArrayList<Observer> observers;
	/**
	 * The list of all orders created by this OrderPerformer
	 */
	private ArrayList<IOrderExecutable> orders;
	
	/**
	 * Typical constructor method, sets the configs and dataSouce field values according to the passed arguments
	 * and initiates its used data structures
	 * @param configs User CLI input
	 * @param dataSource THe data source of NBP
	 */
	public NBPOrderPerformer(CommandLine configs, AbstractDataSource dataSource) {
		this.configs = configs;
		this.dataSource = (NBPDataSource) dataSource;
		observers = new ArrayList<Observer>();
		orders = new ArrayList<>();
	}

	/* (non-Javadoc)
	 * @see webapi.IOrderPerformer#performOrders()
	 */
	@Override
	public void performOrders() throws MalformedURLException, IOException, WebApiException {
		
		String parameter;
		String parameters[];
		IOrderExecutable order;
		
		for(Option arg : configs.getOptions()) {
			if(configs.hasOption(arg.getLongOpt())) {
				switch(arg.getLongOpt()) {
					case "date-price":
						parameters = configs.getOptionValues("date-price");
						order = new GivenDateGoldCurrencyOrder(parameters, dataSource, this);
						break;
					case "gold-average":
						parameters = configs.getOptionValues("gold-average");
						order = new AverageGoldPriceOrder(parameters, dataSource, this);
						break;
					case "highest-amplitude":
						parameter = configs.getOptionValue("highest-amplitude");
						order = new HighestAmplitudeOrder(parameter, dataSource, this);
						break;
					case "lowest-price":
						parameter = configs.getOptionValue("lowest-price");
						order = new LowestPriceCurrencyOrder(parameter, dataSource, this);
						break;
					case "sort-by-difference":
						parameters = configs.getOptionValues("sort-by-difference");
						order = new SortedByDifferenceOrder(parameters, dataSource, this);
						break;
					case "lowest-highest":
						parameter = configs.getOptionValue("lowest-highest");
						order = new WhenLowestHighestOrder(parameter, dataSource, this);
						break;
					case "week-graph":
						parameters = configs.getOptionValues("week-graph");
						order = new WeeklyBarGraphOrder(parameters, dataSource, this);
						break;
					default:
						order = null;
						break;
				}
				if(order != null) {
					System.out.print("Executing order - ");
					order.execute();
					this.orders.add(order);
					System.out.println();
					System.out.println("---------------------------------------------------------------");
					System.out.println();
				}
			}
		}
	}

	/**
	 * Retrieves the dataSource field value
	 * @return the current dataSource field value
	 */
	public NBPDataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Sets the dataSource field to a new value, also notifies all the observers of this subject about the change
	 * as a part of the Observer Design Pattern
	 * @param dataSource the dataSource to set
	 */
	public void setDataSource(NBPDataSource dataSource) {
		this.dataSource = dataSource;
		notifyObserver();
	}

	/* (non-Javadoc)
	 * @see webapi.Subject#register(webapi.Observer)
	 */
	@Override
	public void register(Observer newObserver) {
		observers.add(newObserver);
		
	}

	/* (non-Javadoc)
	 * @see webapi.Subject#unregister(webapi.Observer)
	 */
	@Override
	public void unregister(Observer removeObserver) {
		int observerIndex = observers.indexOf(removeObserver);
		observers.remove(observerIndex);
		
	}

	/* (non-Javadoc)
	 * @see webapi.Subject#notifyObserver()
	 */
	@Override
	public void notifyObserver() {

		for(Observer observer : observers) {
			
			observer.update(this.dataSource);
			
		}
		
	}

	/**
	 * Retrieves the current value of the orders field
	 * @return the current value of orders field
	 */
	public ArrayList<IOrderExecutable> getOrders() {
		return orders;
	}

	/**
	 * Sets the orders field to a new value
	 * @param orders the orders to set
	 */
	public void setOrders(ArrayList<IOrderExecutable> orders) {
		this.orders = orders;
	}

}
