/**
 * 
 */
package servers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;

import models.ItemModel;
import models.ServerType;
import models.UserModel;
import replicamanagers.ReplicaManagerImplementation;
import utilities.ApplicationConstant;

/**
 * @author Rohit Gupta
 *
 */
public class ServerMcgill extends Thread {
	public static volatile boolean crashFailure = false;
	public static DatagramSocket aSocket = null;

	@Override
	public void run() {

		ReplicaManagerImplementation replicaManagerImplementation = new ReplicaManagerImplementation(ServerType.MCGILL);
		replicaManagerImplementation.logging("Mcgill Server");
		Runnable task = () -> {
			receive(replicaManagerImplementation);
		};
		Runnable task1 = () -> {
			receiveLocalUDP(replicaManagerImplementation);
		};
		Thread thread = new Thread(task);
		thread.start();
		Thread thread1 = new Thread(task1);
		thread1.start();
	}

	private static void receive(ReplicaManagerImplementation replicaManagerImplementation) {

		try {
			aSocket = new DatagramSocket(ApplicationConstant.UDP_MCGILL_PORT);
			byte[] buffer = new byte[1000];// to stored the received data from
											// the client.
			System.out.println("Server Started............");
			while (true) {// non-terminating loop as the server is always in listening mode.
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);

				// Server waits for the request to come
				aSocket.receive(request);// request received

				String requestData = new String(request.getData(), request.getOffset(), request.getLength());
				System.out.println("Request received from client: " + requestData.trim());
				String replyMessage = performAction(requestData.trim(), replicaManagerImplementation);
				DatagramPacket reply = new DatagramPacket(replyMessage.getBytes(), replyMessage.getBytes().length,
						request.getAddress(), request.getPort());// reply packet ready

				aSocket.send(reply);// reply sent
			}
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
	}

	public static String performAction(String requestData, ReplicaManagerImplementation replicaManagerImplementation) {
		String outputMessage = "";
		String[] requestParams = requestData.split(",");
		String sequenceNumber = requestParams[0].trim();
		String action = requestParams[1].trim();

		if (action.equalsIgnoreCase(ApplicationConstant.OP_ADD_ITEM)) {

			String managerId = requestParams[2].trim();
			String itemId = requestParams[3].trim();
			String itemName = requestParams[4].trim();
			String quantity = requestParams[5].trim();
			outputMessage = String.valueOf(
					replicaManagerImplementation.addItem(managerId, itemId, itemName, Integer.valueOf(quantity)));

		}
		if (action.equalsIgnoreCase(ApplicationConstant.OP_REMOVE_ITEM)) {
			String managerId = requestParams[2].trim();
			String itemId = requestParams[3].trim();
			String quantity = requestParams[4].trim();
			outputMessage = String
					.valueOf(replicaManagerImplementation.removeItem(managerId, itemId, Integer.valueOf(quantity)));
		}
		if (action.equalsIgnoreCase(ApplicationConstant.OP_LIST_ITEM_AVAILABLILITY)) {
			String managerId = requestParams[2].trim();
			outputMessage = replicaManagerImplementation.listItemAvailability(managerId);
		}
		if (action.equalsIgnoreCase(ApplicationConstant.OP_BORROW_ITEM)) {
			String userId = requestParams[2].trim();
			String itemId = requestParams[3].trim();
			String noOfDays = requestParams[4].trim();
			outputMessage = replicaManagerImplementation.borrowItem(userId, itemId, Integer.valueOf(noOfDays));

		}
		if (action.equalsIgnoreCase(ApplicationConstant.OP_FIND_ITEM)) {
			String userId = requestParams[2].trim();
			String itemName = requestParams[3].trim();
			outputMessage = replicaManagerImplementation.findItem(userId, itemName);
		}
		if (action.equalsIgnoreCase(ApplicationConstant.OP_RETURN_ITEM)) {
			String userId = requestParams[2].trim();
			String itemId = requestParams[3].trim();
			outputMessage = String.valueOf(replicaManagerImplementation.returnItem(userId, itemId));

		}
		if (action.equalsIgnoreCase(ApplicationConstant.OP_EXCHANGE_ITEM)) {
			String userId = requestParams[2].trim();
			String newItemId = requestParams[3].trim();
			String oldItemId = requestParams[4].trim();
			outputMessage = String.valueOf(replicaManagerImplementation.exchangeItem(userId, newItemId, oldItemId));
		}
		return outputMessage;
	}

