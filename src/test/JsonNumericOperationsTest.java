package test;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.jupiter.api.Test;

import webapi.JsonNumericOperations;

class JsonNumericOperationsTest {

	@Test
	void getSumOfArrTest() {
		try {
			
			JsonNumericOperations ops = JsonNumericOperations.getInstance();
			File file = new File("C:\\Users\\Patryk\\eclipse-workspace\\WebAPI\\test1.json");
			InputStream myStream = new FileInputStream(file);
			JsonReader reader = Json.createReader(myStream);
			JsonArray array = reader.readArray();
			reader.close();
			myStream.close();
			
			assertNotNull(ops.getAvgOfArr(array, "cena"));
			assertNull(null);
			assertNotEquals(149.9, ops.getAvgOfArr(array, "cena"));
			assertNotEquals(149.8, ops.getAvgOfArr(array, "cena"));
			assertNotEquals(150.0, ops.getAvgOfArr(array, "cena"));
			assertNotEquals(150, ops.getAvgOfArr(array, "cena"));
			assertNotEquals(148, ops.getAvgOfArr(array, "cena"));
			assertNotEquals(148.0, ops.getAvgOfArr(array, "cena"));
			assertNotEquals(149.0, ops.getAvgOfArr(array, "cena"));
			assertNotEquals(149.90, ops.getAvgOfArr(array, "cena"));
			assertNotEquals(149.961, ops.getAvgOfArr(array, "cena"));
			assertEquals(149.96, ops.getAvgOfArr(array, "cena"));
			assertNotEquals(149.959, ops.getAvgOfArr(array, "cena"));
			assertNotEquals(0, ops.getAvgOfArr(array, "cena"));
			assertNotEquals(149.9601, ops.getAvgOfArr(array, "cena"));
			assertNotNull(ops.getSumOfArr(array, "cena"));
			assertNull(null);
			assertNotEquals(449.8, ops.getSumOfArr(array, "cena"));
			assertNotEquals(449.87, ops.getSumOfArr(array, "cena"));
			assertNotEquals(449.8801, ops.getSumOfArr(array, "cena"));
			assertNotEquals(449.879, ops.getSumOfArr(array, "cena"));
			assertNotEquals(449.8791, ops.getSumOfArr(array, "cena"));
			assertNotEquals(449.0, ops.getSumOfArr(array, "cena"));
			assertNotEquals(445.0, ops.getSumOfArr(array, "cena"));
			assertNotEquals(449.1, ops.getSumOfArr(array, "cena"));
			assertNotEquals(448.88, ops.getSumOfArr(array, "cena"));
			assertNotEquals(450.88, ops.getSumOfArr(array, "cena"));
			assertNotEquals(0, ops.getSumOfArr(array, "cena"));
			assertEquals(449.88, ops.getSumOfArr(array, "cena"));
			Map<String, Object> map1 = ops.getMinOfArr(array, "cena", "data");
			assertNotNull(map1);
			assertNull(null);
			assertTrue(((String)map1.get("name")).equals("2017-10-18"));
			assertFalse(((String)map1.get("name")).equals("2017--10-1"));
			assertFalse(((String)map1.get("name")).equals("17-10-1"));
			assertFalse(((String)map1.get("name")).equals("2017-10-01"));
			assertFalse(((String)map1.get("name")).equals("2017-1-18"));
			assertFalse(((String)map1.get("name")).equals("17--10-18"));
			assertFalse(((String)map1.get("name")).equals("2017-10-19"));
			assertFalse(((String)map1.get("name")).equals("2018-10-18"));
			assertFalse(((String)map1.get("name")).equals("2017-11-18"));
			assertFalse(((String)map1.get("name")).equals("2017-09-18"));
			assertTrue(((double)map1.get("min")) != 148.58);
			assertTrue(((double)map1.get("min")) != 148.59);
			assertTrue(((double)map1.get("min")) != 148.50);
			assertTrue(((double)map1.get("min")) != 148.5);
			assertTrue(((double)map1.get("min")) != 148);
			assertTrue(((double)map1.get("min")) != 0);
			assertFalse(((double)map1.get("min")) == 148.58);
			assertTrue(((double)map1.get("min")) != 148.577);
			assertTrue(((double)map1.get("min")) == 148.57);
			JsonObject val = ops.getMinOfArrJsonObj(array, "cena");
			assertNotNull(val);
			assertNull(null);
			assertEquals(array.get(2), val);
			assertNotEquals(array.get(1), val);
			assertNotEquals(array.get(0), val);
			val = ops.getMaxOfArrJsonObj(array, "cena");
			assertNotNull(val);
			assertNull(null);
			assertEquals(array.get(1), val);
			assertNotEquals(array.get(2), val);
			assertNotEquals(array.get(0), val);
			file = new File("C:\\Users\\Patryk\\eclipse-workspace\\WebAPI\\test2.json");
			myStream = new FileInputStream(file);
			reader = Json.createReader(myStream);
			array = reader.readArray();
			reader.close();
			myStream.close();
			Map<String, Object> result = ops.findMaxAmpOfArr(array, "rates", "code", "mid", "effectiveDate");
			assertFalse(((String) result.get("object")).equals("gbp"));
			assertFalse(((String) result.get("object")).equals(" gbp "));
			assertFalse(((String) result.get("object")).equals(" GBP "));
			assertFalse(((String) result.get("object")).equals(""));
			assertTrue(((String) result.get("object")).equals("GBP"));
			assertNotEquals(result.get("min"), 4.7);
			assertNotEquals(result.get("min"), 4.69999);
			assertNotEquals(result.get("min"), 4.6);
			assertNotEquals(result.get("min"), 4.0);
			assertNotEquals(result.get("min"), 4);
			assertEquals(result.get("min"), 4.699);
			assertFalse(((String) result.get("whenMin")).equals("2018-01-8"));
			assertFalse(((String) result.get("whenMin")).equals("2018-1-8"));
			assertFalse(((String) result.get("whenMin")).equals("2018-1-08"));
			assertFalse(((String) result.get("whenMin")).equals(""));
			assertTrue(((String) result.get("whenMin")).equals("2018-01-08"));
			assertNotEquals(result.get("max"), 4.73);
			assertNotEquals(result.get("max"), 4.7);
			assertNotEquals(result.get("max"), 4);
			assertNotEquals(result.get("max"), 4.70);
			assertNotEquals(result.get("max"), 4.734701);
			assertEquals(result.get("max"), 4.7347);
			assertFalse(((String) result.get("whenMax")).equals("2018-01-9"));
			assertFalse(((String) result.get("whenMax")).equals("2018-1-9"));
			assertFalse(((String) result.get("whenMax")).equals("2018-1-09"));
			assertFalse(((String) result.get("whenMax")).equals(""));
			assertTrue(((String) result.get("whenMax")).equals("2018-01-09"));
			assertNotEquals(((double) result.get("amplitude")), 0.0357);
			assertNotEquals(((double) result.get("amplitude")), 0.03570000000000028);
			assertNotEquals(((double) result.get("amplitude")), 0.03570000000000030);
			assertNotEquals(((double) result.get("amplitude")), 0.035);
			assertNotEquals(((double) result.get("amplitude")), 0.03);
			assertNotEquals(((double) result.get("amplitude")), 0);
			assertEquals(((double) result.get("amplitude")), 0.03570000000000029);
			
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
