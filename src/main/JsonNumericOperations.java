package webapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;

/**
 * This class holds all the helper methods which are used for performing numerical and statistical operations
 * on JSON objects, every method in this class is kind of generic (it is not resticted to a particular Json Object, rather
 * it can receive special parameters which allow it to function on any sort of Json elements), this means in can be used later,
 * when one will desire to extend the functionality of a system to handle more sources of JSON objects.
 * Uses the singleton design pattern.
 * @author Patryk Wegrzyn
 */
public class JsonNumericOperations {
	
	//Singleton Design Pattern
	/**
	 * Static field, which holds the only instance of this class during the lifetime of an application run
	 */
	private static JsonNumericOperations firstInstance = null;
		
	/**
	 * Private constructor because we only allow to create this object be using the getInstance method
	 */
	private JsonNumericOperations() {}
		
	/**
	 * Static method used as a factory for this class, if an object of this class already exists - it returns it, 
	 * otherwise it returns a newly created object and saves it for further requests. This way only one instance of this class
	 * will be present at any given time. The essence of the Singleton Design Pattern
	 * @return The only available instance of this class
	 */
	public static JsonNumericOperations getInstance() {
			
		if(firstInstance == null) {
				
			firstInstance = new JsonNumericOperations();
				
		}
			
		return firstInstance;
	}
	
	/**
	 * Calculates the sum of a particular field in a JsonArray object
	 * @param array the JsonArray object which represents an array in a real JSON file
	 * @param field name of field which this method is supposed to sum
	 * @return the calculated sum of all fields with the provided name
	 */
	public double getSumOfArr(JsonArray array, String field) {
		double sum = 0;
		JsonObject object;
		for(int i=0; i<array.size(); i++) {
			object = array.getJsonObject(i);
			sum += object.getJsonNumber(field).doubleValue();
		}
		return sum;
	}
	
	/**
	 * Calculates the average of a particular field in a JsonArray object
	 * @param array the JsonArray object which represents an array in a real JSON file
	 * @param field name of field which this method is supposed to get the average of
	 * @return the calculated average of all fields with the provided name
	 */
	public double getAvgOfArr(JsonArray array, String field) {
		return getSumOfArr(array, field) / array.size();
	}
	
