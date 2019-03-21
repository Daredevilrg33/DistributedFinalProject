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
	public boolean addItem(String managerId, String itemId, String itemName, int quantity);

	public boolean removeItem(String managerId, String itemId, int quantity);

	public String listItemAvailability(String managerId);

	public String borrowItem(String userId, String itemId, int noOfDays);

	public String findItem(String userId, String itemName);

	public boolean returnItem(String userId, String itemId);

	public boolean exchangeItem(String userId, String newItemId, String oldItemId);
}