	private static void receiveLocalUDP(ReplicaManagerImplementation replicaManagerImplementation) {
		DatagramSocket dataSocket = null;
		try {
			dataSocket = new DatagramSocket(ApplicationConstant.UDP_MCG_SERVER);
			while (true) {
				byte[] buffer = new byte[3000];
				DatagramPacket request = null;
				request = new DatagramPacket(buffer, buffer.length);
				dataSocket.receive(request);
				System.out.println("UDP Request Recieved MCGILL. ");
				replicaManagerImplementation.logger.info("UDP Request Recieved MCGILL!!");
				String inputFromServer = new String(request.getData(), request.getOffset(), request.getLength());
				DatagramPacket reply = null;
				String replyMessage = "";
				replicaManagerImplementation.logger.info(inputFromServer);
				System.out.println(inputFromServer);
				if (inputFromServer.contains("Borrow")) {
					String[] data = inputFromServer.split(",");
					String action = data[0].trim();
					String userId = data[1].trim();
					String itemId = data[2].trim();
					String noOfDays = data[3].trim();
					boolean isWaitList = Boolean.parseBoolean(data[4].trim());
					int noOfDay = Integer.valueOf(noOfDays);
					replyMessage = replicaManagerImplementation.borrowItemUsingUDP(userId, itemId, noOfDay, isWaitList);
				} else if (inputFromServer.contains("Find Item")) {
					String[] data = inputFromServer.split(",");
					String action = data[0].trim();
					String userId = data[1].trim();
					String itemName = data[2].trim();
					List<ItemModel> itemModels = replicaManagerImplementation.findItemUsingUDP(itemName);
					if (itemModels.size() > 0) {
						replyMessage = "successful";
						for (ItemModel itemModel : itemModels) {
							replyMessage = replyMessage.concat("," + itemModel.getItemId() + ","
									+ itemModel.getItemName() + "," + itemModel.getQuantity());
						}
					} else
						replyMessage = "unsuccessful";
				} else if (inputFromServer.contains("Return Item")) {
					String[] data = inputFromServer.split(",");
					String action = data[0].trim();
					String userId = data[1].trim();
					String itemId = data[2].trim();
					boolean isReturned = replicaManagerImplementation.returnItemAndAssignToWaitListUser(userId, itemId);
					if (isReturned)
						replyMessage = "successful";
					else
						replyMessage = "unsuccessful";
				} else {
					String[] data = inputFromServer.split(",");
					String action = data[0].trim();
					String userId = data[1].trim();
					String itemId = data[2].trim();
					HashMap<String, UserModel> userHashMap = replicaManagerImplementation.getUserHashMap();
					UserModel userModel = userHashMap.get(userId);
					userModel.getItemList().add(itemId);
					replyMessage = "successful";
				}
				replicaManagerImplementation.logger.info(replyMessage);
				System.out.println(replyMessage);
				byte[] finalmessage = replyMessage.getBytes();
				reply = new DatagramPacket(finalmessage, finalmessage.length, request.getAddress(), request.getPort());
				dataSocket.send(reply);
			}
		} catch (SocketException e) {
			replicaManagerImplementation.logger.info("Socket: " + e.getMessage());
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			replicaManagerImplementation.logger.info("IO: " + e.getMessage());
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (dataSocket != null)
				dataSocket.close();
		}
	}

	public boolean isCrashFailure() {
		return crashFailure;
	}

	public static void setCrashFailure(boolean crashFail) {
		crashFailure = crashFail;
	}

}
