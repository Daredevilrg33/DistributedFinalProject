/**
 * 
 */
package models;

import java.io.Serializable;

/**
 * @author Rohit Gupta
 *
 */
public class ItemModel implements Serializable {

	private String itemId;
	private String itemName;
	private int quantity;

	/**
	 * @param itemId
	 * @param itemName
	 * @param quantity
	 */
	public ItemModel(String itemId, String itemName, int quantity) {
		super();
		this.itemId = itemId;
		this.itemName = itemName;
		this.quantity = quantity;
	}

	/**
	 * @return the itemId
	 */
	public String getItemId() {
		return itemId;
	}

	/**
	 * @param itemId the itemId to set
	 */
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	/**
	 * @return the itemName
	 */
	public String getItemName() {
		return itemName;
	}

	/**
	 * @param itemName the itemName to set
	 */
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	/**
	 * @return the quantity
	 */
	public int getQuantity() {
		return quantity;
	}

	/**
	 * @param quantity the quantity to set
	 */
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ItemModel [itemId=" + itemId + ", itemName=" + itemName + ", quantity=" + quantity + "]";
	}
}
