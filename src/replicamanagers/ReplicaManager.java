/**
 * 
 */
package replicamanagers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.PriorityQueue;
import java.util.Queue;

import servers.ServerConcordia;
import servers.ServerMcgill;
import servers.ServerMontreal;
import utilities.ApplicationConstant;

/**
 * @author Rohit Gupta
 *
 */
public class ReplicaManager {
	static Queue<Integer> pwaitListQueue = new PriorityQueue<Integer>();
	static LinkedHashMap<Integer, String> historyBuffer = new LinkedHashMap<Integer, String>();
	static int seqCount = 0;
	static Thread conThread, monThread, mcgThread;
	static ServerConcordia conServer;
	static ServerMontreal monServer;
	static ServerMcgill mcgServer;

	public static void main(String[] args) {
		Runnable task = () -> {
			recieveMessage();
		};
		Thread thread = new Thread(task);
		thread.start();
		startingServer();
		// Runnable task1 = () -> {
		// recieveMessageForCrashRecovery();
		// };
		// Thread thread1 = new Thread(task1);
		// thread1.start();

	}

	// private static void recieveMessageForCrashRecovery() {
	// // TODO Auto-generated method stub
	// DatagramSocket aSocket = null;
	// try {
	// aSocket = new DatagramSocket(ApplicationConstant.UDP_RM_RECOVERY_PORT);
	// byte[] buffer = new byte[1000];// to stored the received data from
	// // the client.
	// System.out.println("Recovery Server Started............");
	// while (true) {// non-terminating loop as the server is always in listening
	// mode.
	// DatagramPacket request = new DatagramPacket(buffer, buffer.length);
	//
	// // Server waits for the request to come
	// aSocket.receive(request);
	// // request received
	// String requestData = new String(request.getData(), request.getOffset(),
	// request.getLength());
	//
	// System.out.println("Request received from client for Recovery: " +
	// requestData.trim());
	//// String response = performAction(requestData.trim());
	//
	// DatagramPacket reply = new DatagramPacket(response.trim().getBytes(),
	// response.trim().getBytes().length,
	// request.getAddress(), request.getPort());// reply packet ready
	// aSocket.send(reply);// reply sent
	// }
	// } catch (SocketException e) {
	// System.out.println("Socket: " + e.getMessage());
	// } catch (IOException e) {
	// System.out.println("IO: " + e.getMessage());
	// } finally {
	// if (aSocket != null)
	// aSocket.close();
	// }
	// }

	public static void startingServer() {
		conServer = new ServerConcordia();
		conThread = new Thread(conServer);
		monServer = new ServerMontreal();
		monThread = new Thread(monServer);
		mcgServer = new ServerMcgill();
		mcgThread = new Thread(mcgServer);
		conThread.run();
		monThread.run();
		mcgThread.run();
	}

	private static void recieveMessage() {
		MulticastSocket aSocket = null;
		try {

			aSocket = new MulticastSocket(ApplicationConstant.UDP_REPLICA_MANAGER_PORT);

			aSocket.joinGroup(InetAddress.getByName("230.1.1.2"));

			System.out.println("Server Started............");

			while (true) {
				byte[] buffer = new byte[1000];
				System.out.println("recieveMessage");
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				String requestData = new String(request.getData(), request.getOffset(), request.getLength());
				System.out.println("************ request message: " + requestData);

				String replyMessage = "RM1:";

				String response = performAction(requestData.trim());
				if (!response.trim().isEmpty()) {
					replyMessage = replyMessage.concat(response.trim());
					System.out.println("replyMessage  " + replyMessage);
					InetAddress aHost = InetAddress.getByName("localhost");
					// if (!replyMessage.equalsIgnoreCase("Reply : ")) {
					DatagramPacket reply = new DatagramPacket(replyMessage.getBytes(), replyMessage.getBytes().length,
							aHost, ApplicationConstant.UDP_FRONT_END_PORT);
					aSocket.send(reply);
				}
				// }
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
		String methodName = requestParams[0];
		System.out.println("methodName" + methodName);
		if (methodName.trim().equalsIgnoreCase(ApplicationConstant.OP_CRASH_SERVER)) {

			boolean isCrashed = handlingCrashFailure(requestParams[1]);
			if (isCrashed)
				outputMessage = "System Crashed";
			else
				outputMessage = "System Recovered from crashed";
		} else {
			int sequenceNumber = Integer.parseInt(requestParams[0].trim());
			System.out.println("sequenceNumber" + sequenceNumber);
			System.out.println("historyBuffer" + historyBuffer);
			System.out.println("pwaitListQueue" + pwaitListQueue);
			if (!historyBuffer.containsKey(sequenceNumber)) {
				pwaitListQueue.add(sequenceNumber);
				historyBuffer.put(sequenceNumber, requestData);
				String action = requestParams[1].trim();
				String managerId = requestParams[2].trim();
				int value = seqCount + 1;
				if (pwaitListQueue.peek() == value) {
					while (!pwaitListQueue.isEmpty()) {
						int seqNo = pwaitListQueue.peek();
						String request = historyBuffer.get(seqNo);
						String[] requestParams1 = request.split(",");
						String managerId1 = requestParams1[2].trim();
						outputMessage = sendUDPRequestToServer(getServerPort(managerId1), request);

						seqCount = seqCount + 1;
						pwaitListQueue.poll();
					}
				} else {
					System.out.println("Waiting for required request");
				}
			}

		}

		return outputMessage;

	}

	public static boolean handlingCrashFailure(String opt) {
		boolean isCrashed = false;
		if (Integer.valueOf(opt) < 0) {
			conServer.aSocket.close();
			monServer.aSocket.close();
			mcgServer.aSocket.close();

			isCrashed = true;
		} else {
			System.out.println("Crash Recovery Code Running");
			try {
				conServer.aSocket = new DatagramSocket(null);
				conServer.aSocket.setReuseAddress(true);
				conServer.aSocket.bind(new InetSocketAddress(InetAddress.getByName("localhost"),
						ApplicationConstant.UDP_CONCORDIA_PORT));

				mcgServer.aSocket = new DatagramSocket(null);
				mcgServer.aSocket.setReuseAddress(true);
				mcgServer.aSocket.bind(
						new InetSocketAddress(InetAddress.getByName("localhost"), ApplicationConstant.UDP_MCGILL_PORT));

				monServer.aSocket = new DatagramSocket(null);
				monServer.aSocket.setReuseAddress(true);
				monServer.aSocket.bind(new InetSocketAddress(InetAddress.getByName("localhost"),
						ApplicationConstant.UDP_MONTREAL_PORT));

			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Before Accessing history Buffer" + historyBuffer.size());
			// ServerConcordia conServer = new ServerConcordia();
			if (historyBuffer.size() > 0) {
				for (int i = 1; i < seqCount; i++) {
					System.out.println("Accessing history Buffer");
					String request = historyBuffer.get(i);
					System.out.println("Accessing history Buffer request " + request);

					String[] reqParams = request.split(",");
					String managerId = reqParams[2].trim();
					String response = sendUDPRequestToServer(getServerPort(managerId), request);
					System.out.println("Response Messaghes " + response);
				}
			}
			isCrashed = false;
		}
		return isCrashed;
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
