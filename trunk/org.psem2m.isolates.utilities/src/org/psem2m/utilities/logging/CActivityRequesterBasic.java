package org.psem2m.utilities.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;

public class CActivityRequesterBasic extends CActivityRequesterStd {

	/**
	 * @param aTracer
	 * @param aLoggerName
	 * @param aFilePathPattern
	 * @param aFileCount
	 * @return
	 * @throws Exception
	 */
	public static IActivityRequester newRequester(String aRequesterName,
			String aFilePathPattern, int aFileCount) throws Exception {
		CActivityFileHandler wFileHandler = new CActivityFileHandler(
				aFilePathPattern, aFileCount);
		wFileHandler.setFormatter(new CActivityFormaterBasic());
		CActivityRequesterBasic wRequesterBasic = new CActivityRequesterBasic(
				aRequesterName, wFileHandler);
		wRequesterBasic.setFileHandlerClosable(true);
		return wRequesterBasic;
	}

	/**
	 * @param aTracer
	 * @param aId
	 * @param aFileHandler
	 */
	CActivityRequesterBasic(String aId, CActivityFileHandler aFileHandler) {
		super(aId, aFileHandler);
	}

	/**
	 * @param aLineText
	 * @return
	 */
	private CActivityRecordPart getNextRecordPartBasic(String aLineText,
			int aOffset) {
		return getNextRecordPart(aLineText, aOffset,
				CActivityFormaterBasic.SEP_COLUMN);
	}

	/**
	 * <pre>
	 * 1202217466550;00000042051957;2008/02/05;02:17:46.550;WARNING;displayLog;Test le log numéro [472]
	 * </pre>
	 * 
	 * @param aLogLine
	 * @return
	 */
	@Override
	protected CActivityRequestRecord parseOneRecord(CActivityLine aActivityLine) {
		String wText = aActivityLine.getText();

		CActivityRecordPart wPartMillis = getNextRecordPartBasic(wText, 0);
		CActivityRecordPart wPartNanos = getNextRecordPartBasic(wText,
				wPartMillis.getPosNextColumn());
		CActivityRecordPart wPartDate = getNextRecordPartBasic(wText,
				wPartNanos.getPosNextColumn());
		CActivityRecordPart wPartTime = getNextRecordPartBasic(wText,
				wPartDate.getPosNextColumn());
		CActivityRecordPart wPartLevel = getNextRecordPartBasic(wText,
				wPartTime.getPosNextColumn());
		CActivityRecordPart wPartMethod = getNextRecordPartBasic(wText,
				wPartLevel.getPosNextColumn());

		String wMessage = wText.substring(wPartMethod.getPosNextColumn());

		Level wLevel = CActivityUtils.levelToLevel(wPartLevel.getValue());

		LogRecord wLogRecord = new LogRecord(wLevel, wMessage);

		wLogRecord.setMillis(Long.parseLong(wPartMillis.getValue()));
		wLogRecord.setSourceMethodName(wPartMethod.getValue());

		return new CActivityRequestRecord(wLogRecord);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.logging.CActivityRequesterStd#readPreviousLine(java
	 * .lang.String, int)
	 */
	@Override
	protected CActivityLine readPreviousLine(String aLogText, int aFromIndex)
			throws Exception {
		return super.readPreviousLine(aLogText, aFromIndex,
				CActivityFormater.SEP_LINE);
	}

}
