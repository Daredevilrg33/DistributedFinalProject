package clients;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import dlms.FrontEndOperations;
import dlms.FrontEndOperationsHelper;
import utilities.ApplicationConstant;

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
			String result = frontEndOperations.borrowItem("CONU0001", "CON0001", 3);
			System.out.println("result " + result);
			if (result.equalsIgnoreCase(ApplicationConstant.MSG_BORROW_ITEM_SUCCESSFULLY))
				System.out.println("Item CON0001 has been borrowed successfully !!");
			else
				System.out.println("Unable to borrow Item.");

			result = frontEndOperations.exchangeItem("CONU0001", "CON0002", "CON0001");
			System.out.println("result " + result);
			if (result.equalsIgnoreCase(ApplicationConstant.MSG_ITEM_EXCHANGE_SUCCESSFULLY)) {
				System.out.println("Item has been exchanged successfully.");
				System.out.println("New Item Borrowed: " + "CON0002");
				System.out.println("Item returned: " + "CON0001");
			} else
				System.out.println("Exchange operation unsuccessful.");

			result = frontEndOperations.returnItem("CONU0001", "CON0002");
			System.out.println("result " + result);

			if (result.equalsIgnoreCase(ApplicationConstant.MSG_ITEM_RETURNED_SUCCESSFULLY))
				System.out.println("The item CON0002 has been successfully returned by CONU0001.");
			else
				System.out.println("The return operation was unsuccessful.");

			System.out.println(Thread.currentThread().getName());
		};
		Runnable task2 = () -> {
			System.out.println(Thread.currentThread().getName());
			String result = frontEndOperations.borrowItem("MCGU0001", "CON0001", 3);
			if (result.equalsIgnoreCase(ApplicationConstant.MSG_BORROW_ITEM_SUCCESSFULLY))
				System.out.println("Item CON0001 has been borrowed successfully.");
			else
				System.out.println("Unable to borrow Item.");

			result = frontEndOperations.exchangeItem("MCGU0001", "CON0002", "CON0001");
			if (result.equalsIgnoreCase(ApplicationConstant.MSG_ITEM_EXCHANGE_SUCCESSFULLY)) {
				System.out.println("Item has been exchanged successfully.");
				System.out.println("New Item Borrowed: " + "CON0002");
				System.out.println("Item returned: " + "CON0001");
			} else
				System.out.println("Exchange operation unsuccessful.");

			result = frontEndOperations.returnItem("MCGU0001", "CON0002");
			if (result.equalsIgnoreCase(ApplicationConstant.MSG_ITEM_RETURNED_SUCCESSFULLY))
				System.out.println("The item CON0002 has been successfully returned by MCGU0001.");
			else
				System.out.println("The return operation was unsuccessful.");

			System.out.println(Thread.currentThread().getName());
		};

		Runnable task3 = () -> {
			System.out.println(Thread.currentThread().getName());
			String result = frontEndOperations.borrowItem("CONU0001", "MCG0001", 3);
			if (result.equalsIgnoreCase(ApplicationConstant.MSG_BORROW_ITEM_SUCCESSFULLY))
				System.out.println("Item MCG0001 has been borrowed successfully.");
			else
				System.out.println("Unable to borrow Item.");

			result = frontEndOperations.exchangeItem("CONU0001", "MON0001", "MCG0001");
			if (result.equalsIgnoreCase(ApplicationConstant.MSG_ITEM_EXCHANGE_SUCCESSFULLY)) {
				System.out.println("Item has been exchanged successfully.");
				System.out.println("New Item Borrowed: " + "MON0001");
				System.out.println("Item returned: " + "MCG0001");
			} else
				System.out.println("Exchange operation unsuccessful.");

			result = frontEndOperations.returnItem("CONU0001", "MON0001");
			if (result.equalsIgnoreCase(ApplicationConstant.MSG_ITEM_RETURNED_SUCCESSFULLY))
				System.out.println("The item MON0001 has been successfully returned by CONU0001.");
			else
				System.out.println("The return operation was unsuccessful.");

			System.out.println(Thread.currentThread().getName());
		};

		Runnable task4 = () -> {
			System.out.println(Thread.currentThread().getName());
			String result = frontEndOperations.addItem("CONM0001", "CON2121", "TestItem", 1);
			if (result.equalsIgnoreCase(ApplicationConstant.MSG_ADD_ITEM_ADDED))
				System.out.println("Item CON2121 has been added successfully.");
			else
				System.out.println("Unable to add item.");

			result = frontEndOperations.borrowItem("CONU0001", "CON2121", 2);
			if (result.equalsIgnoreCase(ApplicationConstant.MSG_BORROW_ITEM_SUCCESSFULLY))
				System.out.println("Item CON2121 has been borrowed successfully.");
			else
				System.out.println("Unable to borrow item.");

			result = frontEndOperations.borrowItem("CONU0002", "CON2121", 3);
			if (result.equalsIgnoreCase(ApplicationConstant.MSG_BORROW_ITEM_SUCCESSFULLY))
				System.out.println("Item CON2121 has been borrowed successfully.");
			else
				System.out.println("Unable to borrow item.");

			result = frontEndOperations.returnItem("CONU0001", "CON2121");
			if (result.equalsIgnoreCase(ApplicationConstant.MSG_ITEM_RETURNED_SUCCESSFULLY))
				System.out.println("The item CON2121 has been successfully returned by CONU0001.");
			else
				System.out.println("The return operation was unsuccessful.");
			result = frontEndOperations.returnItem("CONU0002", "CON2121");
			if (result.equalsIgnoreCase(ApplicationConstant.MSG_ITEM_RETURNED_SUCCESSFULLY))
				System.out.println("The item CON2121 has been successfully returned by CONU0002.");
			else
				System.out.println("The return operation was unsuccessful.");

		};
		Runnable task5 = () -> {
			System.out.println(Thread.currentThread().getName());
			String result = frontEndOperations.addItem("CONM0001", "CON3131", "TestItem2", 1);
			if (result.equalsIgnoreCase(ApplicationConstant.MSG_ADD_ITEM_ADDED))
				System.out.println("Item CON3131 has been added successfully.");
			else
				System.out.println("Unable to add item.");

			result = frontEndOperations.borrowItem("CONU0001", "CON3131", 2);
			if (result.equalsIgnoreCase(ApplicationConstant.MSG_BORROW_ITEM_SUCCESSFULLY))
				System.out.println("Item CON3131 has been borrowed successfully.");
			else
				System.out.println("Unable to borrow item.");

			result = frontEndOperations.borrowItem("CONU0002", "CON3131", 3);
			if (result.equalsIgnoreCase(ApplicationConstant.MSG_BORROW_ITEM_SUCCESSFULLY))
				System.out.println("Item CON3131 has been borrowed successfully.");
			else
				System.out.println("Unable to borrow item.");

			result = frontEndOperations.addItem("CONM0001", "CON3131", "TestItem2", 5);
			if (result.equalsIgnoreCase(ApplicationConstant.MSG_ADD_ITEM_ADDED))
				System.out.println("Item CON3131 has been added successfully.");
			else
				System.out.println("Unable to add item.");

			result = frontEndOperations.returnItem("CONU0001", "CON3131");
			if (result.equalsIgnoreCase(ApplicationConstant.MSG_ITEM_RETURNED_SUCCESSFULLY))
				System.out.println("The item CON3131 has been successfully returned by CONU0001.");
			else
				System.out.println("The return operation was unsuccessful.");
			result = frontEndOperations.returnItem("CONU0002", "CON3131");
			if (result.equalsIgnoreCase(ApplicationConstant.MSG_ITEM_RETURNED_SUCCESSFULLY))
				System.out.println("The item CON3131 has been successfully returned by CONU0002.");
			else
				System.out.println("The return operation was unsuccessful.");
		};

		Thread thread = new Thread(task);
		Thread thread2 = new Thread(task2);
		Thread thread3 = new Thread(task3);
		Thread thread4 = new Thread(task4);
		Thread thread5 = new Thread(task5);
		thread.start();
		// thread2.start();
		// thread3.start();
		// thread4.start();
		// thread5.start();

	}
}
