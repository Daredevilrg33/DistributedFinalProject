/**
 * 
 */
package replicamanagers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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

}
