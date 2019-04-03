/**
 * 
 */
package sequencer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.logging.Logger;

import utilities.ApplicationConstant;
import utilities.Utility;

/**
 * @author Rohit Gupta
 *
 */
public class DLMSSequencer {
	static int sequenceNumber = 1;
	public static Logger logger = Logger.getLogger(DLMSSequencer.class.getName());
	static HashMap<Integer, String> historyBuffer = new HashMap<>();

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
			aSocket = new DatagramSocket(ApplicationConstant.UDP_SEQUENCER_PORT);
			// the client.
			System.out.println("Sequencer Server Started............");
			while (true) {// non-terminating loop as the server is always in listening mode.
				byte[] buffer = new byte[1000];// to stored the received data from

				DatagramPacket request = new DatagramPacket(buffer, buffer.length);

				// Server waits for the request to come
				aSocket.receive(request);// request received
				String requestData = new String(request.getData(), request.getOffset(), request.getLength());
				System.out.println("Request received from Front End: " + requestData.trim());
				String multicastMessage = String.valueOf(sequenceNumber) + "," + requestData;

				buffer = new byte[1000];
				historyBuffer.put(sequenceNumber, multicastMessage);
				// if (sequenceNumber != 3)
				// multicastUDPRequest(multicastMessage);
				// if (sequenceNumber == 5) {
				// multicastUDPRequest(historyBuffer.get(3));
				// }
				multicastUDPRequest(multicastMessage);
				sequenceNumber++;
				// DatagramPacket reply = new DatagramPacket(request.getData(),
				// request.getLength(), request.getAddress(),
				// request.getPort());// reply packet ready
				//
				// aSocket.send(reply);// reply sent
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

	private static synchronized void multicastUDPRequest(String requestMessage) {
		//
		// new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// // TODO Auto-generated method stub
		// sendUDPRequest(ApplicationConstant.IP_ADDRESS_HASTI,
		// ApplicationConstant.UDP_REPLICA_MANAGER_PORT,
		// requestMessage);
		//
		// }
		// }).start();
		// new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// // TODO Auto-generated method stub
		// sendUDPRequest(ApplicationConstant.IP_ADDRESS_NANCY,
		// ApplicationConstant.UDP_REPLICA_MANAGER_PORT,
		// requestMessage);
		//
		// }
		// }).start();
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// ApplicationConstant.IP_ADDRESS_ROHIT
				// sendUDPRequest("localhost", ApplicationConstant.UDP_REPLICA_MANAGER_PORT,
				// requestMessage);
				int count = 0;
				while (count < 2) {
					sendMultiCastRequest(requestMessage);
					count++;
				}
			}
		}).start();
		// new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// // TODO Auto-generated method stub
		// sendUDPRequest(ApplicationConstant.IP_ADDRESS_ROOHANI,
		// ApplicationConstant.UDP_REPLICA_MANAGER_PORT,
		// requestMessage);
		//
		// }
		// }).start();

	}

	private static String sendUDPRequest(String ipAddress, int serverPort, String requestMessage) {
		Utility.log("Accessing UDP Request", logger);
		Utility.log("Requesting Port " + serverPort + " message: " + requestMessage, logger);
		DatagramSocket aSocket = null;
		String messageReceived = null;
		try {
			aSocket = new DatagramSocket();
			byte[] mes = requestMessage.getBytes();
			InetAddress aHost = InetAddress.getByName(ipAddress);
			System.out.println("sendUDPRequest Message: " + requestMessage);
			DatagramPacket request = new DatagramPacket(mes, mes.length, aHost, serverPort);
			aSocket.send(request);
			byte[] buffer = new byte[1000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			aSocket.receive(reply);
			Utility.log("Received reply" + reply, logger);
			messageReceived = new String(reply.getData(), reply.getOffset(), reply.getLength());
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

	private static void sendMultiCastRequest(String message) {
		DatagramSocket datagramSocket = null;

		try {
			datagramSocket = new DatagramSocket();
			byte[] by = message.getBytes();
			InetAddress aHost = InetAddress.getByName("230.1.1.2");
			DatagramPacket datagramPacket = new DatagramPacket(by, by.length, aHost,
					ApplicationConstant.UDP_REPLICA_MANAGER_PORT);
			datagramSocket.send(datagramPacket);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (datagramSocket != null) {
				datagramSocket.close();
			}
		}

	}
}
