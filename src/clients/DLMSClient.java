/**
 * 
 */
package clients;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import dlms.FrontEndOperations;
import dlms.FrontEndOperationsHelper;
import models.UserModel;
import models.UserType;
import utilities.ApplicationConstant;
import utilities.Utility;

/**
 * @author Rohit Gupta
 *
 */
public class DLMSClient {
	private static Logger logger = Logger.getLogger(DLMSClient.class.getClass().getSimpleName());
	private static Scanner scanner;
	static FrontEndOperations frontEndOperations;

	public static void main(String[] args) {

		System.out.println("\t\t WELCOME TO DISTRIBUTED LIBRARY MANAGEMENT SYSTEM");
		scanner = new Scanner(System.in);
		try {
			ORB orb = ORB.init(args, null);
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			frontEndOperations = (FrontEndOperations) FrontEndOperationsHelper.narrow(ncRef.resolve_str("frontend"));

		} catch (InvalidName e) {
			// TODO Auto-generated catch block
			logger.info("Invalid name Exception: " + e.getMessage());
			e.printStackTrace();
		} catch (NotFound e) {
			// TODO Auto-generated catch block
			logger.info("Not Found Exception: " + e.getMessage());
			e.printStackTrace();
		} catch (CannotProceed e) {
			// TODO Auto-generated catch block
			logger.info("Cannot Proceed Exception: " + e.getMessage());
			e.printStackTrace();
		} catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
			// TODO Auto-generated catch block
			logger.info("Naming Context Exception: " + e.getMessage());
			e.printStackTrace();
		}

		welcomePortal();
	}

	private static void welcomePortal() {
		String userId = requestUserId();
		if (!Utility.validateUserId(userId)) {
			welcomePortal();
			return;
		}
		UserModel userModel = new UserModel(userId);
		logger = logging(userId);
		openOptionMenu(userModel);
	}

	/**
	 * @param userModel
	 */
	private static void openOptionMenu(UserModel userModel) {
		// TODO Auto-generated method stub
		int optionSelected = optionMenu(userModel.getUserType());
		if (userModel.getUserType() == UserType.MANAGER)
			performActionForManager(optionSelected, userModel);
		else
			performActionForUser(optionSelected, userModel);

	}

	/**
	 * @param optionSelected
	 * @throws RemoteException
	 */
	private static void performActionForUser(int optionSelected, UserModel userModel) {
		// TODO Auto-generated method stub
		switch (optionSelected) {
		case ApplicationConstant.USER_BORROW_ITEM:
			logger.info("Accessing the borrow item !!");
			log("Please enter Item Id and no of days using comma seperation.");
			String value = scanner.nextLine();
			String[] values = value.split(",");
			String itemId = values[0];
			int noOfDays = Integer.valueOf(values[1]);
			String message = frontEndOperations.borrowItem(userModel.getUserId(), itemId, noOfDays);
			log(message);
			openOptionMenu(userModel);
			break;
		case ApplicationConstant.USER_FIND_ITEM:
			logger.info("Accessing Find an Item !");
			log("Please enter item name: ");
			String itemName = scanner.nextLine();
			String response = frontEndOperations.findItem(userModel.getUserId(), itemName);
			if (!response.trim().isEmpty()) {
				log(response);
			} else
				log("NO ITEM FOUND !!!");
			openOptionMenu(userModel);
			break;
		case ApplicationConstant.USER_RETURN_ITEM:
			logger.info("Return an Item !");
			log("Please enter item id: ");
			String itemId1 = scanner.nextLine();
			log(frontEndOperations.returnItem(userModel.getUserId(), itemId1));
			// if (isItemReturned)
			// log("Item Returned Successfully !!!!!");
			// else
			// log("Unable to return the Item !!!!!");
			openOptionMenu(userModel);
			break;

		case ApplicationConstant.USER_EXCHANGE_ITEM:
			logger.info("Exchange an Item !");
			System.out.println("Please enter new Item Id and old Item Id using comma seperation.");
			String data = scanner.nextLine();
			String[] s = data.split(",");
			String newItemId = s[0].trim();
			String oldItemId = s[1].trim();
			log(frontEndOperations.exchangeItem(userModel.getUserId(), newItemId, oldItemId));
			// if (isItemExchanged)
			// log("Item has been exchanged successfully !!!!");
			// else
			// log("Unable to exchange the item !!!");
			openOptionMenu(userModel);
			break;
		case ApplicationConstant.USER_EXIT:
			logger.info("User has exited!!");
			welcomePortal();
			break;
		default:
			log("Invalid input value.");
			openOptionMenu(userModel);
			break;
		}
	}

	/**
	 * @param optionSelected
	 * @throws RemoteException
	 */
	private static void performActionForManager(int optionSelected, UserModel userModel) {
		// TODO Auto-generated method stub
		switch (optionSelected) {
		case ApplicationConstant.MANAGER_ADD_ITEM:
			logger.info(" Accessing add item !!");
			log("Please enter Item Id, Item Name and quantity using comma seperation.");
			String value = scanner.nextLine();
			String[] values = value.split(",");
			String itemId = values[0];
			String itemName = values[1];
			int quantity = Integer.valueOf(values[2]);
			log(frontEndOperations.addItem(userModel.getUserId(), itemId, itemName, quantity));
			// if (isItemAdded)
			// log("Item added successfully");
			// else
			// log("Item is not added");
			openOptionMenu(userModel);
			break;
		case ApplicationConstant.MANAGER_REMOVE_ITEM:
			logger.info(" Accessing remove item !!");
			log("Please enter Item Id and quantity using comma seperation.");
			String value1 = scanner.nextLine();
			String[] values1 = value1.split(",");
			String itemId1 = values1[0];
			int quantity1 = Integer.valueOf(values1[1]);
			log(frontEndOperations.removeItem(userModel.getUserId(), itemId1, quantity1));
			// if (isItemRemoved)
			// log("Item Removed Successfully");
			// else
			// log("Item not removed");
			openOptionMenu(userModel);
			break;
		case ApplicationConstant.MANAGER_LIST_ITEM_AVAILABILITY:
			logger.info(" Accessing List Item Availability !!");
			String response = frontEndOperations.listItemAvailability(userModel.getUserId());
			log(response);
			openOptionMenu(userModel);
			break;
		case ApplicationConstant.MANAGER_EXIT:
			logger.info(" Manager has exited !!");
			welcomePortal();
			break;
		default:
			log("Invalid input value.");
			openOptionMenu(userModel);
			break;
		}
	}

	/**
	 * @param scanner
	 * @param userType
	 * @return
	 */
	private static int optionMenu(UserType userType) {
		// TODO Auto-generated method stub
		log("Please select one of the operations: ");
		switch (userType) {
		case USER:
			System.out.println("1. Borrow Item");
			System.out.println("2. Find Item");
			System.out.println("3. Return Item");
			System.out.println("4. Exchange Item");
			System.out.println("5. Exit");
			break;
		case MANAGER:
			System.out.println("1. Add Item ");
			System.out.println("2. Remove Item ");
			System.out.println("3. List Item Availability");
			System.out.println("4. Exit");
			break;
		default:
			break;
		}
		String option = scanner.nextLine();
		int optionSelected = Integer.valueOf(option);
		return optionSelected;
	}

	private static String requestUserId() {
		System.out.println("Please enter your ID:");
		String userId = scanner.nextLine();
		return userId;
	}

	private static void log(String message) {
		logger.info(message);
		System.out.println(message);
	}

	public static Logger logging(String fileName) {
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
		return logger;
	}
}
