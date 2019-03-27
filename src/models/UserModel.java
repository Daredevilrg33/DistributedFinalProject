/**
 * 
 */
package models;

import java.util.ArrayList;
import java.util.List;

import utilities.ApplicationConstant;

/**
 * @author Rohit Gupta
 *
 */
public class UserModel {

	private String userId;
	private List<String> itemList;
	private UserType userType;
	private ServerType serverType;

	/**
	 * @param userId
	 */
	public UserModel(String userId) {
		// TODO Auto-generated constructor stub
		super();
		this.userId = userId;
		this.itemList = new ArrayList<>();
		computeUserTypeAndServerType(this.userId);
	}

	/**
	 * @param userId
	 * @param itemList
	 */
	public UserModel(String userId, List<String> itemList) {
		super();
		this.userId = userId;
		this.itemList = itemList;
		computeUserTypeAndServerType(this.userId);
	}

	/**
	 * @param userId2
	 */
	private void computeUserTypeAndServerType(String userId) {
		// TODO Auto-generated method stub
		String server = userId.substring(0, 3);
		if (server.equalsIgnoreCase(ApplicationConstant.CONCORDIA_SERVER))
			setServerType(ServerType.CONCORDIA);
		else if (server.equalsIgnoreCase(ApplicationConstant.MCGILL_SERVER))
			setServerType(ServerType.MCGILL);
		else
			setServerType(ServerType.MONTREAL);
		char user = userId.charAt(3);
		if (user == 'm' || user == 'M')
			setUserType(UserType.MANAGER);
		else
			setUserType(UserType.USER);
	}

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * @return the itemList
	 */
	public List<String> getItemList() {
		return itemList;
	}

	/**
	 * @param itemList the itemList to set
	 */
	public void setItemList(List<String> itemList) {
		this.itemList = itemList;
	}

	/**
	 * @param userType the userType to set
	 */
	public void setUserType(UserType userType) {
		this.userType = userType;
	}

	/**
	 * @param serverType the serverType to set
	 */
	public void setServerType(ServerType serverType) {
		this.serverType = serverType;
	}

	/**
	 * @return the userType
	 */
	public UserType getUserType() {
		return userType;
	}

	/**
	 * @return the serverType
	 */
	public ServerType getServerType() {
		return serverType;
	}

	public boolean addItem(String itemModel) {
		return itemList.add(itemModel);
	}

	public boolean removeItem(String itemModel) {
		return itemList.remove(itemModel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UserModel [userId=" + userId + ", itemList=" + itemList + "]";
	}

}
