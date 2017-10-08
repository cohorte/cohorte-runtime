package org.psem2m.isolates.loggers.shell;

import java.io.File;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.service.command.Descriptor;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.loggers.ILogChannelSvc;
import org.psem2m.isolates.loggers.ILogChannelsSvc;
import org.psem2m.utilities.CXException;
import org.psem2m.utilities.CXLoremIpsum;
import org.psem2m.utilities.CXStringUtils;

/**
 * MOD_OG_CASE_0000211 Associates a LogChannel to a thread to trace explicitly a
 * request
 *
 * @author ogattaz
 *
 */
@Component(name = "psem2m-loggers-channels-commands-Factory")
@Instantiate(name = "psem2m-loggers-channels-commands")
@Provides(specifications = { CCpntLogChannelCommands.class })
public class CCpntLogChannelCommands extends CAbstractCommands {

	/** The Gogo commands */
	@ServiceProperty(name = "osgi.command.function", value = "{lclist,lcclean,lcfile,lcfiles,lclog,lcnew,lcremove,lcsimulate}")
	private String[] pCommands;

	@Requires
	private ILogChannelsSvc pILogChannels;

	@Requires
	private IIsolateLoggerSvc pLogger;

	private String pMemoChannelId = null;

	/** The Gogo commands scope */
	@ServiceProperty(name = "osgi.command.scope", value = "logchannels")
	private String pScope;

	@Requires
	private IThreadContext pThreadContext;

	@Requires
	private CCpntThreadContextTester pThreadContextTester;

	/**
	 *
	 */
	public CCpntLogChannelCommands() {
		super();
	}

	/**
	 * @param aArgs
	 * @return
	 * @throws IllegalArgumentException
	 */
	private String checkChannelIdInArg0(final String[] aArgs)
			throws IllegalArgumentException {

		if (aArgs.length < 1 || "*".equals(aArgs[0])) {
			if (!hasMemoChannelId()) {
				throw new IllegalArgumentException(
						"1 argument mandatory : ChannelID");
			}
		} else {
			setMemoChannelId(aArgs[0]);
		}
		return getMemoChannelId();
	}

	/**
	 * @return the momorized ChannelId
	 */
	private String getMemoChannelId() {
		return (hasMemoChannelId()) ? pMemoChannelId : "";
	}

