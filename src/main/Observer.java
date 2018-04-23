package webapi;


/**
 * Interface, which is the observer part of the observer design pattern, which itself is used to dynamically update DataSources of Orders
 * @author Patryk Wegrzyn
 *
 */
public interface Observer {

	/**
	 * Updates the DataSource
	 * @param dataSource the new data source
	 */
	public void update(AbstractDataSource dataSource);
	
}
