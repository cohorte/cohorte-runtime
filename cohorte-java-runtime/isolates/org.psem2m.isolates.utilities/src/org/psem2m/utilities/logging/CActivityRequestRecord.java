package org.psem2m.utilities.logging;

import java.util.logging.LogRecord;

import org.psem2m.utilities.CXStringUtils;

/**
 * @author isandlaTech - ogattaz
 * 
 */
public class CActivityRequestRecord extends CActivityObject {

	private final static String LABEL_LEVEL = "level";

	private final LogRecord pLogRecord;

	/**
	 * @param aLogRecord
	 */
	CActivityRequestRecord(LogRecord aLogRecord) {
		super(CActivityRequestRecord.class.getSimpleName());
		pLogRecord = aLogRecord;
	}

	@Override
	public Appendable addDescriptionInBuffer(Appendable aBuffer) {
		super.addDescriptionInBuffer(aBuffer);
		CXStringUtils.appendKeyValInBuff(aBuffer, LABEL_LEVEL, pLogRecord
				.getLevel().getName());
		return aBuffer;
	}

	@Override
	public void destroy() {
	}

	/**
	 * @return
	 */
	public LogRecord getLogRecord() {
		return pLogRecord;
	}
}
