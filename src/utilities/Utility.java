/**
 * 
 */
package utilities;

import java.util.logging.Logger;

/**
 * @author Rohit Gupta
 *
 */
public class Utility {

	
	public static boolean validateUserId(String userId) {
		boolean isValid = false;
		if (userId.trim().length() > 8) {
			isValid = false;
		} else {
			String value = userId.substring(0, 3);
			if (value.equalsIgnoreCase("CON") || value.equalsIgnoreCase("MCG") || value.equalsIgnoreCase("MON")) {
				isValid = true;
			} else
				isValid = false;
			char user = userId.charAt(3);
			if (user == 'M' || user == 'm' || user == 'U' || user == 'u')
				isValid = true;
			else
				isValid = false;
		}
		return isValid;
	}

	public static void log(String message,Logger logger) {
		logger.info(message);
		System.out.println(message);
	}
	

}