	/**
	 * @return true if a ChannelId is memorized
	 */
	private boolean hasMemoChannelId() {
		return pMemoChannelId != null && !pMemoChannelId.isEmpty();
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
	public String lcclean(final String[] aArgs) {
		final StringBuilder wSB = appendArgsInSB(new StringBuilder(
				"iotlogchannel:lcclean: "), aArgs);

		try {

			final String wChannelId = checkChannelIdInArg0(aArgs);

			final ILogChannelSvc wLogChannel = pILogChannels
					.getLogChannel(wChannelId);

			logCurrentChannelDescription(wSB, wLogChannel);

			final int wNbDeletedFile = pILogChannels
					.cleanLogChannelFiles(wChannelId);

			addLineInSB(wSB, " - LogChannel[%s]: NbDeletedFile=[%d]",
					wChannelId, wNbDeletedFile);

		} catch (Exception | Error e) {
			logErrorInSB(wSB, e);
		}

		return wSB.toString();
	}

	/**
	 * @param aArgs
	 *            the 2 optionnal arguments of the command
	 * @return the text having to be displayed in the console
	 */
	@Descriptor("Display infos of the file idx of a channel. wait 2 args:")
	public String lcfile(
			@Descriptor("2 args: channelId or * + file index") final String[] aArgs) {

		final StringBuilder wSB = appendArgsInSB(new StringBuilder(
				"iotlogchannel:lcfile: "), aArgs);

		try {

			final String wChannelId = checkChannelIdInArg0(aArgs);

			final ILogChannelSvc wLogChannel = pILogChannels
					.getLogChannel(wChannelId);

			logCurrentChannelDescription(wSB, wLogChannel);

			int wFileIdx = 0;
			if (aArgs.length >= 2) {
				wFileIdx = Integer.parseInt(aArgs[1]);
			}

			final File wLogFile = pILogChannels.getLogChannelFile(wChannelId,
					wFileIdx);

			addLineInSB(wSB, " - LogChannel[%s]: LogFile=[%s] isFIle=[%s]",
					wChannelId, wLogFile.getName(), wLogFile.isFile());

		} catch (Exception | Error e) {
			logErrorInSB(wSB, e);
		}

		return wSB.toString();
	}

	/**
	 * @param aArgs
	 * @return
	 */
	@Descriptor("Display infos of the files a channel.")
	public String lcfiles(
			@Descriptor("1 args: channelId or *") final String[] aArgs) {
		final StringBuilder wSB = appendArgsInSB(new StringBuilder(
				"iotlogchannel:lcfiles: "), aArgs);

		try {

			final String wChannelId = checkChannelIdInArg0(aArgs);

			final ILogChannelSvc wLogChannel = pILogChannels
					.getLogChannel(wChannelId);

			logCurrentChannelDescription(wSB, wLogChannel);

			final List<File> wLogFiles = pILogChannels
					.getLogChannelFiles(wChannelId);

			int wIdx = 0;
			for (final File wLogFile : wLogFiles) {
				addLineInSB(wSB, " - LogChannel[%s] FileIdx=[%d] : %s",
						wChannelId, wIdx, wLogFile.getAbsolutePath());
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
	@Descriptor("Display the list of the existing channels.")
	public String lclist(@Descriptor("needs no args") final String[] aArgs) {
		final StringBuilder wSB = appendArgsInSB(new StringBuilder(
				"iotlogchannel:lclist: "), aArgs);

		try {

			final List<ILogChannelSvc> wLogChannels = pILogChannels
					.getChannels();

			addLineInSB(wSB, "LogChannel list size=[%d]", wLogChannels.size());
			int wIdx = 0;
			for (final ILogChannelSvc wLogChannel : wLogChannels) {
				addLineInSB(wSB, " - LogChannel(%d): %s", wIdx,
						wLogChannel.toDescription());
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
	public String lclog(final String[] aArgs) {
		final StringBuilder wSB = appendArgsInSB(new StringBuilder(
				"iotlogchannel:lclog: "), aArgs);

		try {

			final String wChannelId = checkChannelIdInArg0(aArgs);

			final ILogChannelSvc wLogChannel = pILogChannels
					.getLogChannel(wChannelId);

			logCurrentChannelDescription(wSB, wLogChannel);

			// how many § to log
			int wNbParagraph = randomWithRange(1, 5);
			if (aArgs.length > 1) {
				wNbParagraph = Integer.parseInt(aArgs[1]);
			}

			// generate §
			final String[] wParagraphs = new CXLoremIpsum().getParagraphs(
					wNbParagraph).split("\n");

			// dump §
			final int wNbParagraphs = wParagraphs.length;
			wLogChannel.logInfo(this, "lclog", "----- Nb paragraphs=[%d] --",
					wNbParagraphs);
			int wIdx = 0;
			long wSize = 0;
			for (final String wParagraph : wParagraphs) {

				wLogChannel.logInfo(this, "lclog", "§(%d):%s ", wIdx,
						wParagraph);
				wIdx++;
				wSize += wParagraph.length();
			}
			wLogChannel.logInfo(this, "lclog",
					"----- printed paragraphs=[%d/%d] total size=[%d] --",
					wIdx, wNbParagraphs, wSize);

		} catch (Exception | Error e) {
			logErrorInSB(wSB, e);
		}

		return wSB.toString();
	}

	/**
	 * @param aArgs
	 * @return
	 */
	public String lcnew(final String[] aArgs) {
		final StringBuilder wSB = appendArgsInSB(new StringBuilder(
				"iotlogchannel:lcnew: "), aArgs);

		try {

			final String wChannelId = checkChannelIdInArg0(aArgs);

			final ILogChannelSvc wLogChannel = pILogChannels
					.getLogChannel(wChannelId);

			logCurrentChannelDescription(wSB, wLogChannel);

		} catch (Exception | Error e) {
			logErrorInSB(wSB, e);
		}

		return wSB.toString();
	}

	/**
	 * @param aArgs
	 * @return
	 */
	public String lcremove(final String[] aArgs) {
		final StringBuilder wSB = appendArgsInSB(new StringBuilder(
				"iotlogchannel:lcremove: "), aArgs);

		try {

			final String wChannelId = checkChannelIdInArg0(aArgs);

			final ILogChannelSvc wLogChannel = pILogChannels
					.getLogChannel(wChannelId);

			logCurrentChannelDescription(wSB, wLogChannel);

			final boolean wRemoved = pILogChannels.removeLogChannel(wChannelId);

			addLineInSB(wSB, " - LogChannel[%s]: Removed=[%b]", wChannelId,
					wRemoved);

		} catch (Exception | Error e) {
			logErrorInSB(wSB, e);
		}

		return wSB.toString();
	}

	/**
	 * @param aArgs
	 * @return
	 */
	public String lcsimulate(final String[] aArgs) {
		final StringBuilder wSB = appendArgsInSB(new StringBuilder(
				"iotlogchannel:lcsimulate: "), aArgs);

		try {
			final String wChannelId = checkChannelIdInArg0(aArgs);

			final ILogChannelSvc wLogChannel = pILogChannels
					.getLogChannel(wChannelId);

			logCurrentChannelDescription(wSB, wLogChannel);

			pThreadContext.setDebugChannel(wLogChannel);

			pThreadContextTester.myMethod();

		} catch (Exception | Error e) {
			logErrorInSB(wSB, e);
		}

		return wSB.toString();
	}

	/**
	 * @param aSB
	 * @param aLogChannel
	 */
	private void logCurrentChannelDescription(final StringBuilder aSB,
			final ILogChannelSvc aLogChannel) {

		addLineInSB(aSB, " + Current log channel[%s]: %s", aLogChannel.getId(),
				aLogChannel.toDescription());
	}

	/**
	 * @param aSB
	 * @param e
	 */
	private void logErrorInSB(final StringBuilder aSB, final Throwable e) {
		aSB.append(String.format("\n###\nERROR: %s",
				CXException.eCauseMessagesInString(e)));

	}

	private int randomWithRange(int min, int max) {
		final int range = (max - min) + 1;
		return (int) (Math.random() * range) + min;
	}

	/**
	 * @param aMemoChannelId
	 */
	private void setMemoChannelId(final String aMemoChannelId) {
		pMemoChannelId = aMemoChannelId;
	}

	/**
	 *
	 */
	@Validate
	public void validate() {
		pLogger.logInfo(this, "validate", "validating...");

		try {

			final List<String> wIDs = pILogChannels.getChannelsIds();

			pLogger.logInfo(this, "validate", "LogChannelsIds=[%s]",
					CXStringUtils.stringListToString(wIDs));

		} catch (final Exception | Error e) {
			pLogger.logSevere(this, "validate", "ERROR: %s", e);
		}

		pLogger.logInfo(this, "validate", "validated");
	}
}
