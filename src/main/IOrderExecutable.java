package webapi;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Public interface for the all orders that this application is supposed to handle,
 * defines only one method - execute - called, when a particular order is ordered
 * @author Patryk Wegrzyn
 */
public interface IOrderExecutable {

	/**
	 * Main method of this interface, represents the execution a particular order
	 * @throws MalformedURLException thrown when an invalid URL was encountered
	 * @throws IOException thrown when an IO error was encountered during the process of fetching data from the server
	 * @throws WebApiException thrown when no data has been found for this query, or the query was invalid (wrong format or exceeded limit)
	 */
	public void execute() throws MalformedURLException, IOException, WebApiException;
	
}
