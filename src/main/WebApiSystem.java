package webapi;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

/**
 * I use the Commons CLI tool in this project
 * See http://commons.apache.org/proper/commons-cli/
 * for more details and the JSON-P library
 * See https://javaee.github.io/jsonp/ for more details
 * 
 * This is an application, which is able to retrieve particular pieces of data from the NBP
 * web API http://api.nbp.pl/ and process them according to the settings selected by the user.
 * Has the potential to be easily expanded into operating with other web APIs.
 * @author Patryk Wegrzyn
 *
 */
public class WebApiSystem {

	/**
	 * Run the program without arguments for the help window
	 * @param args Passed command line arguments
	 */
	public static void main(String[] args) {

		try {
			
			//parse the passed arguments
			ArgumentParser argParser = new ArgumentParser(args);
			CommandLine configs = argParser.parse();
				
			//if no argument were passed exit the program
			//(printing the help screen)
			if(configs == null) return;
			
			//create the dataSource for the NBP web API,
			//I use the Singleton Design Pattern to prevent creating more than one instance of this class
			//because having only one object will be always enough and we can save memory be possibly sharing it
			AbstractDataSource dataSource = NBPDataSource.getInstance();
			
			//this object will perform all the desired orders on the desired dataSource
			IOrderPerformer orderPerformer = new NBPOrderPerformer(configs, dataSource);
			orderPerformer.performOrders();
		
		} catch (ParseException e) {
			System.out.println("Parse error!");
		} catch (WebApiException e) {
			System.out.println("Web API Error!");
			System.out.println(e.getMessage());
		} catch (Exception e) {
			System.out.println("The following error has occurred: ");
			e.printStackTrace();
		}
		
	}
}