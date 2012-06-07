package org.psem2m.utilities.logging;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.psem2m.utilities.CXStringUtils;

class CActivityLine extends CActivityObject {
	private final static String LABEL_EI = "EndIndex";

	private final static String LABEL_SI = "StartIndex";

	private final static String LABEL_TEXT = "Text";

	private int pEndIndex = -1;

	private int pStartIndex = -1;

	private String pText = null;

	/**
   * 
   */
	CActivityLine() {
		super(CActivityLine.class.getSimpleName());
	}

	/**
	 * @param aText
	 * @param aStartIndex
	 * @param aEndIndex
	 */
	CActivityLine(String aText, int aStartIndex, int aEndIndex) {
		this();
		pText = aText;
		pStartIndex = aStartIndex;
		pEndIndex = aEndIndex;
	}

	@Override
	public Appendable addDescriptionInBuffer(Appendable aBuffer) {
		super.addDescriptionInBuffer(aBuffer);
		CXStringUtils.appendKeyValInBuff(aBuffer, LABEL_SI, getStartIndex());
		CXStringUtils.appendKeyValInBuff(aBuffer, LABEL_EI, getEndIndex());
		CXStringUtils
				.appendKeyValInBuff(aBuffer, LABEL_TEXT, getTrucatedText());
		return aBuffer;
	}

	@Override
	public void destroy() {
		// ...
	}

	/**
	 * @return
	 */
	int getEndIndex() {
		return pEndIndex;
	}

	/**
	 * @return
	 */
	int getStartIndex() {
		return pStartIndex;
	}

	/**
	 * @return
	 */
	String getText() {
		return pText;
	}

	/**
	 * @return
	 */
	int getTextLength() {
		return (pText != null) ? pText.length() : -1;
	}

	/**
	 * @return
	 */
	private String getTrucatedText() {
		return (getTextLength() > 128) ? getText().subSequence(0, 128) + " ..."
				: getText();

	}

	boolean hasText() {
		return getTextLength() > 0;
	}

	/**
	 * @return
	 */
	boolean isNull() {
		return getStartIndex() == -1;
	}

}

