package org.psem2m.isolates.loggers.shell;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.service.command.Descriptor;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.loggers.CLoggingConditionsException;
import org.psem2m.isolates.loggers.ILoggingConditions;
import org.psem2m.isolates.loggers.ILoggingConditionsManager;
import org.psem2m.utilities.CXException;

@Component(name = "psem2m-logging-condition-commands-Factory")
@Instantiate(name = "psem2m-logging-condition-commands")
@Provides(specifications = { CCpntLoggingConditionCommands.class })
public class CCpntLoggingConditionCommands extends CAbstractCommands {

	/** The Gogo commands */
	@ServiceProperty(name = "osgi.command.function", value = "{lcolist,lcoclean,lcofile,lcofiles,lconew,lcoremove}")
	private String[] pCommands;

	@Requires
	private IIsolateLoggerSvc pLogger;

	@Requires
	private ILoggingConditions pLoggingConditions;

	@Requires
	private ILoggingConditionsManager pLoggingConditionsManager;

	private String pMemoConditionsId = null;

	/** The Gogo commands scope */
	@ServiceProperty(name = "osgi.command.scope", value = "logconditions")
	private String pScope;

	@Requires
	private IThreadContext pThreadContext;

	/**
	 * @param aArgs
	 * @return
	 * @throws IllegalArgumentException
	 */
	private String checkConditionsIdInArg0(final String[] aArgs)
			throws IllegalArgumentException {

		if (aArgs.length < 1 || "*".equals(aArgs[0])) {
			if (!hasMemoConditionsId()) {
				throw new IllegalArgumentException(
						"1 argument mandatory : ConditionsId");
			}
		} else {
			setMemoConditionsId(aArgs[0]);
		}
		return getMemoConditionsId();
	}

	/**
	 * @return the momorized ConditionsId
	 */
	private String getMemoConditionsId() {
		return (hasMemoConditionsId()) ? pMemoConditionsId : "";
	}

	/**
	 * @return true if a ConditionsId is memorized
	 */
	private boolean hasMemoConditionsId() {
		return pMemoConditionsId != null && !pMemoConditionsId.isEmpty();
	}

	/**
	 *
	 */
	@Invalidate
	public void invalidate() {
		pLogger.logInfo(this, "invalidate", "Invalidating...");

		pLogger.logInfo(this, "invalidate", "Invalidated.");
	}

	/**
	 * @param aArgs
	 * @return
	 */
	@Descriptor("Delete the file of a logging conditions.")
	public String lcoclean(
			@Descriptor("1 args: conditionsId or *") final String[] aArgs) {
		final StringBuilder wSB = appendArgsInSB(new StringBuilder(
				"logconditions:lcoclean: "), aArgs);

		try {

			final String wConditionsId = checkConditionsIdInArg0(aArgs);

			final ILoggingConditions wLoggingConditions = pLoggingConditionsManager
					.newLoggingConditions(wConditionsId);

			logCurrentConditionsDescription(wSB, wLoggingConditions);

			final int wNbDeletedFile = pLoggingConditionsManager
					.cleanLoggingConditionsFile(wConditionsId);

			addLineInSB(wSB, " - log conditions [%s]: NbDeletedFile=[%d]",
					wConditionsId, wNbDeletedFile);

		} catch (Exception | Error e) {
			logErrorInSB(wSB, e);
		}

		return wSB.toString();
	}

	/**
	 * @param aArgs
	 * @return
	 */
	@Descriptor("Display the infos of the file of a logging conditions.")
	public String lcofile(
			@Descriptor("1 args: conditionsId or *") final String[] aArgs) {
		final StringBuilder wSB = appendArgsInSB(new StringBuilder(
				"logconditions:lcofile: "), aArgs);

		try {
			final String wConditionsId = checkConditionsIdInArg0(aArgs);

			final ILoggingConditions wLoggingConditions = pLoggingConditionsManager
					.newLoggingConditions(wConditionsId);

			logCurrentConditionsDescription(wSB, wLoggingConditions);

			final File wFile = wLoggingConditions.getFile();

			addLineInSB(wSB, " - log conditions [%s]: File=[%s]",
					wConditionsId, wFile);

		} catch (Exception | Error e) {
			logErrorInSB(wSB, e);
		}

		return wSB.toString();
	}

	/**
	 * @param aArgs
	 * @return
	 */
	@Descriptor("Display the infos of the files of the existing logging conditions.")
	public String lcofiles(@Descriptor("no args") final String[] aArgs) {
		final StringBuilder wSB = appendArgsInSB(new StringBuilder(
				"logconditions:lcofile: "), aArgs);

		try {
			final List<ILoggingConditions> wList = pLoggingConditionsManager
					.getLoggingConditions();

			addLineInSB(wSB, "Logging conditions list size=[%d]", wList.size());
			int wIdx = 0;
			for (final ILoggingConditions wLoggingConditions : wList) {
				addLineInSB(wSB,
						" - Logging conditions(%d): Id=[%s] File=[%s]", wIdx,
						wLoggingConditions.getId(),
						wLoggingConditions.getFile());
				wIdx++;
			}

		} catch (Exception | Error e) {
			logErrorInSB(wSB, e);
		}

		return wSB.toString();
	}

