/**
 * 
 */
package frontends;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.omg.CORBA.ORB;

import dlms.FrontEndOperationsPOA;
import utilities.ApplicationConstant;
import utilities.Utility;

/**
 * @author Rohit Gupta
 *
 */
public class FrontEndImplementation extends FrontEndOperationsPOA {
	public Logger logger = Logger.getLogger(FrontEndImplementation.class.getName());
	String outputResult = "";
	HashMap<String, String> responseMap = new HashMap();

	/**
	 * @param orb
	 */
	public void setORB(ORB orb) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dlms.FrontEndOperationsOperations#addItem(java.lang.String,
	 * java.lang.String, java.lang.String, int)
	 */
	@Override
	public String addItem(String managerId, String itemId, String itemName, int quantity) {
		// TODO Auto-generated method stub
		String udpMessage = ApplicationConstant.OP_ADD_ITEM + "," + managerId + "," + itemId + "," + itemName + ","
				+ String.valueOf(quantity);
		System.out.println("Add ITEM " + udpMessage);
		String output = sendUDPRequestToSequencer(ApplicationConstant.UDP_SEQUENCER_PORT, udpMessage);
		return output;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dlms.FrontEndOperationsOperations#removeItem(java.lang.String,
	 * java.lang.String, int)
	 */
	@Override
	public String removeItem(String managerId, String itemId, int quantity) {
		// TODO Auto-generated method stub
		String udpMessage = ApplicationConstant.OP_REMOVE_ITEM + "," + managerId + "," + itemId + ","
				+ String.valueOf(quantity);
		System.out.println("Remove ITEM " + udpMessage);

		String output = sendUDPRequestToSequencer(ApplicationConstant.UDP_SEQUENCER_PORT, udpMessage);
		return output;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dlms.FrontEndOperationsOperations#listItemAvailability(java.lang.String)
	 */
	@Override
	public String listItemAvailability(String managerId) {
		// TODO Auto-generated method stub
		String udpMessage = ApplicationConstant.OP_LIST_ITEM_AVAILABLILITY + "," + managerId;
		System.out.println("List ITEM " + udpMessage);

		String output = sendUDPRequestToSequencer(ApplicationConstant.UDP_SEQUENCER_PORT, udpMessage);

		return output;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dlms.FrontEndOperationsOperations#borrowItem(java.lang.String,
	 * java.lang.String, int)
	 */
	@Override
	public String borrowItem(String userId, String itemId, int noOfDays) {
		// TODO Auto-generated method stub
		String udpMessage = ApplicationConstant.OP_BORROW_ITEM + "," + userId + "," + itemId + ","
				+ String.valueOf(noOfDays);
		System.out.println("Borrow ITEM " + udpMessage);

		String output = sendUDPRequestToSequencer(ApplicationConstant.UDP_SEQUENCER_PORT, udpMessage);
		return output;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dlms.FrontEndOperationsOperations#findItem(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public String findItem(String userId, String itemName) {
		// TODO Auto-generated method stub
		String udpMessage = ApplicationConstant.OP_FIND_ITEM + "," + userId + "," + itemName;
		System.out.println("FIND ITEM " + udpMessage);

		String output = sendUDPRequestToSequencer(ApplicationConstant.UDP_SEQUENCER_PORT, udpMessage);
		return output;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dlms.FrontEndOperationsOperations#returnItem(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public String returnItem(String userId, String itemId) {
		// TODO Auto-generated method stub
		String udpMessage = ApplicationConstant.OP_RETURN_ITEM + "," + userId + "," + itemId;
		System.out.println("Return ITEM " + udpMessage);

		String output = sendUDPRequestToSequencer(ApplicationConstant.UDP_SEQUENCER_PORT, udpMessage);
		return output;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dlms.FrontEndOperationsOperations#exchangeItem(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public String exchangeItem(String userId, String newItemId, String oldItemId) {
		// TODO Auto-generated method stub

		String udpMessage = ApplicationConstant.OP_EXCHANGE_ITEM + "," + userId + "," + newItemId + "," + oldItemId;
		System.out.println("Exchange ITEM " + udpMessage);

		String output = sendUDPRequestToSequencer(ApplicationConstant.UDP_SEQUENCER_PORT, udpMessage);
		return output;
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

	private String sendUDPRequestToSequencer(int serverPort, String message) {

		Utility.log("Accessing UDP Request", logger);
		Utility.log("Requesting Port " + serverPort + " message: " + message, logger);
		DatagramSocket aSocket = null;
		String messageReceived = null;
		try {
			aSocket = new DatagramSocket(ApplicationConstant.UDP_FRONT_END_PORT);
			// aSocket.setSoTimeout(10000);
			byte[] mes = message.getBytes();
			InetAddress aHost = InetAddress.getByName("localhost");

			DatagramPacket request = new DatagramPacket(mes, mes.length, aHost, serverPort);

			aSocket.send(request);
			// String requestData = new String(request.getData());
			// System.out.println("Request received from client: " + requestData.trim());

			byte[] buffer = new byte[1000];
			int resultCount = 0;
			while (resultCount < 2) {
				DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(reply);
				messageReceived = "";
				outputResult = "";
				messageReceived = new String(reply.getData(), reply.getOffset(), reply.getLength());
				// String resIdentifier=messageReceived.split("@")[0];
				// responseMap.put(resIdentifier, messageReceived.split("@")[1]);
				System.out.println("Message Recieved: " + messageReceived.trim());
				System.out.println("Address : " + reply.getAddress());
				System.out.println("Port: " + reply.getPort());
				Utility.log("Received reply" + messageReceived.trim(), logger);
				resultCount++;
				buffer = new byte[1000];
			}
			if (resultCount == 1)
				aSocket.close();
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

	private String sendUDPRequestForCrashFailure(int serverPort, String message) {

		Utility.log("Accessing UDP Request", logger);
		Utility.log("Requesting Port " + serverPort + " message: " + message, logger);
		DatagramSocket aSocket = null;
		String messageReceived = null;
		try {
			aSocket = new DatagramSocket(ApplicationConstant.UDP_FRONT_END_PORT);
			// aSocket.setSoTimeout(10000);
			byte[] mes = message.getBytes();
			InetAddress aHost = InetAddress.getByName("localhost");

			DatagramPacket request = new DatagramPacket(mes, mes.length, aHost, serverPort);

			aSocket.send(request);
			// String requestData = new String(request.getData());
			// System.out.println("Request received from client: " + requestData.trim());

			byte[] buffer = new byte[1000];

			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			aSocket.receive(reply);
			messageReceived = "";
			messageReceived = new String(reply.getData(), reply.getOffset(), reply.getLength());
			// String resIdentifier=messageReceived.split("@")[0];
			// responseMap.put(resIdentifier, messageReceived.split("@")[1]);
			Utility.log("Received reply" + messageReceived.trim(), logger);
			buffer = new byte[1000];
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

	private void compareResponse() {
		int count = 0;
		boolean rmNancy = false;
		boolean rmRohit = false;
		boolean rmRoohani = false;
		boolean rmHasti = false;
		if (responseMap.get("RMNancy").equals(responseMap.get("RMRohit"))) {
			count++;
			rmRohit = true;
		} else if (responseMap.get("RMNancy").equals(responseMap.get("RMRoohani"))) {
			count++;
			rmRoohani = true;
		} else if (responseMap.get("RMNancy").equals(responseMap.get("RMHasti"))) {
			count++;
			rmHasti = true;
		}

		if (rmRohit && rmRoohani && rmHasti) {
			System.out.println("\n\n\n All Responses matches");
			rmNancy = true;
		}
		if (!rmRohit && !rmRoohani && !rmHasti) {
			if (responseMap.get("RMRohit").equals(responseMap.get("RMHasti"))) {
				count++;
				rmHasti = true;
			}
			if (responseMap.get("RMRohit").equals(responseMap.get("RMRoohani"))) {
				count++;
				rmRoohani = true;
			}
			if (rmHasti && rmRoohani) {
				rmRohit = true;
			}
			if (!rmHasti && !rmRoohani) {
				if (responseMap.get("RMHasti").equals(responseMap.get("RMRoohani"))) {

				}
			}
		}
		if (!rmNancy) {
			System.out.println("\n\n\n Nancy RM fails");
		}
		if (!rmRohit) {
			System.out.println("\n\n\n Rohit RM fails");
		}
		if (!rmRoohani) {
			System.out.println("\n\n\n Rohit RM fails");
		}
		if (!rmHasti) {
			System.out.println("\n\n\n Hasti RM fails");
		}

		if (count > 2) {
			System.out.println("Majority responses are matching ");
		}
	}

	@Override
	public String crashingServer(int status) {
		// TODO Auto-generated method stub
		String response = "";
		String udpMessage = ApplicationConstant.OP_CRASH_SERVER + "," + status;
		System.out.println("CRASH SERVER" + udpMessage);

		response = sendUDPRequestForCrashFailure(ApplicationConstant.UDP_REPLICA_MANAGER_PORT, udpMessage);
		// if (response.contains("System Crashed")) {
		// udpMessage = ApplicationConstant.OP_CRASH_SERVER + "," + 10;
		// response =
		// sendUDPRequestForCrashFailure(ApplicationConstant.UDP_REPLICA_MANAGER_PORT,
		// udpMessage);
		// }
		return response;

	}

}
