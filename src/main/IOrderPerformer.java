package webapi;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Public interface class which represents a object whose job is to perform all the orders
 * which have been ordered by the user
 * @author Patryk Wegrzyn
 */
public interface IOrderPerformer {

	/**
	 * Main method of this interface, represents the process of executing all the orders
	 * of this application which are associated with a particular data source and a 
	 * particular web API
	 * @throws MalformedURLException thrown when an invalid URL was encountered
	 * @throws IOException thrown when an IO error was encountered during the process of fetching data from the server
	 * @throws WebApiException thrown when no data has been found for this query, or the query was invalid (wrong format or exceeded limit)
	 */
	void performOrders() throws MalformedURLException, IOException, WebApiException;

}