	/**
	 * @param aArgs
	 * @return
	 */
	@Descriptor("Display the list of the existing logging conditions.")
	public String lcolist(@Descriptor("0 args") final String[] aArgs) {
		final StringBuilder wSB = appendArgsInSB(new StringBuilder(
				"logconditions:lcolist: "), aArgs);

		try {

			final List<ILoggingConditions> wList = pLoggingConditionsManager
					.getLoggingConditions();

			addLineInSB(wSB, "Logging conditions list size=[%d]", wList.size());
			int wIdx = 0;
			for (final ILoggingConditions wLoggingConditions : wList) {
				addLineInSB(wSB, " - Logging conditions(%d): %s", wIdx,
						wLoggingConditions.toDescription());
				wIdx++;
			}

		} catch (Exception | Error e) {
			logErrorInSB(wSB, e);
		}

		return wSB.toString();
	}

	/**
	 * @param aArgs
	 * @return
	 */
	@Descriptor("Create a logging  conditions or one condition.")
	public String lconew(
			@Descriptor("3 args: conditionsId or *, conditionId, a Level name") final String[] aArgs) {
		final StringBuilder wSB = appendArgsInSB(new StringBuilder(
				"logconditions:lconew: "), aArgs);

		try {

			final String wConditionsId = checkConditionsIdInArg0(aArgs);

			if (pLoggingConditionsManager.hasLoggingConditions(wConditionsId)) {
				addLineInSB(wSB, " * Logging conditions[%s]: already exists",
						wConditionsId);
			}
			//
			else {
				final ILoggingConditions wLoggingConditions = pLoggingConditionsManager
						.newLoggingConditions(wConditionsId);

				logCurrentConditionsDescription(wSB, wLoggingConditions);

				addLineInSB(wSB, " - Logging conditions[%s]: Created=[%b]",
						wConditionsId, true);
			}
			if (aArgs.length == 3) {

				final ILoggingConditions wLoggingConditions = pLoggingConditionsManager
						.newLoggingConditions(wConditionsId);

				if (wLoggingConditions == null) {
					throw new CLoggingConditionsException(
							"Logging conditions [%s] must exists to set a condition",
							wConditionsId);
				}
				wLoggingConditions.newLoggingCondition(aArgs[1],
						Level.parse(aArgs[2]), "Comment...");

				logCurrentConditionsDescription(wSB, wLoggingConditions);

				addLineInSB(wSB, " - Logging conditions[%s]: Modified=[%b]",
						wConditionsId, true);

			}

		} catch (Exception | Error e) {
			logErrorInSB(wSB, e);
		}

		return wSB.toString();
	}

	/**
	 * @param aArgs
	 * @return
	 */
	public String lcoremove(final String[] aArgs) {
		final StringBuilder wSB = appendArgsInSB(new StringBuilder(
				"logconditions:lcoremove: "), aArgs);

		try {

			final String wConditionsId = checkConditionsIdInArg0(aArgs);

			if (!pLoggingConditionsManager.hasLoggingConditions(wConditionsId)) {
				throw new CLoggingConditionsException(
						"Logging conditions [%s] doesn't exist", wConditionsId);
			}

			final boolean wRemoved = pLoggingConditionsManager
					.removeLoggingConditions(wConditionsId);

			addLineInSB(wSB, " - Logging conditions[%s]: Removed=[%b]",
					wConditionsId, wRemoved);

		} catch (Exception | Error e) {
			logErrorInSB(wSB, e);
		}

		return wSB.toString();
	}

	/**
	 * @param aSB
	 * @param aLogChannel
	 */
	private void logCurrentConditionsDescription(final StringBuilder aSB,
			final ILoggingConditions aLoggingConditions) {

		addLineInSB(aSB, " + Current log conditions[%s]: %s",
				aLoggingConditions.getId(), aLoggingConditions.toDescription());
	}

	/**
	 * @param aSB
	 * @param e
	 */
	private void logErrorInSB(final StringBuilder aSB, final Throwable e) {
		aSB.append(String.format("\n###\nERROR: %s",
				CXException.eCauseMessagesInString(e)));

	}

	/**
	 * @param aMemoConditionsId
	 */
	private void setMemoConditionsId(final String aMemoConditionsId) {
		pMemoConditionsId = aMemoConditionsId;
	}

	/**
	 *
	 */
	@Validate
	public void validate() {
		pLogger.logInfo(this, "validate", "validating...");

		try {

		} catch (final Exception | Error e) {
			pLogger.logSevere(this, "validate", "ERROR: %s", e);
		}

		pLogger.logInfo(this, "validate", "validated");
	}

}
