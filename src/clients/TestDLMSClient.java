/**
 * 
 */
package clients;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import dlms.FrontEndOperations;
import dlms.FrontEndOperationsHelper;

/**
 * @author Rohit Gupta
 *
 */
public class TestDLMSClient {
	static FrontEndOperations frontEndOperations;

	public static void main(String[] args) {
		try {
			ORB orb = ORB.init(args, null);
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			frontEndOperations = (FrontEndOperations) FrontEndOperationsHelper.narrow(ncRef.resolve_str("frontend"));

		} catch (InvalidName e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotFound e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CannotProceed e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Runnable task = () -> {
			System.out.println(Thread.currentThread().getName());
			System.out.println(frontEndOperations.borrowItem("CONU0001", "CON1011", 3));
			frontEndOperations.exchangeItem("CONU0001", "CON1025", "CON1011");
			System.out.println("Item has been exchanged successfully.");
			System.out.println("New Item Borrowed: " + "CON1025");
			System.out.println("Item returned: " + "CON1011");

//			} else
			System.out.println("Exchange operation unsuccessful.");
			frontEndOperations.returnItem("CONU0001", "CON1025");
//				if () {
			System.out.println("The item CON1025 has been successfully returned by CONU0001.");

//			} else
			System.out.println("The return operation was unsuccessful.");

			System.out.println(Thread.currentThread().getName());
		};
		Runnable task2 = () -> {
			System.out.println(Thread.currentThread().getName());
			System.out.println(frontEndOperations.borrowItem("MCGU0005", "CON1011", 3));
			frontEndOperations.exchangeItem("MCGU0005", "CON1025", "CON1011");
//			if () {
			System.out.println("Item has been exchanged successfully.");
			System.out.println("New Item Borrowed: " + "CON1025");
			System.out.println("Item returned: " + "CON1011");
//			} else
			System.out.println("Exchange operation unsuccessful.");
//				frontEndOperations.returnItem("MCGU0005", "CON125")
//			if () {
			System.out.println("The item CON1025 has been successfully returned by MCGU0005.");

//			} else
			System.out.println("The return operation was unsuccessful.");

			System.out.println(Thread.currentThread().getName());
		};

		Runnable task3 = () -> {
			System.out.println(Thread.currentThread().getName());
			System.out.println(frontEndOperations.borrowItem("CONU0001", "MON1030", 3));

			frontEndOperations.exchangeItem("CONU0001", "MCG1080", "MON1030");
//		if () {
			System.out.println("Item has been exchanged successfully.");
			System.out.println("New Item Borrowed: " + "MCG1080");
			System.out.println("Item returned: " + "MON1030");

//		} else
			System.out.println("Exchange operation unsuccessful.");
			frontEndOperations.returnItem("CONU0001", "MCG1080");
//		if () {
			System.out.println("The item MCG1080 has been successfully returned by CONU0001.");
//		} else
			System.out.println("The return operation was unsuccessful.");

			System.out.println(Thread.currentThread().getName());
		};

		Thread thread = new Thread(task);
		Thread thread2 = new Thread(task2);
		Thread thread3 = new Thread(task3);
		thread.start();
		thread2.start();
		thread3.start();

	}
}
