package org.psem2m.isolates.loggers.shell;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.psem2m.utilities.CXArray;
import org.psem2m.utilities.CXStringUtils;

/**
 * @author ogattaz
 *
 */
public abstract class CAbstractCommands {

	private static SimpleDateFormat sDateFormater = new SimpleDateFormat(
			"yyyy/MM/dd HH:mm:ss:SSS");

	private static String getTS() {
		return sDateFormater.format(new Date());
	}

	protected CAbstractCommands() {
		super();
	}

	/**
	 * @param aSB
	 * @param aFormat
	 * @param aArgs
	 * @return
	 */
	protected StringBuilder addLineInSB(final StringBuilder aSB,
			final String aFormat, final Object... aArgs) {

		String[] wLines = String.format(aFormat, aArgs).split("\n");
		if (wLines.length > 0) {
			aSB.append(formatFirstLine(wLines[0]));
			if (wLines.length > 1) {
				wLines = (String[]) CXArray.removeOneObject(wLines, 0);
				for (final String wLine : wLines) {
					aSB.append(formatOtherLines(wLine));
				}
			}
		}

		return aSB;
	}

	/**
	 * @param aSB
	 * @param aArgs
	 */
	protected StringBuilder appendArgsInSB(final StringBuilder aSB,
			final String[] aArgs) {
		addLineInSB(aSB, "...");
		if (aArgs != null && aArgs.length > 0) {
			addLineInSB(aSB, "Args: %s",
					CXStringUtils.stringTableToString(aArgs));
		} else {
			addLineInSB(aSB, "No Args");
		}
		return aSB;
	}

	/**
	 * @param aLine
	 * @return
	 */
	private String formatFirstLine(final String aLine) {
		return String.format("\n%23s > %s", getTS(), aLine);
	}

	/**
	 * @param aLine
	 * @return
	 */
	private String formatOtherLines(final String aLine) {
		return String.format("\n%23s > %s", " -", aLine);
	}

}
