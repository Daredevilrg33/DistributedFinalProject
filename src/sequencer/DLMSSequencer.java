/**
 * 
 */
package sequencer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Logger;

import utilities.ApplicationConstant;
import utilities.Utility;

/**
 * @author Rohit Gupta
 *
 */
public class DLMSSequencer {
	static long SequenceNumber = 1;
	public static Logger logger = Logger.getLogger(DLMSSequencer.class.getName());

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
			byte[] buffer = new byte[1000];// to stored the received data from
											// the client.
			System.out.println("Sequencer Server Started............");
			while (true) {// non-terminating loop as the server is always in listening mode.
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);

				// Server waits for the request to come
				aSocket.receive(request);// request received
				String requestData = new String(request.getData());
				System.out.println("Request received from client: " + requestData.trim());

				multicastUDPRequest(request.getData().toString());
				DatagramPacket reply = new DatagramPacket(request.getData(), request.getLength(), request.getAddress(),
						request.getPort());// reply packet ready

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
				sendUDPRequest(ApplicationConstant.IP_ADDRESS_ROHIT, ApplicationConstant.UDP_REPLICA_MANAGER_PORT,
						requestMessage);

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

}
