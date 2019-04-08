/**
 * 
 */
package replicamanagers;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import interfaces.RMInterface;
import models.ItemModel;
import models.ServerType;
import models.UserModel;
import utilities.ApplicationConstant;
import utilities.Utility;

/**
 * @author Rohit Gupta
 *
 */
public class ReplicaManagerImplementation implements RMInterface {

	private HashMap<String, ItemModel> itemHashMap;
	private HashMap<String, Queue<String>> waitList;
	private ServerType currentServer;
	public Logger logger = Logger.getLogger(ReplicaManagerImplementation.class.getName());
	private HashMap<String, UserModel> userHashMap;

	/**
	 * 
	 */
	public ReplicaManagerImplementation(ServerType serverType) {
		// TODO Auto-generated constructor stub
		itemHashMap = new HashMap<>();
		userHashMap = new HashMap<>();
		waitList = new HashMap<>();
		currentServer = serverType;
		Utility.log("Starting " + serverType + " server.", logger);
		dataSet(currentServer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.RMInterface#addItem(java.lang.String, java.lang.String,
	 * java.lang.String, int)
	 */
	@Override
	public synchronized String addItem(String managerId, String itemId, String itemName, int quantity) {
		// TODO Auto-generated method stub
		String reply = "";
		Utility.log("Accessing Add Item. ", logger);
		ItemModel itemModel = new ItemModel(itemId, itemName, quantity);
		UserModel userModel = userHashMap.get(managerId);
		if (userModel != null)
			reply = addItemToHashMap(itemModel);
		return reply;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.RMInterface#removeItem(java.lang.String, java.lang.String,
	 * int)
	 */
	@Override
	public synchronized String removeItem(String managerId, String itemId, int quantity) {
		// TODO Auto-generated method stub
		String reply = "";
		Utility.log("Accessing Remove Item.", logger);
		UserModel userModel = userHashMap.get(managerId);
		if (quantity < 0) {
			Utility.log("Quantity is less than zero.", logger);
			if (itemHashMap.containsKey(itemId)) {
				ItemModel itemModel = itemHashMap.remove(itemId);
				for (String key : userHashMap.keySet()) {
					UserModel userModel1 = userHashMap.get(key);
					userModel1.removeItem(itemModel.getItemId());
					userHashMap.put(userModel1.getUserId(), userModel1);
				}
				Utility.log("Item has been removed successfully !!", logger);
				reply = ApplicationConstant.MSG_REMOVE_ITEM;

			} else
				reply = ApplicationConstant.MSG_ITEM_RETURNED_DOESNOT_EXIST;
		} else {
			Utility.log("Quantity is greater than zero.", logger);
			if (itemHashMap.containsKey(itemId)) {
				ItemModel itemModel = itemHashMap.get(itemId);
				int oldQuantity = itemModel.getQuantity();
				if (oldQuantity < quantity) {
					Utility.log("Item quantity cannot be reduced.", logger);
					reply = ApplicationConstant.MSG_REMOVE_ITEM_INSUFFICIENT_QUANTITY;
				} else {
					Utility.log("Old quantity of the item: " + oldQuantity, logger);
					Utility.log("Quantity to be reduce : " + quantity, logger);
					int value = oldQuantity - quantity;
					itemModel.setQuantity(value);
					Utility.log("Item quantity has been reduced to : " + value, logger);
					reply = ApplicationConstant.MSG_REMOVE_ITEM_DECREASED_QUANTITY;
				}
			} else
				reply = ApplicationConstant.MSG_ITEM_RETURNED_DOESNOT_EXIST;
		}
		return reply;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.RMInterface#listItemAvailability(java.lang.String)
	 */
	@Override
	public String listItemAvailability(String managerId) {
		// TODO Auto-generated method stub
		UserModel userModel = userHashMap.get(managerId);
		if (userModel != null) {
			return parseData(itemHashMap).toString();
		} else
			return ApplicationConstant.MSG_NO_ITEMS_AVAILABLE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.RMInterface#borrowItem(java.lang.String, java.lang.String,
	 * int)
	 */
	@Override
	public synchronized String borrowItem(String userId, String itemId, int noOfDays) {
		// TODO Auto-generated method stub
		UserModel userModel = userHashMap.get(userId);
		String replyMessage = "";
		ItemModel itemModel = itemHashMap.get(itemId);
		if (itemModel != null) {
			if (itemModel.getQuantity() == 0) {
				System.out.println("Item is unavalilable. Would you like to be on waitlist (y/n) ?");
				Scanner scanner = new Scanner(System.in);
				String value = scanner.nextLine();
				if (value.trim().equalsIgnoreCase("y")) {
					if (waitList.containsKey(itemId)) {
						Queue<String> userQueue = waitList.get(itemId);
						userQueue.add(userId);
						waitList.put(itemId, userQueue);
					} else {
						Queue<String> userQueue1 = new PriorityQueue<>();
						userQueue1.add(userModel.getUserId());
						waitList.put(itemId, userQueue1);
					}
					replyMessage = ApplicationConstant.MSG_USER_ADDED_TO_WAITLIST;

				} else {
					replyMessage = ApplicationConstant.MSG_USER_WAITLIST_OPT_OUT;
				}
			} else {
				if (userModel != null) {
					if (userModel.getItemList().contains(itemModel.getItemId())) {
						replyMessage = ApplicationConstant.MSG_BORROW_USER_ALREADY_HAS_ITEM;
					} else {
						int quantity = itemModel.getQuantity() - 1;
						itemModel.setQuantity(quantity);
						itemHashMap.put(itemModel.getItemId(), itemModel);
						userModel.addItem(itemModel.getItemId());
						userHashMap.put(userModel.getUserId(), userModel);
						replyMessage = ApplicationConstant.MSG_BORROW_ITEM_SUCCESSFULLY;
					}
				}

			}
		} else {
			if (isUserAllowedToBorrow(userModel, itemId)) {
				// Make UDP Request to the servers.
				String itemValue = itemId.substring(0, 3);
				if (itemValue.equalsIgnoreCase(ApplicationConstant.CONCORDIA_SERVER)) {
					replyMessage = sendUDPRequest(ApplicationConstant.UDP_CON_SERVER,
							"Borrow" + "," + userId + "," + itemId + "," + noOfDays + "," + true);
				} else if (itemValue.equalsIgnoreCase(ApplicationConstant.MCGILL_SERVER)) {
					replyMessage = sendUDPRequest(ApplicationConstant.UDP_MCG_SERVER,
							"Borrow" + "," + userId + "," + itemId + "," + noOfDays + "," + true);
				} else {
					replyMessage = sendUDPRequest(ApplicationConstant.UDP_MON_SERVER,
							"Borrow" + "," + userId + "," + itemId + "," + noOfDays + "," + true);
				}
				if (replyMessage.trim().equalsIgnoreCase("User has borrowed successfully !!")) {
					UserModel user = userHashMap.get(userId);
					user.addItem(itemId);

					userHashMap.put(user.getUserId(), user);
				}
			} else {
				replyMessage = ApplicationConstant.MSG_BORROW_USER_NOT_ALLOWED_TO_BORROW;
			}
		}
		return replyMessage;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.RMInterface#findItem(java.lang.String, java.lang.String)
	 */
	@Override
	public synchronized String findItem(String userId, String itemName) {
		// TODO Auto-generated method stub
		List<ItemModel> itemModels = new ArrayList<>();
		for (String key : itemHashMap.keySet()) {
			ItemModel itemModel = itemHashMap.get(key);
			if (itemModel.getItemName().trim().equalsIgnoreCase(itemName.trim())) {
				itemModels.add(itemModel);
			}
		}
		// Make UDP Request to both the servers.
		if (currentServer == ServerType.CONCORDIA) {
			String reply1 = sendUDPRequest(ApplicationConstant.UDP_MCG_SERVER,
					"Find Item" + "," + userId + "," + itemName).trim();
			itemModels.addAll(fetchItemsFromReply(reply1));
			String reply2 = sendUDPRequest(ApplicationConstant.UDP_MON_SERVER,
					"Find Item" + "," + userId + "," + itemName).trim();
			itemModels.addAll(fetchItemsFromReply(reply2));
		} else if (currentServer == ServerType.MCGILL) {
			String reply3 = sendUDPRequest(ApplicationConstant.UDP_CON_SERVER,
					"Find Item" + "," + userId + "," + itemName).trim();
			itemModels.addAll(fetchItemsFromReply(reply3));
			String reply4 = sendUDPRequest(ApplicationConstant.UDP_MON_SERVER,
					"Find Item" + "," + userId + "," + itemName).trim();
			itemModels.addAll(fetchItemsFromReply(reply4));
		} else {
			String reply5 = sendUDPRequest(ApplicationConstant.UDP_CON_SERVER,
					"Find Item" + "," + userId + "," + itemName).trim();
			itemModels.addAll(fetchItemsFromReply(reply5));
			String reply6 = sendUDPRequest(ApplicationConstant.UDP_MCG_SERVER,
					"Find Item" + "," + userId + "," + itemName).trim();
			itemModels.addAll(fetchItemsFromReply(reply6));
		}
		if (itemModels.size() > 0) {
			return parseData(itemModels).toString();
		} else
			return ApplicationConstant.MSG_NO_ITEMS_AVAILABLE;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.RMInterface#returnItem(java.lang.String, java.lang.String)
	 */
	@Override
	public synchronized String returnItem(String userId, String itemId) {
		// TODO Auto-generated method stub
		String replyMessage = "";
		UserModel user = userHashMap.get(userId);
		if (user != null && user.getItemList().contains(itemId)) {
			if (itemHashMap.containsKey(itemId)) {
				boolean isItemReturned = returnItemAndAssignToWaitListUser(userId, itemId);
				if (isItemReturned)
					replyMessage = ApplicationConstant.MSG_ITEM_RETURNED_SUCCESSFULLY;
				else
					replyMessage = ApplicationConstant.MSG_ITEM_RETURNED_DOESNOT_EXIST;
			} else {
				String str = itemId.substring(0, 3);
				String reply = "";
				if (str.equalsIgnoreCase("CON")) {
					reply = sendUDPRequest(ApplicationConstant.UDP_CON_SERVER,
							"Return Item" + "," + userId + "," + itemId).trim();
				} else if (str.equalsIgnoreCase("MCG")) {
					reply = sendUDPRequest(ApplicationConstant.UDP_MCG_SERVER,
							"Return Item" + "," + userId + "," + itemId).trim();
				} else
					reply = sendUDPRequest(ApplicationConstant.UDP_MON_SERVER,
							"Return Item" + "," + userId + "," + itemId).trim();

				if (reply.trim().equalsIgnoreCase("successful")) {
					UserModel user1 = userHashMap.get(userId);
					user1.removeItem(itemId);
					userHashMap.put(user1.getUserId(), user1);
					replyMessage = ApplicationConstant.MSG_ITEM_RETURNED_SUCCESSFULLY;

				} else {
					replyMessage = ApplicationConstant.MSG_ITEM_RETURNED_DOESNOT_EXIST;

				}
			}
		} else
			replyMessage = ApplicationConstant.MSG_BORROW_ITEM_NOT_BORROWED;
		return replyMessage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.RMInterface#exchangeItem(java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public synchronized String exchangeItem(String userId, String newItemId, String oldItemId) {
		// TODO Auto-generated method stub
		String replyMessage = "";

		int noOfDays = 4;
		UserModel userModel = userHashMap.get(userId);
		String newItemValue = newItemId.substring(0, 3);
		if (userModel.getItemList().contains(oldItemId)) {
			ItemModel itemModel = itemHashMap.get(newItemId);
			if (itemModel != null) {
				if (itemModel.getQuantity() > 0) {
					int quantity = itemModel.getQuantity();
					quantity--;
					itemModel.setQuantity(quantity);
					userModel.addItem(itemModel.getItemId());
					itemHashMap.put(itemModel.getItemId(), itemModel);
					userHashMap.put(userModel.getUserId(), userModel);

					String itemReturned = returnItem(userId, oldItemId);
					if (itemReturned.equalsIgnoreCase("Item has been returned successfully"))
						replyMessage = ApplicationConstant.MSG_ITEM_EXCHANGE_SUCCESSFULLY;
					else
						replyMessage = ApplicationConstant.MSG_ITEM_EXCHANGE_UNSUCCESSFUL;
				}
			} else {
				if (newItemValue.equalsIgnoreCase(ApplicationConstant.CONCORDIA_SERVER)) {
					replyMessage = sendUDPRequest(ApplicationConstant.UDP_CON_SERVER,
							"Borrow" + "," + userId + "," + newItemId + "," + noOfDays + "," + false);
				} else if (newItemValue.equalsIgnoreCase(ApplicationConstant.MCGILL_SERVER)) {
					replyMessage = sendUDPRequest(ApplicationConstant.UDP_MCG_SERVER,
							"Borrow" + "," + userId + "," + newItemId + "," + noOfDays + "," + false);
				} else {
					replyMessage = sendUDPRequest(ApplicationConstant.UDP_MON_SERVER,
							"Borrow" + "," + userId + "," + newItemId + "," + noOfDays + "," + false);
				}
				if (replyMessage.trim().equalsIgnoreCase("User has borrowed successfully !!")) {
					UserModel user = userHashMap.get(userId);
					user.addItem(newItemId);
					userHashMap.put(user.getUserId(), user);
					String itemReturned = returnItem(userId, oldItemId);
					if (itemReturned.equalsIgnoreCase("Item has been returned successfully"))
						replyMessage = ApplicationConstant.MSG_ITEM_EXCHANGE_SUCCESSFULLY;
					else
						replyMessage = ApplicationConstant.MSG_ITEM_EXCHANGE_UNSUCCESSFUL;
				} else {
					System.out.println(replyMessage);
					replyMessage = ApplicationConstant.MSG_ITEM_EXCHANGE_UNSUCCESSFUL;
				}
			}
		} else
			replyMessage = ApplicationConstant.MSG_ITEM_EXCHANGE_UNSUCCESSFUL;
		return replyMessage;
	}

	/**
	 * @param userId
	 * @param itemId
	 */
	public synchronized boolean returnItemAndAssignToWaitListUser(String userId, String itemId) {
		// TODO Auto-generated method stub
		boolean isItemReturned = false;
		if (itemHashMap.containsKey(itemId)) {
			ItemModel itemModel = itemHashMap.get(itemId);
			int quantity = itemModel.getQuantity();
			quantity = quantity + 1;
			itemModel.setQuantity(quantity);
			itemHashMap.put(itemId, itemModel);
			UserModel userModel = userHashMap.get(userId);
			if (userModel != null) {
				userModel.removeItem(itemId);
				userHashMap.put(userModel.getUserId(), userModel);
			}
			isItemReturned = true;
			boolean isAssigned = assignItemToWaitListUser(itemModel);
			if (isAssigned) {
				int quantityValue = itemModel.getQuantity() - 1;
				itemModel.setQuantity(quantityValue);
				itemHashMap.put(itemModel.getItemId(), itemModel);
				isItemReturned = true;
			}
		} else
			isItemReturned = false;
		return isItemReturned;
	}

	/**
	 * 
	 */
	private synchronized boolean assignItemToWaitListUser(ItemModel itemModel) {
		// TODO Auto-generated method stub
		boolean isAssigned = false;
		if (waitList.size() > 0) {
			if (waitList.containsKey(itemModel.getItemId())) {
				Queue<String> userQueue = waitList.get(itemModel.getItemId());
				if (!userQueue.isEmpty()) {
					String userId = userQueue.poll();
					UserModel userModel = userHashMap.get(userId);
					if (userModel != null) {
						userModel.addItem(itemModel.getItemId());
						userHashMap.put(userModel.getUserId(), userModel);
						waitList.put(itemModel.getItemId(), userQueue);
						isAssigned = true;
					} else {
						String replyMessage;
						String userValue = userId.substring(0, 3);
						String itemId = itemModel.getItemId();
						if (userValue.equalsIgnoreCase(ApplicationConstant.CONCORDIA_SERVER)) {
							replyMessage = sendUDPRequest(ApplicationConstant.UDP_CON_SERVER,
									"Assign" + "," + userId + "," + itemId + "," + 5);
						} else if (userValue.equalsIgnoreCase(ApplicationConstant.MCGILL_SERVER)) {
							replyMessage = sendUDPRequest(ApplicationConstant.UDP_MCG_SERVER,
									"Assign" + "," + userId + "," + itemId + "," + 5);
						} else {
							replyMessage = sendUDPRequest(ApplicationConstant.UDP_MON_SERVER,
									"Assign" + "," + userId + "," + itemId + "," + 5);
						}
						if (replyMessage.trim().equalsIgnoreCase("successful")) {
							System.out.println("Wait List User has been assigned the Item of different server.");
							logger.info("Wait List User has been assigned the Item of different server.");
							isAssigned = true;
						} else
							isAssigned = false;
					}
				}
			} else
				isAssigned = false;
		}
		return isAssigned;
	}

	private synchronized String addItemToHashMap(ItemModel itemModel) {
		String returnMessage = "";
		if (itemHashMap.containsKey(itemModel.getItemId())) {
			Utility.log("Item already present in concordia hash map.", logger);
			ItemModel itemModel1 = itemHashMap.get(itemModel.getItemId());
			Utility.log("Previous Quantity: " + itemModel1.getQuantity(), logger);
			itemModel1.setQuantity(itemModel1.getQuantity() + itemModel.getQuantity());
			Utility.log("Final Quantity: " + itemModel1.getQuantity(), logger);
			boolean isAssigned = assignItemToWaitListUser(itemModel1);
			if (isAssigned) {
				int quantityValue = itemModel1.getQuantity() - 1;
				itemModel1.setQuantity(quantityValue);
			}
			itemHashMap.put(itemModel1.getItemId(), itemModel1);
			returnMessage = ApplicationConstant.MSG_ADD_ITEM_QUANTITY_UPDATED;
		} else {
			itemHashMap.put(itemModel.getItemId(), itemModel);
			Utility.log("Item successfully added to concordia hashmap.", logger);
			returnMessage = ApplicationConstant.MSG_ADD_ITEM_ADDED;
		}
		return returnMessage;
	}

	private synchronized void dataSet(ServerType serverType) {
		Utility.log("Accessing the Data Set", logger);
		switch (serverType) {
		case CONCORDIA:
			ItemModel itemModel = new ItemModel("CON0001", "ITEM 11", 10);
			itemHashMap.put(itemModel.getItemId(), itemModel);
			ItemModel itemModel1 = new ItemModel("CON0002", "ITEM 30", 5);
			itemHashMap.put(itemModel1.getItemId(), itemModel1);
			ItemModel itemModel2 = new ItemModel("CON0003", "ITEM 30", 2);
			itemHashMap.put(itemModel2.getItemId(), itemModel2);
			UserModel userModel1 = new UserModel("CONU0001");
			UserModel userModel2 = new UserModel("CONU0002");
			UserModel userModel3 = new UserModel("CONM0001");
			UserModel userModel4 = new UserModel("CONM0002");
			userHashMap.put(userModel1.getUserId(), userModel1);
			userHashMap.put(userModel2.getUserId(), userModel2);
			userHashMap.put(userModel3.getUserId(), userModel3);
			userHashMap.put(userModel4.getUserId(), userModel4);

			break;
		case MCGILL:
			ItemModel itemModel6 = new ItemModel("MCG0001", "ITEM 30", 10);
			itemHashMap.put(itemModel6.getItemId(), itemModel6);
			ItemModel itemModel7 = new ItemModel("MCG0002", "ITEM 30", 5);
			itemHashMap.put(itemModel7.getItemId(), itemModel7);
			ItemModel itemModel8 = new ItemModel("MCG0003", "ITEM 80", 8);
			itemHashMap.put(itemModel8.getItemId(), itemModel8);
			UserModel userModel5 = new UserModel("MCGU0001");
			UserModel userModel6 = new UserModel("MCGU0002");
			UserModel userModel7 = new UserModel("MCGM0001");
			UserModel userModel8 = new UserModel("MCGM0002");
			userHashMap.put(userModel5.getUserId(), userModel5);
			userHashMap.put(userModel6.getUserId(), userModel6);
			userHashMap.put(userModel7.getUserId(), userModel7);
			userHashMap.put(userModel8.getUserId(), userModel8);

			break;
		case MONTREAL:
			ItemModel itemModel3 = new ItemModel("MON0001", "ITEM 30", 8);
			itemHashMap.put(itemModel3.getItemId(), itemModel3);
			ItemModel itemModel4 = new ItemModel("MON0002", "ITEM 40", 6);
			itemHashMap.put(itemModel4.getItemId(), itemModel4);
			ItemModel itemModel5 = new ItemModel("MON0003", "ITEM 51", 4);
			itemHashMap.put(itemModel5.getItemId(), itemModel5);
			UserModel userModel9 = new UserModel("MONU0001");
			UserModel userModel10 = new UserModel("MONU0002");
			UserModel userModel11 = new UserModel("MONM0001");
			UserModel userModel12 = new UserModel("MONM0002");
			userHashMap.put(userModel9.getUserId(), userModel9);
			userHashMap.put(userModel10.getUserId(), userModel10);
			userHashMap.put(userModel11.getUserId(), userModel11);
			userHashMap.put(userModel12.getUserId(), userModel12);
			break;
		default:
			break;
		}

	}

	private synchronized String sendUDPRequest(int serverPort, String requestMessage) {
		Utility.log("Accessing UDP Request", logger);
		Utility.log("Requesting Port " + serverPort + " message: " + requestMessage, logger);
		DatagramSocket aSocket = null;
		String messageReceived = null;
		try {
			aSocket = new DatagramSocket();
			byte[] mes = requestMessage.getBytes();
			InetAddress aHost = InetAddress.getByName("localhost");
			DatagramPacket request = new DatagramPacket(mes, mes.length, aHost, serverPort);
			aSocket.send(request);
			byte[] buffer = new byte[1000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			aSocket.receive(reply);
			Utility.log("Received reply" + reply, logger);
			messageReceived = new String(reply.getData());
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
		return messageReceived;
	}

	public synchronized List<ItemModel> findItemUsingUDP(String itemName) {
		List<ItemModel> itemModels = new ArrayList<>();
		for (String key : itemHashMap.keySet()) {
			ItemModel item = itemHashMap.get(key);
			if (item.getItemName().trim().equalsIgnoreCase(itemName)) {
				itemModels.add(item);
			}
		}
		return itemModels;
	}

	private synchronized List<ItemModel> fetchItemsFromReply(String replyMessage) {
		List<ItemModel> itemModels = new ArrayList<>();
		String[] reply = replyMessage.split(",");
		String status = reply[0];
		if (reply.length > 1) {
			for (int i = 1; i < reply.length; i = i + 3) {
				ItemModel item = new ItemModel(reply[i], reply[i + 1], Integer.valueOf(reply[i + 2]));
				itemModels.add(item);
			}
		}
		return itemModels;
	}

	public synchronized String borrowItemUsingUDP(String userId, String itemId, int noOfDays, boolean isWaitList) {
		String replyMessage = "";
		ItemModel itemModel = itemHashMap.get(itemId);
		if (itemModel != null) {
			if (itemModel.getQuantity() == 0) {
				if (isWaitList) {
					System.out.println("Item is unavalilable. Would you like to be on waitlist (y/n) ?");
					Scanner scanner = new Scanner(System.in);
					String value = scanner.nextLine();
					if (value.trim().equalsIgnoreCase("y")) {
						if (waitList.containsKey(itemId)) {
							Queue<String> userQueue = waitList.get(itemId);
							userQueue.add(userId);
							waitList.put(itemId, userQueue);
						} else {
							Queue<String> userQueue1 = new PriorityQueue<>();
							userQueue1.add(userId);
							waitList.put(itemId, userQueue1);
						}
						replyMessage = "User successfully added to waitlist";

					} else {
						replyMessage = "User has not been added to waitlist.";
					}
				} else
					replyMessage = "Item is unavailaible.";

			} else {
				int quantity = itemModel.getQuantity() - 1;
				itemModel.setQuantity(quantity);
				itemHashMap.put(itemModel.getItemId(), itemModel);
				replyMessage = ApplicationConstant.MSG_BORROW_ITEM_SUCCESSFULLY;
			}
		}
		return replyMessage;
	}

	public void logging(String fileName) {
		File theDir = null;
		theDir = new File(ApplicationConstant.PATH);
		LogManager.getLogManager().reset();
		ConsoleHandler ch = new ConsoleHandler();
		if (!theDir.exists()) {
			theDir.mkdirs();
		}
		File f = new File(theDir.getAbsolutePath().toString() + "\\" + fileName + ".txt");
		if (!f.exists())
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		FileHandler fh = null;
		try {
			fh = new FileHandler(f.getAbsolutePath(), true);
		} catch (SecurityException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fh.setFormatter(new SimpleFormatter());
		fh.setLevel(Level.INFO);
		logger.addHandler(fh);
		ch.setLevel(Level.SEVERE);
		logger.addHandler(ch);

	}

	/**
	 * @return the userHashMap
	 */
	public HashMap<String, UserModel> getUserHashMap() {
		return userHashMap;
	}

	private boolean isUserAllowedToBorrow(UserModel userModel, String itemId) {
		boolean isAllowed = true;
		for (String itemName : userModel.getItemList()) {
			if (itemName.substring(0, 3).equalsIgnoreCase(itemId.substring(0, 3))) {
				isAllowed = false;
				break;
			}
		}
		return isAllowed;
	}

	public String parseData(HashMap<String, ItemModel> itemModels) {
		String output = "";
		for (String key : itemModels.keySet()) {
			ItemModel itemModel = itemModels.get(key);
			output = output.concat(
					itemModel.getItemId() + "," + itemModel.getItemName() + "," + itemModel.getQuantity() + "@");
		}
		output = output.substring(0, output.length() - 1);
		return output;
	}

	public String parseData(List<ItemModel> itemModels) {
		String output = "";
		for (ItemModel itemModel : itemModels)
			output = output.concat(
					itemModel.getItemId() + "," + itemModel.getItemName() + "," + itemModel.getQuantity() + "@");
		output = output.substring(0, output.length() - 1);
		return output;
	}

}
