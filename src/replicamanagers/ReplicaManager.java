/**
 * 
 */
package replicamanagers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import utilities.ApplicationConstant;

/**
 * @author Rohit Gupta
 *
 */
public class ReplicaManager {

	public static void main(String[] args) {
		Runnable task = () -> {
			receive();
		};
		Thread thread = new Thread(task);
		thread.start();
	}

	private static void receive() {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(ApplicationConstant.UDP_REPLICA_MANAGER_PORT);
			byte[] buffer = new byte[1000];// to stored the received data from
											// the client.
			System.out.println("Server Started............");
			while (true) {// non-terminating loop as the server is always in listening mode.
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);

				// Server waits for the request to come
				aSocket.receive(request);// request received

				String requestData = new String(request.getData());
				System.out.println("Request received from client: " + requestData.trim());
				System.out.println("Request received from Sequencer: " + requestData.trim());
				performAction(requestData.trim());
				InetAddress aHost = InetAddress.getByName("localhost");
				DatagramPacket reply = new DatagramPacket(request.getData(), request.getLength(), aHost,
						ApplicationConstant.UDP_FRONT_END_PORT);// reply packet ready

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

	public static String performAction(String requestData) {
		String outputMessage = "";
		String[] requestParams = requestData.split(",");
		String sequenceNumber = requestParams[0].trim();
		String action = requestParams[1].trim();

		if (action.equalsIgnoreCase("AddItem")) {

			String managerId = requestParams[2].trim();
			String itemId = requestParams[3].trim();
			String itemName = requestParams[4].trim();
			String quantity = requestParams[5].trim();
			
		}
		if (action.equalsIgnoreCase("RemoveItem")) {
			String managerId = requestParams[2].trim();
			String itemId = requestParams[3].trim();
			String quantity = requestParams[4].trim();
		}
		if (action.equalsIgnoreCase("ListItem")) {

			String managerId = requestParams[2].trim();
		}
		if (action.equalsIgnoreCase("BorrowItem")) {
			String userId = requestParams[2].trim();
			String itemId = requestParams[3].trim();
			String noOfDays = requestParams[4].trim();
		}
		if (action.equalsIgnoreCase("FindItem")) {
			String userId = requestParams[2].trim();
			String itemName = requestParams[3].trim();
		}
		if (action.equalsIgnoreCase("ReturnItem")) {
			String userId = requestParams[2].trim();
			String itemId = requestParams[3].trim();
		}
		if (action.equalsIgnoreCase("ExchangeItem")) {
			String userId = requestParams[2].trim();
			String newItemId = requestParams[3].trim();
			String oldItemId = requestParams[4].trim();
		}

		return outputMessage;
	}
}
