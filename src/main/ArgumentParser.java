package webapi;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Class used handle the Commons CLI command line parser. Has the ability to expand the list of program options.
 * @author Patryk Wegrzyn
 */
public class ArgumentParser {

	/**
	 * Program arguments passed from the CL, need to be parsed
	 */
	private String[] args;
	
	/**
	 * Binds a particular set of CL args with an instance of a parser.
	 * @param args Command line arguments passed to the program.
	 */
	public ArgumentParser(String[] args) {
		
		this.args = args;
		
	}
	
	/**
	 * Sets the args field with a custom set of arguments.
	 * @param args Custom command line arguments.
	 */
	public void setArgs(String[] args) {
		this.args = args;
	}
	
	/**
	 * Retrieves the array of CL arguments associated with an particular instance of a parser.
	 * @return Saved command line arguments
	 */
	public String[] getArgs() {
		return this.args;
	}
	
	/**
	 * Main function of this class: first, it creates a custom set of program options, then it parsers the args field according to them.
	 * Handles the absence of any arguments by printing the usage help interface.
	 * @return Commons CLI class which represents CL arguments parsed according to the set of created options.
	 * @throws ParseException Represents an error, which occurred during the process of parsing. Comes from the Commons CLI library.
	 */
	public CommandLine parse() throws ParseException {

		CommandLineParser parser = new DefaultParser();
		
		//create options
		Options options = new Options();
		options.addOption(Option.builder("c").longOpt("date-price").desc("Print the price on a given date of "
				+ "gold and a given currency").argName("currency,[date]").hasArgs().valueSeparator(',').build());
		options.addOption(Option.builder("a").longOpt("gold-average").desc("Print the average price of "
				+ "gold for a given period").argName("start,[end]").hasArgs().valueSeparator(',').build());
		options.addOption(Option.builder("h").longOpt("highest-amplitude").desc("Print the currency, "
				+ "which had the highest price amplitude starting from a given date").argName("date").hasArg().build());
		options.addOption(Option.builder("l").longOpt("lowest-price").desc("Print the currency, "
				+ "which was the cheapest to buy on a given date").argName("[date]").hasArg().build());
		options.addOption(Option.builder("s").longOpt("sort-by-difference").desc("Prints the list of "
				+ "N currencies from table C sorted by the difference of their buy and sell price on a given date").argName("N,[date]").hasArgs().valueSeparator(',').build());
		options.addOption(Option.builder("w").longOpt("lowest-highest").desc("Print the dates "
				+ "on which a given currency reached its highest and lowest price").argName("currency").hasArg().build());
		options.addOption(Option.builder("p").longOpt("week-graph").desc("Print a week - based ASCII graph presenting the relative "
				+ "change of value of a given currency during a given period").argName("currency;start;end").hasArgs().valueSeparator(';').build());
		
		if(this.args.length == 0) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(100,"WebApiSystem", "Provides basic numeric and statistical operations for the NBP Web API\n\n", options, "\nPlease report issues at abc@xyz.com");
			System.out.println();
			System.out.println("Use commas to separe values if a given option can take multiple arguments.");
			System.out.println("Often when a date is not specified in a required field, the current date will be assumed.");
			return null;
		}
		
		CommandLine configs = parser.parse(options, args, true);
		return configs;
		
	}

}
