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
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
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
	HashMap<String, String> addressMap = new HashMap();
	boolean resultFound = false;
	String failedRM = "";
	String rmStatus="";

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
		responseMap.clear();

		DatagramSocket aSocket = null;

		String messageReceived = null;
		try {
			aSocket = new DatagramSocket(ApplicationConstant.UDP_FRONT_END_PORT);
			aSocket.setSoTimeout(10000);
			byte[] mes = message.getBytes();
			InetAddress aHost = InetAddress.getByName("localhost");

			DatagramPacket request = new DatagramPacket(mes, mes.length, aHost, serverPort);

			aSocket.send(request);
			// String requestData = new String(request.getData());
			// System.out.println("Request received from client: " + requestData.trim());

			byte[] buffer = new byte[1000];
			int resultCount = 0;
			while (resultCount < 3) {
				DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(reply);
				messageReceived = "";
				outputResult = "";
				messageReceived = new String(reply.getData(), reply.getOffset(), reply.getLength());
				addResponseToMap(messageReceived);
				addressMap.put(messageReceived.split(":")[0].trim(), reply.getAddress().toString());
				// String resIdentifier=messageReceived.split("@")[0];
				// responseMap.put(resIdentifier, messageReceived.split("@")[1]);
				System.out.println("Message Recieved: " + messageReceived.trim());
				System.out.println("Address : " + reply.getAddress());
				System.out.println("Port: " + reply.getPort());
				Utility.log("Received reply" + messageReceived.trim(), logger);
				resultCount++;
			}
		} catch (SocketTimeoutException e) {
			System.out.println("Socket Time Out: " + e.getMessage());
			 sentRecoverRMRequest();
			// crashingServer(10);
		} catch (SocketException e1) {
			System.out.println("Socket: " + e1.getMessage());
//			sentRecoverRMRequest();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
		String response1, response2, response3, response4 = "";

		response1 = responseMap.get("RM1") == null ? "" : responseMap.get("RM1");
		response2 = responseMap.get("RM2") == null ? "" : responseMap.get("RM2");
		response3 = responseMap.get("RM3") == null ? "" : responseMap.get("RM3");
		response4 = responseMap.get("RM4") == null ? "" : responseMap.get("RM4");
		messageReceived =resultComparison(response1, response2, response3, response4);
		if(!rmStatus.isEmpty())
		{
			 byzantineNotify() ;
		}

		return messageReceived;
	}

	private void sentRecoverRMRequest() {
		// TODO Auto-generated method stub
		if (!responseMap.containsKey("RM1")) {
			failedRM = "RM1";
		}
		if (!responseMap.containsKey("RM2")) {
			failedRM = "RM2";
		}
		if (!responseMap.containsKey("RM3")) {
			failedRM = "RM3";
		}
		if (!responseMap.containsKey("RM4")) {
			failedRM = "RM4";
		}
		String udpMessage = ApplicationConstant.OP_CRASH_SERVER + "," + 10;
		System.out.println("\n\n sentRecoverRMRequest " + udpMessage);
		sendUDPRequestForCrashFailure(ApplicationConstant.UDP_REPLICA_MANAGER_PORT, udpMessage,"localhost");
	}

	private void addResponseToMap(String messageReceived) {
		// TODO Auto-generated method stub
		String[] str = messageReceived.split(":");
		if (str.length > 1)
			responseMap.put(str[0].trim(), str[1].trim());
	}

	private String sendUDPRequestForCrashFailure(int serverPort, String message, String ipAddress) {

		Utility.log("Accessing UDP Request", logger);
		Utility.log("Requesting Port " + serverPort + " message: " + message, logger);
		DatagramSocket aSocket = null;
		String messageReceived = null;
		try {
			aSocket = new DatagramSocket();

			byte[] mes = message.getBytes();
			InetAddress aHost = InetAddress.getByName(ipAddress);

			DatagramPacket request = new DatagramPacket(mes, mes.length, aHost, serverPort);

			aSocket.send(request);
			String requestData = new String(request.getData());
			System.out.println("Request received from client: " + requestData.trim());
			messageReceived = "Operation Done";
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

	private String resultComparison(String response1, String response2, String response3, String response4) {
		String output = "";

		boolean isRM1AndRM4Equal = false;
		boolean isRM2AndRM3Equal = false;
		resultFound = true;
		if (response1.trim().equalsIgnoreCase(response4.trim())) {

			System.out.println("RM1 and RM4 results are same.");
			isRM1AndRM4Equal = true;
		}
		if (response2.trim().equalsIgnoreCase(response3.trim())) {
			System.out.println("RM2 and RM3 results are same.");
			isRM2AndRM3Equal = true;
		}

		if (isRM1AndRM4Equal && isRM2AndRM3Equal) {
			if (response1.trim().equalsIgnoreCase(response2.trim())) {
				resultFound = true;
				return response1;
			}
		} else if (!isRM1AndRM4Equal && isRM2AndRM3Equal) {
			if (response1.trim().equalsIgnoreCase(response2.trim())) {
				resultFound = true;
				rmStatus="RM4";
				return response1;
			} else if (response4.trim().equalsIgnoreCase(response2.trim())) {
				resultFound = true;
				rmStatus="RM1";
				return response4;
			} else {
				resultFound = true;
				rmStatus="RM4";
				return response2;
			}

		} else if (!isRM2AndRM3Equal && isRM1AndRM4Equal) {
			if (response2.trim().equalsIgnoreCase(response1.trim())) {
				resultFound = true;
				rmStatus="RM3";
				return response2;
			} else if (response3.trim().equalsIgnoreCase(response1.trim())) {
				resultFound = true;
				rmStatus="RM2";
				return response3;
			} else {
				resultFound = true;
				rmStatus="RM3";
				return response1;
			}

		}

		return output;
	}

	@Override
	public String crashingServer(int status) {
		// TODO Auto-generated method stub
		String response = "";
		String udpMessage = ApplicationConstant.OP_CRASH_SERVER + "," + status;
		System.out.println("CRASH SERVER" + udpMessage);

		response = sendUDPRequestForCrashFailure(ApplicationConstant.UDP_REPLICA_MANAGER_PORT, udpMessage,"localhost");

		return response;

	}

	
	public String byzantineNotify() {
		// TODO Auto-generated method stub
		String response = "";
		String udpMessage = ApplicationConstant.OP_BYZANTINE;
		System.out.println("BYZANTINE NOTIFY" + udpMessage);
		response = sendUDPRequestForCrashFailure(ApplicationConstant.UDP_REPLICA_MANAGER_PORT, udpMessage,addressMap.get(rmStatus));

		return response;

	}
}
