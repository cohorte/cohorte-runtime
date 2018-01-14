package org.psem2m.isolates.base;

import java.io.PrintStream;
import java.util.logging.Level;

import org.psem2m.utilities.CXArray;
import org.psem2m.utilities.CXException;

/**
 * MOD_OG_1.0.17 creation
 * 
 * @author ogattaz
 *
 */
public abstract class CAbstractGoGoCommand {

	public abstract IIsolateLoggerSvc getLogger();

	/**
	 * One of Gogo’s most simplifying features: System.out is the preferable way to
	 * create output.
	 * 
	 * However, Gogo uses Threadio, which is a service that multiplexes System.out
	 * and System.err (and also System.in). Each thread is associated with its own
	 * triplet of streams. So as long as you print to sysout inside a command then
	 * any Gogo user will get the information even if they run the shell remotely.
	 * 
	 * @ see http://enroute.osgi.org/appnotes/gogo-cmd.html #Console Output
	 * 
	 * 
	 * @param aLevel
	 * @param aWhat
	 * @param format
	 * @param args
	 */
	public void logTwice(final Level aLevel, final String aWhat, final String format, final Object... args) {
		String wLine = String.format(format, args);
		@SuppressWarnings("resource")
		PrintStream wGoGoStream = (Level.SEVERE.equals(aLevel)) ? System.err : System.out;
		wGoGoStream.println(wLine);
		getLogger().log(aLevel, this, aWhat, wLine);
	}

	/**
	 * @param aWhat
	 * @param format
	 * @param args
	 */
	public void logTwiceInfo(final String aWhat, final String format, final Object... args) {
		logTwice(Level.INFO, aWhat, format, args);
	}

	/**
	 * @param aWhat
	 * @param format
	 * @param args
	 */
	public void logTwiceSevere(final String aWhat, final String format, final Object... args) {
		logTwice(Level.SEVERE, aWhat, format, args);
	}

	/**
	 * @param aWhat
	 * @param format
	 * @param args
	 */
	public void logTwiceSevere(final String aWhat, final Throwable aThrowable) {
		logTwice(Level.SEVERE, aWhat, "ERROR: %s", CXException.eInString(aThrowable));
	}

	/**
	 * @param aWhat
	 * @param aThrowable
	 * @param format
	 * @param args
	 */
	public void logTwiceSevere(final String aWhat, final Throwable aThrowable, final String format,
			final Object... args) {
		logTwice(Level.SEVERE, aWhat, format + "ERROR: %s", CXArray.appendOneObject(args, aThrowable));
	}

	/**
	 * @param aWhat
	 * @param format
	 * @param args
	 */
	public void logTwiceWarn(final String aWhat, final String format, final Object... args) {
		logTwice(Level.WARNING, aWhat, format, args);
	}
}
