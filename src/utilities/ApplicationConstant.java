/**
 * 
 */
package utilities;

/**
 * @author Rohit Gupta
 *
 */

public class ApplicationConstant {

	public static final int MANAGER_ADD_ITEM = 1;
	public static final int MANAGER_REMOVE_ITEM = 2;
	public static final int MANAGER_LIST_ITEM_AVAILABILITY = 3;
	public static final int MANAGER_EXIT = 4;

	public static final int USER_BORROW_ITEM = 1;
	public static final int USER_FIND_ITEM = 2;
	public static final int USER_RETURN_ITEM = 3;
	public static final int USER_EXCHANGE_ITEM = 4;
	public static final int USER_EXIT = 5;

	public static final String CONCORDIA_SERVER = "CON";

	public static final String MCGILL_SERVER = "MCG";

	public static final String MONTREAL_SERVER = "MON";

	public static final int UDP_SEQUENCER_PORT = 7009;
	public static final int UDP_FRONT_END_PORT = 7005;
	public static final int UDP_REPLICA_MANAGER_PORT = 7001;
	public static final int RM_PORT = 7010;
	public static final int UDP_CONCORDIA_PORT = 7777;
	public static final int UDP_MCGILL_PORT = 7778;
	public static final int UDP_MONTREAL_PORT = 7779;

	// public static final String PATH =
	// "G:\\workspace\\DistributedFinalProject\\Log Files";
	public static final String PATH = "G:\\DistributedFinalProject.git\\trunk\\Log Files";

	public static final String IP_ADDRESS_ROOHANI = "132.205.64.142";
	public static final String IP_ADDRESS_ROHIT = "132.205.46.226";
	public static final String IP_ADDRESS_NANCY = "132.205.46.167";
	public static final String IP_ADDRESS_HASTI = "132.205.46.234";
	public static final String DATA_USER_ID = "userId";
	public static final String DATA_ITEM_ID = "itemId";
	public static final String DATA_ITEM_NAME = "itemName";
	public static final String DATA_NO_OF_DAYS = "NoOfDays";
	public static final String DATA_QUANTITY = "quantity";
	public static final String DATA_NEW_ITEM_ID = "newItemId";
	public static final String DATA_OLD_ITEM_ID = "oldItemId";

	public static final String OP_ADD_ITEM = "addItem";
	public static final String OP_REMOVE_ITEM = "removeItem";
	public static final String OP_LIST_ITEM_AVAILABLILITY = "listItemAvailability";

	public static final String OP_BORROW_ITEM = "borrowItem";
	public static final String OP_RETURN_ITEM = "returnItem";
	public static final String OP_EXCHANGE_ITEM = "exchangeItem";
	public static final String OP_FIND_ITEM = "findItem";

}