	/**
	 * Finds the the biggest fluctuation in a particular numeric filed in a Json Object, alongside with all the additional
	 * information about the found object, is parametric which means it can be used to find any sort of fluctuations
	 * @param outerArray The outer array of objects which represents all the data sets which are supposed to be checked
	 * @param field1 First parametric field name
	 * @param field2 Second parametric field name
	 * @param field3 Third parametric field name
	 * @param field4 Fourth parametric field name
	 * @return Map, containing the found object alongside with more usefull information about the found object
	 */
	public Map<String, Object> findMaxAmpOfArr(JsonArray outerArray, String field1, String field2, String field3, String field4 ) {
		
		JsonArray innerArray;
		JsonObject arrayObject;
		JsonObject innerObject;
		Map<String,Double> min = new HashMap<>();
		Map<String,Double> max = new HashMap<>();
		Map<String,String> whenMin = new HashMap<>();
		Map<String,String> whenMax = new HashMap<>();
		double maxAmp = Double.MIN_VALUE;
		double minVal = Double.MAX_VALUE;
		double maxVal = Double.MIN_VALUE;
		String whichObject = "";
		String whenMinVal = "";
		String whenMaxVal = "";
		
		arrayObject = outerArray.getJsonObject(0);
		innerArray = arrayObject.getJsonArray(field1);
		for(int i=0; i<innerArray.size(); i++) {
			arrayObject = innerArray.getJsonObject(i);
			min.put(arrayObject.getString(field2), Double.MAX_VALUE);
			max.put(arrayObject.getString(field2), Double.MIN_VALUE);
			whenMin.put(arrayObject.getString(field2), "");
			whenMax.put(arrayObject.getString(field2), "");
		}
		for(int i=0; i<outerArray.size(); i++) {
			arrayObject = outerArray.getJsonObject(i);
			innerArray = arrayObject.getJsonArray(field1);
			for(int j=0; j<innerArray.size(); j++) {
				innerObject = innerArray.getJsonObject(j);
				if(min.get(innerObject.getString(field2)) > innerObject.getJsonNumber(field3).doubleValue()) {
					min.put(innerObject.getString(field2), innerObject.getJsonNumber(field3).doubleValue());
					whenMin.put(innerObject.getString(field2), arrayObject.getString(field4));
				}
				if(max.get(innerObject.getString(field2)) < innerObject.getJsonNumber(field3).doubleValue()) {
					max.put(innerObject.getString(field2), innerObject.getJsonNumber(field3).doubleValue());
					whenMax.put(innerObject.getString(field2), arrayObject.getString(field4));
				}
			}
		}
		arrayObject = outerArray.getJsonObject(0);
		innerArray = arrayObject.getJsonArray(field1);
		for(int i=0; i<innerArray.size(); i++) {
			arrayObject = innerArray.getJsonObject(i);
			if(max.get(arrayObject.getString(field2)) - min.get(arrayObject.getString(field2)) > maxAmp) {
				maxAmp = max.get(arrayObject.getString(field2)) - min.get(arrayObject.getString(field2));
				whichObject = arrayObject.getString(field2);
				whenMinVal = whenMin.get(arrayObject.getString(field2));
				whenMaxVal = whenMax.get(arrayObject.getString(field2));
				minVal = min.get(arrayObject.getString(field2));
				maxVal = max.get(arrayObject.getString(field2));
			}
		}
		
		Map<String, Object> result = new HashMap<>();
		result.put("object", whichObject);
		result.put("amplitude", maxAmp);
		result.put("whenMin", whenMinVal);
		result.put("whenMax", whenMaxVal);
		result.put("min", minVal);
		result.put("max", maxVal);
		return result;
	}
	
	/**
	 * For a given Json Array this methods is able to generate value arrays holding minimal and maximal values of a particular field for every object in the inner array,
	 * is parametric in the same way as findMaxAmpOfArr
	 * @param outerArray The outer array of objects which represents all the data sets which are supposed to be checked
	 * @param field1 First parametric field name
	 * @param field2 Second parametric field name
	 * @param field3 Third parametric field name
	 * @param field4 Fourth parametric field name
	 * @return Map, containing the maps of mins and maxs for all objects alongside with other usefull information
	 */
	public Map<String, Map<String, Object>> getMinMaxArrays(JsonArray outerArray, String field1, String field2, String field3, String field4) {
		JsonArray innerArray;
		JsonObject arrayObject;
		JsonObject innerObject;
		Map<String,Object> min = new HashMap<>();
		Map<String,Object> max = new HashMap<>();
		Map<String,Object> whenMin = new HashMap<>();
		Map<String,Object> whenMax = new HashMap<>();
		
		for(int i=0; i<outerArray.size(); i++) {
			arrayObject = outerArray.getJsonObject(i);
			innerArray = arrayObject.getJsonArray(field1);
			for(int j=0; j<innerArray.size(); j++) {
				innerObject = innerArray.getJsonObject(j);
				if(!min.containsKey(innerObject.getString(field2))) {
					min.put(innerObject.getString(field2), Double.MAX_VALUE);
					max.put(innerObject.getString(field2), Double.MIN_VALUE);
					whenMin.put(innerObject.getString(field2), "");
					whenMax.put(innerObject.getString(field2), "");
				}
				if((Double)(min.get(innerObject.getString(field2))) > innerObject.getJsonNumber(field3).doubleValue()) {
					min.put(innerObject.getString(field2), (innerObject.getJsonNumber(field3).doubleValue()));
					whenMin.put(innerObject.getString(field2), arrayObject.getString(field4));
				}
				if((Double)(max.get(innerObject.getString(field2))) < innerObject.getJsonNumber(field3).doubleValue()) {
					max.put(innerObject.getString(field2), (innerObject.getJsonNumber(field3).doubleValue()));
					whenMax.put(innerObject.getString(field2), arrayObject.getString(field4));
				}
			}
		}
		
		Map<String, Map<String, Object>> result = new HashMap<>();
		result.put("min", min);
		result.put("max", max);
		result.put("whenMin", whenMin);
		result.put("whenMax", whenMax);
		return result;
	}
	
