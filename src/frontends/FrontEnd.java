/**
 * 
 */
package frontends;

import java.net.DatagramSocket;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import dlms.FrontEndOperations;
import dlms.FrontEndOperationsHelper;

/**
 * @author Rohit Gupta
 *
 */
public class FrontEnd {

	public static void main(String[] args) {

		try {
			ORB orb = ORB.init(args, null);
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();
			// create servant and register it with the ORB
			FrontEndImplementation frontEndImplementation = new FrontEndImplementation();
			frontEndImplementation.logging("Front End");
			frontEndImplementation.setORB(orb);
			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(frontEndImplementation);
			FrontEndOperations frontEndOperations = FrontEndOperationsHelper.narrow(ref);
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			NameComponent path[] = ncRef.to_name("frontend");
			ncRef.rebind(path, frontEndOperations);
			System.out.println("Concordia Server Running...");
			Runnable task = () -> {
				receive(frontEndImplementation);
			};
			Thread thread = new Thread(task);
			thread.start();
			for (;;) {
				orb.run();
			}

		} catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace();
		}

	}

	/**
	 * Recieve UDP request from RMs and check the results and then send result back
	 * to client.
	 * 
	 * @param libraryImplementation
	 */
	private static void receive(FrontEndImplementation libraryImplementation) {
		DatagramSocket aSocket = null;
	}
}
