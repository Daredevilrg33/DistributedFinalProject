/**
 * 
 */
package replicamanagers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

import utilities.ApplicationConstant;

/**
 * @author Rohit Gupta
 *
 */
public class ReplicaManager {

	public static void main(String[] args) {
		Runnable task = () -> {
			// receive();
			recieveMessage();
		};
		Thread thread = new Thread(task);
		thread.start();
	}

	private static void receive() {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(ApplicationConstant.UDP_REPLICA_MANAGER_PORT);
			// the client.
			System.out.println("Server Started............");
			while (true) {// non-terminating loop as the server is always in listening mode.
				byte[] buffer = new byte[1000];// to stored the received data from

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

	private static void recieveMessage() {
		MulticastSocket aSocket = null;
		try {

			aSocket = new MulticastSocket(ApplicationConstant.UDP_REPLICA_MANAGER_PORT);

			aSocket.joinGroup(InetAddress.getByName("230.1.1.2"));

			System.out.println("Server Started............");

			while (true) {
				byte[] buffer = new byte[1000];

				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				String requestData = new String(request.getData());
				System.out.println(requestData);

				String replyMessage = "Reply : ";
				replyMessage = replyMessage.concat(performAction(requestData.trim()));
				System.out.println("replyMessage  " + replyMessage);
				InetAddress aHost = InetAddress.getByName("localhost");

				DatagramPacket reply = new DatagramPacket(replyMessage.getBytes(), replyMessage.getBytes().length,
						aHost, ApplicationConstant.UDP_FRONT_END_PORT);
				aSocket.send(reply);
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

		String action = requestParams[1].trim();
		String managerId = requestParams[2].trim();
		outputMessage = sendUDPRequestToServer(getServerPort(managerId), requestData);
		return outputMessage;
	}

	public static int getServerPort(String userId) {
		int portNo = 0;
		String str = userId.trim().substring(0, 3);
		if (str.equalsIgnoreCase(ApplicationConstant.CONCORDIA_SERVER))
			portNo = ApplicationConstant.UDP_CONCORDIA_PORT;
		else if (str.equalsIgnoreCase(ApplicationConstant.MCGILL_SERVER))
			portNo = ApplicationConstant.UDP_MCGILL_PORT;
		else if (str.equalsIgnoreCase(ApplicationConstant.MONTREAL_SERVER))
			portNo = ApplicationConstant.UDP_MONTREAL_PORT;
		return portNo;
	}

	private synchronized static String sendUDPRequestToServer(int serverPort, String message) {

		System.out.println("sendUDPRequestToServer" + message);
		DatagramSocket aSocket = null;
		String messageReceived = null;
		try {
			aSocket = new DatagramSocket(ApplicationConstant.RM_PORT);
			// aSocket.setSoTimeout(30000);
			byte[] mes = message.getBytes();
			InetAddress aHost = InetAddress.getByName("localhost");

			DatagramPacket request = new DatagramPacket(mes, mes.length, aHost, serverPort);

			aSocket.send(request);

			byte[] buffer = new byte[1000];

			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			aSocket.receive(reply);
			messageReceived = new String(reply.getData());
			// Utility.log("Received reply" + messageReceived, logger);
			System.out.println("Received reply : " + ApplicationConstant.RM_PORT + " : " + messageReceived);
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
}
