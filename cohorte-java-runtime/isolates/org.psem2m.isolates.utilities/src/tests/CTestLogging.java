/**
 * 
 */
package tests;

import org.psem2m.utilities.logging.CActivityLoggerBasicConsole;
import org.psem2m.utilities.logging.IActivityLogger;

/**
 * @author ogattaz
 *
 */
public class CTestLogging {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			new CTestLogging().doTest();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 */
	CTestLogging(){
		super();
	}
	
	/**
	 * 
	 */
	void doTest(){
		
		Throwable wThrowable = new Exception("Message de l'exception");

		
		IActivityLogger wLogger = CActivityLoggerBasicConsole.getInstance();
		

		wLogger.logInfo(this, "doTest", "Ligne log info");
		wLogger.logDebug(this, "doTest", "Ligne log debug");
		wLogger.logWarn(this, "doTest", "Ligne log warning");
		wLogger.logSevere(this, "doTest", "Message d'erreur",wThrowable);
		wLogger.logInfo(this, "doTest", "Ligne log info");
		wLogger.close();

	}

}
