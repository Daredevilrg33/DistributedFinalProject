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
		log("Starting " + serverType + " server.");
		dataSet(currentServer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.RMInterface#addItem(java.lang.String, java.lang.String,
	 * java.lang.String, int)
	 */
	@Override
	public synchronized boolean addItem(String managerId, String itemId, String itemName, int quantity) {
		// TODO Auto-generated method stub
		boolean isItemAdded = false;
		log("Accessing Add Item. ");
		ItemModel itemModel = new ItemModel(itemId, itemName, quantity);
		UserModel userModel = userHashMap.get(managerId);
		if (userModel != null)
			isItemAdded = addItemToHashMap(itemModel);
		return isItemAdded;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.RMInterface#removeItem(java.lang.String, java.lang.String,
	 * int)
	 */
	@Override
	public synchronized boolean removeItem(String managerId, String itemId, int quantity) {
		// TODO Auto-generated method stub
		boolean isRemoved = false;
		log("Accessing Remove Item.");
		UserModel userModel = userHashMap.get(managerId);
		if (quantity < 0) {
			log("Quantity is less than zero.");
			if (itemHashMap.containsKey(itemId)) {
				ItemModel itemModel = itemHashMap.remove(itemId);
				for (String key : userHashMap.keySet()) {
					UserModel userModel1 = userHashMap.get(key);
					userModel1.removeItem(itemModel.getItemId());
					userHashMap.put(userModel1.getUserId(), userModel1);
				}
				log("Item has been removed successfully !!");
				isRemoved = true;
			}
		} else {
			log("Quantity is greater than zero.");
			if (itemHashMap.containsKey(itemId)) {
				ItemModel itemModel = itemHashMap.get(itemId);
				int oldQuantity = itemModel.getQuantity();
				if (oldQuantity < quantity) {
					log("Item quantity cannot be reduced.");
					return false;
				} else {
					log("Old quantity of the item: " + oldQuantity);
					log("Quantity to be reduce : " + quantity);
					int value = oldQuantity - quantity;
					itemModel.setQuantity(value);
					log("Item quantity has been reduced to : " + value);
					isRemoved = true;
				}
			}
		}
		return isRemoved;
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
			return itemHashMap.toString();
		} else
			return null;
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
					replyMessage = "User successfully added to waitlist";

				} else {
					replyMessage = "User has not been added to waitlist.";
				}
			} else {
				if (userModel != null) {
					if (userModel.getItemList().contains(itemModel.getItemId())) {
						replyMessage = "You already have borrowed the Item previously.";
					} else {
						int quantity = itemModel.getQuantity() - 1;
						itemModel.setQuantity(quantity);
						itemHashMap.put(itemModel.getItemId(), itemModel);
						userModel.addItem(itemModel.getItemId());
						userHashMap.put(userModel.getUserId(), userModel);
						replyMessage = "User has borrowed successfully !!";
					}
				}

			}
		} else {
			if (isUserAllowedToBorrow(userModel, itemId)) {
				// Make UDP Request to the servers.
				String itemValue = itemId.substring(0, 3);
				if (itemValue.equalsIgnoreCase(ApplicationConstant.CONCORDIA_SERVER)) {
					replyMessage = sendUDPRequest(ApplicationConstant.UDP_CONCORDIA_PORT,
							"Borrow" + "," + userId + "," + itemId + "," + noOfDays + "," + true);
				} else if (itemValue.equalsIgnoreCase(ApplicationConstant.MACGILL_SERVER)) {
					replyMessage = sendUDPRequest(ApplicationConstant.UDP_MACGILL_PORT,
							"Borrow" + "," + userId + "," + itemId + "," + noOfDays + "," + true);
				} else {
					replyMessage = sendUDPRequest(ApplicationConstant.UDP_MONTREAL_PORT,
							"Borrow" + "," + userId + "," + itemId + "," + noOfDays + "," + true);
				}
				if (replyMessage.trim().equalsIgnoreCase("User has borrowed successfully !!")) {
					UserModel user = userHashMap.get(userId);
					user.addItem(itemId);

					userHashMap.put(user.getUserId(), user);
				}
			} else {
				replyMessage = "You are not allowed to borrow more than 1 item from other library.";
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
			String reply1 = sendUDPRequest(ApplicationConstant.UDP_MACGILL_PORT,
					"Find Item" + "," + userId + "," + itemName).trim();
			itemModels.addAll(fetchItemsFromReply(reply1));
			String reply2 = sendUDPRequest(ApplicationConstant.UDP_MONTREAL_PORT,
					"Find Item" + "," + userId + "," + itemName).trim();
			itemModels.addAll(fetchItemsFromReply(reply2));
		} else if (currentServer == ServerType.MCGILL) {
			String reply3 = sendUDPRequest(ApplicationConstant.UDP_CONCORDIA_PORT,
					"Find Item" + "," + userId + "," + itemName).trim();
			itemModels.addAll(fetchItemsFromReply(reply3));
			String reply4 = sendUDPRequest(ApplicationConstant.UDP_MONTREAL_PORT,
					"Find Item" + "," + userId + "," + itemName).trim();
			itemModels.addAll(fetchItemsFromReply(reply4));
		} else {
			String reply5 = sendUDPRequest(ApplicationConstant.UDP_CONCORDIA_PORT,
					"Find Item" + "," + userId + "," + itemName).trim();
			itemModels.addAll(fetchItemsFromReply(reply5));
			String reply6 = sendUDPRequest(ApplicationConstant.UDP_MACGILL_PORT,
					"Find Item" + "," + userId + "," + itemName).trim();
			itemModels.addAll(fetchItemsFromReply(reply6));
		}
		return itemModels.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.RMInterface#returnItem(java.lang.String, java.lang.String)
	 */
	@Override
	public synchronized boolean returnItem(String userId, String itemId) {
		// TODO Auto-generated method stub
		boolean isItemReturned = false;
		UserModel user = userHashMap.get(userId);
		if (user != null && user.getItemList().contains(itemId)) {
			if (itemHashMap.containsKey(itemId)) {
				isItemReturned = returnItemAndAssignToWaitListUser(userId, itemId);
			} else {
				String str = itemId.substring(0, 3);
				String reply = "";
				if (str.equalsIgnoreCase("CON")) {
					reply = sendUDPRequest(ApplicationConstant.UDP_CONCORDIA_PORT,
							"Return Item" + "," + userId + "," + itemId).trim();
				} else if (str.equalsIgnoreCase("MCG")) {
					reply = sendUDPRequest(ApplicationConstant.UDP_MACGILL_PORT,
							"Return Item" + "," + userId + "," + itemId).trim();
				} else
					reply = sendUDPRequest(ApplicationConstant.UDP_MONTREAL_PORT,
							"Return Item" + "," + userId + "," + itemId).trim();

				if (reply.trim().equalsIgnoreCase("successful")) {
					UserModel user1 = userHashMap.get(userId);
					user1.removeItem(itemId);
					userHashMap.put(user1.getUserId(), user1);
					isItemReturned = true;
				} else {
					isItemReturned = false;
				}
			}
		} else
			isItemReturned = false;
		return isItemReturned;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see interfaces.RMInterface#exchangeItem(java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public synchronized boolean exchangeItem(String userId, String newItemId, String oldItemId) {
		// TODO Auto-generated method stub
		boolean isItemExchanged = false;
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

					boolean isItemReturned = returnItem(userId, oldItemId);
					if (isItemReturned)
						isItemExchanged = true;
					else
						isItemExchanged = false;
				}
			} else {
				if (newItemValue.equalsIgnoreCase(ApplicationConstant.CONCORDIA_SERVER)) {
					replyMessage = sendUDPRequest(ApplicationConstant.UDP_CONCORDIA_PORT,
							"Borrow" + "," + userId + "," + newItemId + "," + noOfDays + "," + false);
				} else if (newItemValue.equalsIgnoreCase(ApplicationConstant.MACGILL_SERVER)) {
					replyMessage = sendUDPRequest(ApplicationConstant.UDP_MACGILL_PORT,
							"Borrow" + "," + userId + "," + newItemId + "," + noOfDays + "," + false);
				} else {
					replyMessage = sendUDPRequest(ApplicationConstant.UDP_MONTREAL_PORT,
							"Borrow" + "," + userId + "," + newItemId + "," + noOfDays + "," + false);
				}
				if (replyMessage.trim().equalsIgnoreCase("User has borrowed successfully !!")) {
					UserModel user = userHashMap.get(userId);
					user.addItem(newItemId);
					userHashMap.put(user.getUserId(), user);
					boolean isItemReturned = returnItem(userId, oldItemId);
					if (isItemReturned)
						isItemExchanged = true;
					else
						isItemExchanged = false;
				} else {
					System.out.println(replyMessage);
					isItemExchanged = false;
				}
			}
		} else
			isItemExchanged = false;
		return isItemExchanged;
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
							replyMessage = sendUDPRequest(ApplicationConstant.UDP_CONCORDIA_PORT,
									"Assign" + "," + userId + "," + itemId + "," + 5);
						} else if (userValue.equalsIgnoreCase(ApplicationConstant.MACGILL_SERVER)) {
							replyMessage = sendUDPRequest(ApplicationConstant.UDP_MACGILL_PORT,
									"Assign" + "," + userId + "," + itemId + "," + 5);
						} else {
							replyMessage = sendUDPRequest(ApplicationConstant.UDP_MONTREAL_PORT,
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

	private synchronized boolean addItemToHashMap(ItemModel itemModel) {
		boolean isAdded = false;
		if (itemHashMap.containsKey(itemModel.getItemId())) {
			log("Item already present in concordia hash map.");
			ItemModel itemModel1 = itemHashMap.get(itemModel.getItemId());
			log("Previous Quantity: " + itemModel1.getQuantity());
			itemModel1.setQuantity(itemModel1.getQuantity() + itemModel.getQuantity());
			log("Final Quantity: " + itemModel1.getQuantity());
			boolean isAssigned = assignItemToWaitListUser(itemModel1);
			if (isAssigned) {
				int quantityValue = itemModel1.getQuantity() - 1;
				itemModel1.setQuantity(quantityValue);
			}
			itemHashMap.put(itemModel1.getItemId(), itemModel1);
			isAdded = true;
		} else {
			itemHashMap.put(itemModel.getItemId(), itemModel);
			log("Item successfully added to concordia hashmap.");
			isAdded = true;
		}
		return isAdded;
	}

	private synchronized void dataSet(ServerType serverType) {
		log("Accessing the Data Set");
		switch (serverType) {
		case CONCORDIA:
			ItemModel itemModel = new ItemModel("CON1011", "ITEM 11", 10);
			itemHashMap.put(itemModel.getItemId(), itemModel);
			ItemModel itemModel1 = new ItemModel("CON1021", "ITEM 30", 5);
			itemHashMap.put(itemModel1.getItemId(), itemModel1);
			ItemModel itemModel2 = new ItemModel("CON1025", "ITEM 30", 2);
			itemHashMap.put(itemModel2.getItemId(), itemModel2);
			UserModel userModel1 = new UserModel("CONU0001");
			UserModel userModel2 = new UserModel("CONU0002");
			UserModel userModel3 = new UserModel("CONM0003");
			UserModel userModel4 = new UserModel("CONM0004");
			userHashMap.put(userModel1.getUserId(), userModel1);
			userHashMap.put(userModel2.getUserId(), userModel2);
			userHashMap.put(userModel3.getUserId(), userModel3);
			userHashMap.put(userModel4.getUserId(), userModel4);

			break;
		case MCGILL:
			ItemModel itemModel6 = new ItemModel("MCG1065", "ITEM 30", 10);
			itemHashMap.put(itemModel6.getItemId(), itemModel6);
			ItemModel itemModel7 = new ItemModel("MCG1077", "ITEM 30", 5);
			itemHashMap.put(itemModel7.getItemId(), itemModel7);
			ItemModel itemModel8 = new ItemModel("MCG1080", "ITEM 80", 8);
			itemHashMap.put(itemModel8.getItemId(), itemModel8);
			UserModel userModel5 = new UserModel("MCGU0005");
			UserModel userModel6 = new UserModel("MCGU0006");
			UserModel userModel7 = new UserModel("MCGM0007");
			UserModel userModel8 = new UserModel("MCGM0008");
			userHashMap.put(userModel5.getUserId(), userModel5);
			userHashMap.put(userModel6.getUserId(), userModel6);
			userHashMap.put(userModel7.getUserId(), userModel7);
			userHashMap.put(userModel8.getUserId(), userModel8);

			break;
		case MONTREAL:
			ItemModel itemModel3 = new ItemModel("MON1030", "ITEM 30", 8);
			itemHashMap.put(itemModel3.getItemId(), itemModel3);
			ItemModel itemModel4 = new ItemModel("MON1040", "ITEM 40", 6);
			itemHashMap.put(itemModel4.getItemId(), itemModel4);
			ItemModel itemModel5 = new ItemModel("MON1051", "ITEM 51", 4);
			itemHashMap.put(itemModel5.getItemId(), itemModel5);
			UserModel userModel9 = new UserModel("MONU0009");
			UserModel userModel10 = new UserModel("MONU0010");
			UserModel userModel11 = new UserModel("MONM0011");
			UserModel userModel12 = new UserModel("MONM0012");
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
		log("Accessing UDP Request");
		log("Requesting Port " + serverPort + " message: " + requestMessage);
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
			log("Received reply" + reply);
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

	private void log(String message) {
		logger.info(message);
		System.out.println(message);
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
				replyMessage = "User has borrowed successfully !!";
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
}
