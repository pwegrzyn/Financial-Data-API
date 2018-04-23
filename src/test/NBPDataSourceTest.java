package test;

import static org.junit.jupiter.api.Assertions.*;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import webapi.NBPDataSource;

class NBPDataSourceTest {

	@Test
	void getDateDiffTest() {
		NBPDataSource source = NBPDataSource.getInstance();
		assertEquals(10,(int) source.getDateDiff("2017-11-10", "2017-11-20", TimeUnit.DAYS));
		assertNotEquals(1,(int) source.getDateDiff("2017-11-10", "2017-11-12", TimeUnit.DAYS));
		assertNotEquals(0,(int) source.getDateDiff("2017-11-10", "2017-11-11", TimeUnit.DAYS));
		assertEquals(0,(int) source.getDateDiff("2017-11-10", "2017-11-10", TimeUnit.DAYS));
		assertNotEquals(1,(int) source.getDateDiff("2017-11-11", "2017-11-10", TimeUnit.DAYS));
		assertNotEquals(1,(int) source.getDateDiff("2017-11-10", "2017-11-10", TimeUnit.DAYS));
	}
	
	@Test
	void addDaysTest() {
		NBPDataSource source = NBPDataSource.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		assertEquals("2017-11-13", formatter.format(source.addDays("2017-11-04", 9)));
		assertEquals("2017-12-02", formatter.format(source.addDays("2017-12-01", 1)));
		assertEquals("2017-11-04", formatter.format(source.addDays("2017-11-04", 0)));
		assertNotEquals("2017-11-12", formatter.format(source.addDays("2017-11-04", 9)));
		assertNotEquals("2017-11-14", formatter.format(source.addDays("2017-11-04", 9)));
		assertNotEquals("2017-11-04", formatter.format(source.addDays("2017-11-04", 9)));
	}
	
	@Test
	void miscTest() {
		NBPDataSource source = NBPDataSource.getInstance();
		assertEquals("http://api.nbp.pl/api/", source.getUrlStart());
		assertNotEquals("http://api.nbp.pl/api", source.getUrlStart());
		assertNotEquals("HTTP://api.nbp.pl/api/", source.getUrlStart());
		assertEquals(367, source.getMaxPeriodPerQuery());
		assertNotEquals(366, source.getMaxPeriodPerQuery());
		assertNotEquals(368, source.getMaxPeriodPerQuery());
	}

}
