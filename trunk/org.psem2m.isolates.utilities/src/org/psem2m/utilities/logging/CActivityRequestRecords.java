package org.psem2m.utilities.logging;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.psem2m.utilities.CXStringUtils;

public class CActivityRequestRecords extends CActivityObject {
	private final static String LABEL_SIZE = "size";

	private final List<CActivityRequestRecord> pLogRecords = new ArrayList<CActivityRequestRecord>();

	/**
   * 
   */
	CActivityRequestRecords() {
		super(CActivityRequestRecords.class.getSimpleName());
	}

	@Override
	public Appendable addDescriptionInBuffer(Appendable aBuffer) {
		super.addDescriptionInBuffer(aBuffer);
		CXStringUtils.appendKeyValInBuff(aBuffer, LABEL_SIZE, size());
		return aBuffer;
	}

	/**
	 * @param aLogRecord
	 */
	void addLogRecord(CActivityRequestRecord aLogRecord) {
		pLogRecords.add(aLogRecord);
	}

	@Override
	public void destroy() {
		pLogRecords.clear();
	}

	/**
	 * @return
	 */
	public boolean hasRecords() {
		return pLogRecords != null;
	}

	/**
	 * @return
	 */
	public Iterator<CActivityRequestRecord> iterator() {
		return pLogRecords.iterator();
	}

	/**
	 * @return
	 */
	public int size() {
		return (hasRecords()) ? pLogRecords.size() : -1;
	}
}
