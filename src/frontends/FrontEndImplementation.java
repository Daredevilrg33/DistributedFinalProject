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
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.omg.CORBA.ORB;

import dlms.FrontEndOperationsPOA;
import utilities.ApplicationConstant;

/**
 * @author Rohit Gupta
 *
 */
public class FrontEndImplementation extends FrontEndOperationsPOA {
	public Logger logger = Logger.getLogger(FrontEndImplementation.class.getName());

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
	public boolean addItem(String managerId, String itemId, String itemName, int quantity) {
		// TODO Auto-generated method stub
		String udpMessage = "AddItem," + managerId + "," + itemId + "," + itemName + "," + String.valueOf(quantity);
		sendUDPRequest(ApplicationConstant.UDP_SEQUENCER_PORT, udpMessage);

		return false;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dlms.FrontEndOperationsOperations#removeItem(java.lang.String,
	 * java.lang.String, int)
	 */
	@Override
	public boolean removeItem(String managerId, String itemId, int quantity) {
		// TODO Auto-generated method stub
		String udpMessage = "RemoveItem," + managerId + "," + itemId + "," + String.valueOf(quantity);
		sendUDPRequest(ApplicationConstant.UDP_SEQUENCER_PORT, udpMessage);

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dlms.FrontEndOperationsOperations#listItemAvailability(java.lang.String)
	 */
	@Override
	public String listItemAvailability(String managerId) {
		// TODO Auto-generated method stub
		String udpMessage = "ListItem," + managerId;
		sendUDPRequest(ApplicationConstant.UDP_SEQUENCER_PORT, udpMessage);

		return null;
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
		String udpMessage = "BorrowItem," + userId + "," + itemId + "," + String.valueOf(noOfDays);
		sendUDPRequest(ApplicationConstant.UDP_SEQUENCER_PORT, udpMessage);
		return null;
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
		String udpMessage = "FindItem," + userId + "," + itemName;
		sendUDPRequest(ApplicationConstant.UDP_SEQUENCER_PORT, udpMessage);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dlms.FrontEndOperationsOperations#returnItem(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public boolean returnItem(String userId, String itemId) {
		// TODO Auto-generated method stub
		String udpMessage = "ReturnItem," + userId + "," + itemId;
		sendUDPRequest(ApplicationConstant.UDP_SEQUENCER_PORT, udpMessage);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dlms.FrontEndOperationsOperations#exchangeItem(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public boolean exchangeItem(String userId, String newItemId, String oldItemId) {
		// TODO Auto-generated method stub

		String udpMessage = "ExchangeItem," + userId + "," + newItemId + "," + oldItemId;
		sendUDPRequest(ApplicationConstant.UDP_SEQUENCER_PORT, udpMessage);
		return false;
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
}