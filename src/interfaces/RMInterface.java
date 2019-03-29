/**
 * 
 */
package interfaces;

import java.util.HashMap;
import java.util.List;

import models.ItemModel;

/**
 * @author Rohit Gupta
 *
 */
public interface RMInterface {
	public String addItem(String managerId, String itemId, String itemName, int quantity);

	public String removeItem(String managerId, String itemId, int quantity);

	public String listItemAvailability(String managerId);

	public String borrowItem(String userId, String itemId, int noOfDays);

	public String findItem(String userId, String itemName);

	public String returnItem(String userId, String itemId);

	public String exchangeItem(String userId, String newItemId, String oldItemId);
}
