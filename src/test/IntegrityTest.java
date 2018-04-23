package test;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.commons.cli.CommandLine;
import org.junit.jupiter.api.Test;

import webapi.AbstractDataSource;
import webapi.ArgumentParser;
import webapi.JsonNumericOperations;
import webapi.NBPDataSource;
import webapi.NBPOrderPerformer;

class IntegrityTest {

	@Test
	void integrityTest() {
		
		String[] args = {"--date-price=USD", "--gold-average=2014-01-01,2016-01-03", "--highest-amplitude=2016-01-09", "--lowest-price=2014-06-05",
				"--sort-by-difference=5,2016-02-10", "--lowest-highest=USD", "--week-graph=EUR;2014,11,3;2016,01,2"};
		try {
			
			assertNotNull(args);
			assertNotEquals(args.length, 8);
			assertNotEquals(args.length, 9);
			assertEquals(args.length, 7);
			ArgumentParser argParser = new ArgumentParser(args);
			assertNotNull(argParser);
			assertEquals(argParser.getArgs(), args);
			CommandLine configs = argParser.parse();
			assertNotNull(configs);
			assertFalse(configs.hasOption("help"));
			assertFalse(configs.hasOption("version"));
			assertFalse(configs.hasOption("v"));
			assertFalse(configs.hasOption("dateprice"));
			assertFalse(configs.hasOption("lowestprice"));
			assertFalse(configs.hasOption("sortbydifference"));
			assertFalse(configs.hasOption("weekgraph"));
			assertFalse(configs.hasOption("highestamplitude"));
			assertFalse(configs.hasOption("goldaverage"));
			assertFalse(configs.hasOption("lowesthighest"));
			assertTrue(configs.hasOption("date-price"));
			assertTrue(configs.hasOption("lowest-price"));
			assertTrue(configs.hasOption("sort-by-difference"));
			assertTrue(configs.hasOption("week-graph"));
			assertTrue(configs.hasOption("highest-amplitude"));
			assertTrue(configs.hasOption("gold-average"));
			assertTrue(configs.hasOption("lowest-highest"));
			assertNotEquals(configs.getOptionValue("highest-amplitude"), "2016-01-9");
			assertNotEquals(configs.getOptionValue("highest-amplitude"), "2016-1-09");
			assertEquals(configs.getOptionValue("highest-amplitude"), "2016-01-09");
			assertNotEquals(configs.getOptionValue("lowest-price"), "2014-06-5");
			assertNotEquals(configs.getOptionValue("lowest-price"), "2014-6-05");
			assertEquals(configs.getOptionValue("lowest-price"), "2014-06-05");
			assertEquals(configs.getOptionValue("lowest-highest"), "USD");
			assertEquals(configs.getOptionValue("date-price"), "USD");
			AbstractDataSource dataSource = NBPDataSource.getInstance();
			AbstractDataSource dataSource2 = NBPDataSource.getInstance();
			assertNotNull(dataSource);
			assertNotNull(dataSource2);
			assertEquals(dataSource, dataSource2);
			assertNotEquals(dataSource.getMaxPeriodPerQuery(), 366);
			assertEquals(dataSource.getMaxPeriodPerQuery(), 367);
			assertNotEquals(dataSource.getMaxPeriodPerQuery(), 368);
			assertNotEquals(((NBPDataSource)dataSource).getMaxPeriodPerQuery2(), 92);
			assertNotEquals(((NBPDataSource)dataSource).getMaxPeriodPerQuery2(), 91);
			assertEquals(((NBPDataSource)dataSource).getMaxPeriodPerQuery2(), 93);
			assertTrue(true);
			JsonNumericOperations operations1 = JsonNumericOperations.getInstance();
			JsonNumericOperations operations2 = JsonNumericOperations.getInstance();
			assertNotNull(operations1);
			assertNotNull(operations2);
			assertEquals(operations1, operations2);
			NBPOrderPerformer orderPerformer = new NBPOrderPerformer(configs, dataSource);
			assertEquals(orderPerformer.getDataSource(), NBPDataSource.getInstance());
			assertNotNull(orderPerformer.getOrders());
			assertNotNull(orderPerformer.getDataSource());
			assertNotNull(orderPerformer);
			
		} catch (Exception e) {
			assertTrue(false);
		}
	}

}
