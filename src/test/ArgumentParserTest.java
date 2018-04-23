package test;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Test;

import webapi.ArgumentParser;

class ArgumentParserTest {

	@Test
	void testSetArgs() {
		String args[] = {"--date-price=USD", "--gold-average=2017-11-05"};
		ArgumentParser par = new ArgumentParser(args);
		assertEquals("--date-price=USD", par.getArgs()[0]);
		assertNotEquals("--DATE-price=USD", par.getArgs()[1]);
		assertNotEquals("--DATE-PRICE=USD", par.getArgs()[0]);
		assertNotEquals("--DATE-PRICE=USD", par.getArgs()[0]);
	}

	@Test
	void testGetArgs() {
		String args[] = {"--gold-average=2017-11-05", "--date-price=USD"};
		ArgumentParser par = new ArgumentParser(args);
		assertEquals("--date-price=USD", par.getArgs()[1]);
		assertNotEquals("--dateprice=USD", par.getArgs()[1]);
		assertNotEquals("--date-price", par.getArgs()[1]);
		assertNotEquals("date-price=USD", par.getArgs()[1]);
		assertNotEquals("date-price=usd", par.getArgs()[1]);
		assertNotEquals("date-priceUSD", par.getArgs()[1]);
	}

	@Test
	void testParse() throws ParseException {
		String args[] = {};
		ArgumentParser par = new ArgumentParser(args);
		CommandLine configs = par.parse();
		assertNull(configs);
		String args2[] = {"--date-price=USD"};
		ArgumentParser par2 = new ArgumentParser(args2);
		configs = par2.parse();
		assertNotNull(configs);
		
	}

}