public class CActivityRequesterStd extends CActivityObject implements
		IActivityRequester {

	private final static String MESS_ENOUGHT_REC = "Log reading stopped. [%s] records are found.";

	private final static String MESS_FILE_CHANGED = "Log reading stopped. The file [%s] has changed since it was found.";

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
		wFileHandler.setFormatter(new CActivityFormaterStd());
		CActivityRequesterStd wRequesterStd = new CActivityRequesterStd(
				aRequesterName, wFileHandler);
		wRequesterStd.setFileHandlerClosable(true);
		return wRequesterStd;
	}

	private final CActivityFileHandler pFileHandler;

	private boolean pFileHandlerClosable = false;

	/**
	 * @param aTracer
	 * @param aId
	 */
	CActivityRequesterStd(String aId, CActivityFileHandler aFileHandler) {
		super(CActivityRequesterStd.class.getSimpleName() + aId);
		pFileHandler = aFileHandler;
	}

	@Override
	public Appendable addDescriptionInBuffer(Appendable aBuffer) {
		super.addDescriptionInBuffer(aBuffer);
		pFileHandler.addDescriptionInBuffer(aBuffer);
		return aBuffer;
	}

	/**
   * 
   */
	@Override
	public void close() {
		if (isFileHandlerClosable()) {
			getFileHandler().close();
		}
	}

	@Override
	public void destroy() {
		// ...
	}

	/**
	 * @param aRequestRecord
	 * @param aRequestReply
	 * @param aRequestFilter
	 * @return
	 * @throws Exception
	 */
	protected boolean filterRecord(CActivityRequestRecord aRequestRecord,
			CActivityRequestReply aRequestReply,
			CActivityRequestFilter aRequestFilter) throws Exception {
		boolean wContinue = true;

		boolean wTimeOk = aRequestFilter.isTimeOK(aRequestRecord.getLogRecord()
				.getMillis());
		boolean wMethodOk = aRequestFilter.isMethodOK(aRequestRecord
				.getLogRecord().getSourceMethodName());
		boolean wLevelOk = aRequestFilter.isLevelOK(aRequestRecord
				.getLogRecord().getLevel());
		boolean wOutOfTime = false;

		if (wTimeOk && wMethodOk && wLevelOk) {
			aRequestReply.addLogRecord(aRequestRecord);
		} else if (aRequestFilter.isOutOfTime(aRequestRecord.getLogRecord()
				.getMillis())) {
			wOutOfTime = true;
			wContinue = false;
		}
		// applique la limite de record
		if (aRequestFilter.hasNbMaxLogRecord()
				&& aRequestReply.size() >= aRequestFilter.getNbMaxLogRecord()) {
			String wMess = String
					.format(MESS_ENOUGHT_REC, aRequestReply.size());
			aRequestReply.addInfoMessage(wMess);
			wContinue = false;
		}

		if (isTraceDebugOn())
			traceFilterRecord(wTimeOk, wMethodOk, wLevelOk, wOutOfTime,
					aRequestReply.size(), wContinue);

		return wContinue;
	}

	/**
	 * @return
	 */
	CActivityFileHandler getFileHandler() {
		return pFileHandler;
	}

	/**
	 * @param aMethodName
	 * @param aMax
	 * @return
	 */
	@Override
	public CActivityRequestReply getLogLevelRecords(String aLevelName, int aMax) {
		return getLogRecords(CActivityRequestFilter.newLevelFilter(aLevelName,
				aMax));
	}

	/**
	 * @param aMethodName
	 * @param aMax
	 * @return
	 */
	@Override
	public CActivityRequestReply getLogMethodRecords(String aMethodName,
			int aMax) {
		return getLogRecords(CActivityRequestFilter.newMethodFilter(
				aMethodName, aMax));
	}

	/**
	 * @param aTimeStart
	 * @param aTimeStop
	 * @param aLevel
	 * @param aMethod
	 * @param aNbMax
	 * @return
	 */
	@Override
	public CActivityRequestReply getLogRecords(
			CActivityRequestFilter aRequestFilter) {
		if (isTraceDebugOn()) {
			traceGetLogRecords(aRequestFilter);
		}

		CActivityRequestReply wRequestReply = new CActivityRequestReply(this);

		Iterator<CActivityFileText> wFiles = getFileHandler()
				.getExistingFiles().iterator();
		boolean wContinue = true;
		while (wFiles.hasNext() && wContinue) {
			wContinue = readOneFile(wFiles.next(), wRequestReply,
					aRequestFilter);
		}

		return wRequestReply;
	}

	/**
	 * @param aMethodName
	 * @param aMax
	 * @return
	 */
	@Override
	public CActivityRequestReply getLogRecords(int aMax) {
		return getLogRecords(CActivityRequestFilter.newFilter(aMax));
	}

	/**
	 * @param aLogLine
	 * @return
	 */
	protected CActivityRecordPart getNextRecordPart(String aLogLine,
			int aPosStartColumn, char aColumnSep) {
		CActivityRecordPart wRecordPart = null;
		int wColumnPos = aLogLine.indexOf(aColumnSep, aPosStartColumn);
		if (wColumnPos < 0) {
			if (aPosStartColumn < aLogLine.length() - 1)
				wRecordPart = new CActivityRecordPart(aPosStartColumn,
						aLogLine.substring(aPosStartColumn), -1);

		} else {
			String wValue = CXStringUtils.strFullTrim(aLogLine.substring(
					aPosStartColumn, wColumnPos));
			int pPosNextColumn = (wColumnPos + 1 < aLogLine.length() - 1) ? wColumnPos + 1
					: -1;
			wRecordPart = new CActivityRecordPart(wColumnPos, wValue,
					pPosNextColumn);
		}
		if (isTraceDebugOn()) {
			traceGetNextRecordPart(wRecordPart);
		}

		return wRecordPart;
	}

	/**
	 * @param aLineText
	 * @return
	 */
	private CActivityRecordPart getNextRecordPartStd(String aLineText,
			int aOffset) {
		return getNextRecordPart(aLineText, aOffset,
				CActivityFormaterStd.SEP_COLUMN);
	}

	boolean isFileHandlerClosable() {
		return pFileHandlerClosable;
	}

	/**
	 * <pre>
	 * @ 1202217466362 00000003431963 2008/02/05 02:17:46.362 CTesterUtil displayLog
	 * INFO: Test le log numero [3]
	 * </pre>
	 * 
	 * @param aLogLine
	 * @return
	 */
	protected CActivityRequestRecord parseOneRecord(CActivityLine aActivityLine) {
		String wText = aActivityLine.getText();

		CActivityRecordPart wPartPrefix = getNextRecordPartStd(wText, 0);
		CActivityRecordPart wPartMillis = getNextRecordPartStd(wText,
				wPartPrefix.getPosNextColumn());
		CActivityRecordPart wPartNanos = getNextRecordPartStd(wText,
				wPartMillis.getPosNextColumn());
		CActivityRecordPart wPartDate = getNextRecordPartStd(wText,
				wPartNanos.getPosNextColumn());
		CActivityRecordPart wPartTime = getNextRecordPartStd(wText,
				wPartDate.getPosNextColumn());
		CActivityRecordPart wPartClass = getNextRecordPartStd(wText,
				wPartTime.getPosNextColumn());
		CActivityRecordPart wPartMethod = getNextRecordPart(wText,
				wPartClass.getPosNextColumn(), CActivityFormater.SEP_LINE);

		CActivityRecordPart wPartLevel = getNextRecordPartStd(wText,
				wPartMethod.getPosNextColumn());

		String wMessage = wText.substring(wPartLevel.getPosNextColumn());

		Level wLevel = CActivityUtils.levelToLevel(wPartLevel.getValue());

		LogRecord wLogRecord = new LogRecord(wLevel, wMessage);

		wLogRecord.setMillis(Long.parseLong(wPartMillis.getValue()));
		wLogRecord.setSourceClassName(wPartClass.getValue());
		wLogRecord.setSourceMethodName(wPartMethod.getValue());

		return new CActivityRequestRecord(wLogRecord);
	}

	/**
	 * @param aLogText
	 * @return
	 */
	CActivityLine readLastLine(String aLogText) throws Exception {
		if (isTraceDebugOn())
			traceReadLastLine(aLogText);

		return readPreviousLine(aLogText, aLogText.length() - 1);
	}

	/**
	 * @param aLogFile
	 * @return
	 */
	private boolean readOneFile(CActivityFileText aLogFile,
			CActivityRequestReply aRequestReply,
			CActivityRequestFilter aRequestFilter) {
		if (isTraceDebugOn()) {
			traceReadOneFile(aLogFile);
		}
		if (!aLogFile.isSameWhenFound()) {
			String wMess = String.format(MESS_FILE_CHANGED,
					aLogFile.getAbsolutePath());
			aRequestReply.setStatusNOTOK(wMess);
			return false;
		}

		try {
			String wLogText = aLogFile.readAll();
			CActivityRequestRecord wLogRecord;
			CActivityLine wActivityLine = readLastLine(wLogText);
			boolean wContinue = wActivityLine.getStartIndex() > -1;
			while (wContinue) {
				// Recompose le LogRecord et
				wLogRecord = parseOneRecord(wActivityLine);

				// applique le filtre
				wContinue = filterRecord(wLogRecord, aRequestReply,
						aRequestFilter);

				if (wContinue) {
					// lit la "ligne precedente"
					wActivityLine = readPreviousLine(wLogText,
							wActivityLine.getStartIndex());
					wContinue = wActivityLine.getStartIndex() > -1;
				}

			}
		} catch (Throwable e) {
			aRequestReply.setStatusNOTOK(e);
			return false;
		}

		return true;
	}

	/**
	 * @param aLogText
	 * @param aActivityLine
	 * @return
	 */
	protected CActivityLine readPreviousLine(String aLogText, int aFromIndex)
			throws Exception {
		return readPreviousLine(aLogText, aFromIndex,
				CActivityFormaterStd.PREFIX_LINE);
	}

	/**
	 * @param aLogText
	 * @param aFromIndex
	 * @param aSepLine
	 * @return
	 * @throws Exception
	 */
	protected CActivityLine readPreviousLine(String aLogText, int aFromIndex,
			char aSepLine) throws Exception {
		if (isTraceDebugOn())
			traceReadPreviousLine(aLogText, aFromIndex, aSepLine);

		CActivityLine wActivityLine;
		if (aFromIndex <= 0)
			wActivityLine = new CActivityLine();
		else {
			aFromIndex -= 1;
			int wStartIndex = aLogText.lastIndexOf(aSepLine, aFromIndex - 1);
			if (wStartIndex == -1)
				wStartIndex = 0;
			wActivityLine = new CActivityLine(aLogText.substring(wStartIndex,
					aFromIndex), wStartIndex, aFromIndex);
		}

		if (isTraceDebugOn())
			traceReadPreviousLine(wActivityLine);

		return wActivityLine;
	}

	/**
	 * @param aClosable
	 */
	void setFileHandlerClosable(boolean aClosable) {
		pFileHandlerClosable = aClosable;
	}

	/**
	 * @param aTimeOk
	 * @param aMethodOk
	 * @param aLevelOk
	 * @param aOutOfTime
	 * @param aContinue
	 */
	protected void traceFilterRecord(boolean aTimeOk, boolean aMethodOk,
			boolean aLevelOk, boolean aOutOfTime, int aNbRec, boolean aContinue) {
		CLogLineBuffer wTB = new CLogLineBuffer();
		wTB.appendDescr("TimeOk", aTimeOk);
		wTB.appendDescr("MethodOk", aMethodOk);
		wTB.appendDescr("LevelOk", aLevelOk);
		wTB.appendDescr("OutOfTime", aOutOfTime);
		wTB.appendDescr("NbRec", aNbRec);
		wTB.appendDescr("Continue", aContinue);
		traceDebug(this, "traceFilterRecord", wTB);
	}

	/**
	 * @param aRequestFilter
	 */
	private void traceGetLogRecords(CActivityRequestFilter aRequestFilter) {
		CLogLineBuffer wTB = new CLogLineBuffer();
		wTB.appendDescr(aRequestFilter);
		traceDebug(this, "getLogRecords", wTB);
	}

	/**
	 * @param aRequestFilter
	 */
	private void traceGetNextRecordPart(CActivityRecordPart aRecordPart) {
		CLogLineBuffer wTB = new CLogLineBuffer();
		if (aRecordPart != null)
			wTB.appendDescr(aRecordPart);
		else
			wTB.append("No RecordPart available.");
		traceDebug(this, "getNextRecordPart", wTB);
	}

	/**
	 * @param aLogText
	 */
	protected void traceReadLastLine(String aLogText) {
		CLogLineBuffer wTB = new CLogLineBuffer();
		wTB.appendDescr("LogText.length",
				(aLogText != null) ? aLogText.length() : -1);
		traceDebug(this, "readLastLine", wTB);
	}

	/**
	 * @param aLogFile
	 */
	private void traceReadOneFile(CActivityFileText aLogFile) {
		CLogLineBuffer wTB = new CLogLineBuffer();
		wTB.appendDescr(aLogFile);
		traceDebug(this, "readOneFile", wTB);

	}

	/**
	 * @param aRequestFilter
	 */
	protected void traceReadPreviousLine(CActivityLine aActivityLine) {
		CLogLineBuffer wTB = new CLogLineBuffer();
		wTB.appendDescr(aActivityLine);
		traceDebug(this, "readPreviousLine", wTB);
	}

	/**
	 * @param aRequestFilter
	 */
	protected void traceReadPreviousLine(String aLogText, int aFromIndex,
			char aSepLine) {
		CLogLineBuffer wTB = new CLogLineBuffer();
		wTB.appendDescr("LogText.length",
				(aLogText != null) ? aLogText.length() : -1);
		wTB.appendDescr("FromIndex", aFromIndex);
		wTB.appendDescr("SepLine", (aSepLine < 32) ? "0x" + (int) aSepLine
				: String.valueOf(aSepLine));
		traceDebug(this, "readPreviousLine", wTB);

	}

}