	/**
	 * Simple numeric method, finds the object with the minimal value in a particular field in a Json Array
	 * @param array The Json Array in which we will be looking for a minimum
	 * @param value The name of field used to finding the minimum
	 * @param name Name of the filed which is supposed to be also returned for the minimal field object
	 * @return The map of the found minimum and one additional field value in the found object
	 */
	public Map<String, Object> getMinOfArr(JsonArray array, String value, String name) {
		double min = Double.MAX_VALUE;
		String foundName = "";
		JsonObject object;
		for(int i=0; i<array.size(); i++) {
			object = array.getJsonObject(i);
			if(object.getJsonNumber(value).doubleValue() < min) {
				min = object.getJsonNumber(value).doubleValue();
				foundName = object.getString(name);
			}
		}
		Map<String, Object> result = new HashMap<>();
		result.put("name", foundName);
		result.put("min", min);
		return result;
	}
	
	/**
	 * Works almost the same ways as getMinOfArr, only this one returns the full found JsonObject
	 * @param array The Json Array in which we will be looking for a minimum
	 * @param value The name of field used to finding the minimum
	 * @return The found JsonObject with the minimal value in the given field
	 */
	public JsonObject getMinOfArrJsonObj(JsonArray array, String value) {
		double min = Double.MAX_VALUE;
		JsonObject object, result = null;
		for(int i=0; i<array.size(); i++) {
			object = array.getJsonObject(i);
			if(object.getJsonNumber(value).doubleValue() < min) {
				min = object.getJsonNumber(value).doubleValue();
				result = object;
			}
		}
		return result;
	}
	
	/**
	 * Analogous method to getMinOfArrJsonObj, only this one finds the maximum
	 * @param array The Json Array in which we will be looking for a maximum
	 * @param value The name of field used to finding the maximum
	 * @return The found JsonObject with the maximal value in the given field
	 */
	public JsonObject getMaxOfArrJsonObj(JsonArray array, String value) {
		double max = Double.MIN_VALUE;
		JsonObject object, result = null;
		for(int i=0; i<array.size(); i++) {
			object = array.getJsonObject(i);
			if(object.getJsonNumber(value).doubleValue() > max) {
				max = object.getJsonNumber(value).doubleValue();
				result = object;
			}
		}
		return result;
	}
	
	/**
	 * Simple numeric method, finds the N Json Objects in a Json Array with the biggest difference in two
	 * given fields, returns them as list of JsonObject objects
	 * @param array The Json Array in which we will be looking for the desired objects
	 * @param n Number of objects to be returned
	 * @param value1 Name of the first field in question
	 * @param value2 Name of the second field in question
	 * @return The list of N found Json Objects with the biggest difference in the two given fields
	 */
	public List<JsonObject> getNsortedByDiff(JsonArray array, int n, String value1, String value2) {
		ArrayList<JsonObject> myList = new ArrayList<>();
		for(int i=0; i<array.size(); i++) {
			myList.add(array.getJsonObject(i));
		}
		Collections.sort(myList, new Comparator<JsonObject>() {
			@Override
			public int compare(JsonObject o1, JsonObject o2) {
				double diff1 = o1.getJsonNumber(value1).doubleValue() - o1.getJsonNumber(value2).doubleValue();
				double diff2 = o2.getJsonNumber(value1).doubleValue() - o2.getJsonNumber(value2).doubleValue();
				if (diff2 - diff1 > 0) return 1;
				else if (diff2 - diff1 < 0) return -1;
				else return 0;
			}
		});
		return (n <= myList.size()) ? myList.subList(0, n) : myList;
	}

}
