package org.psem2m.utilities.logging;

import java.util.ArrayList;
import java.util.List;

import org.psem2m.utilities.CXStringUtils;

public class CActivityRequestReply extends CActivityObject {

	private String pErrMessage = null;

	private List<String> pInfoMessages = null;
	private final CActivityRequestRecords pLogRecords = new CActivityRequestRecords();
	private boolean pOK = true;
	private Throwable pThrowable = null;

	/**
	 * @param aLogRecords
	 */
	CActivityRequestReply(CActivityObject aParent) {
		super(aParent, CActivityRequestReply.class.getSimpleName());
	}

	/**
	 * @param e
	 */
	CActivityRequestReply(CActivityObject aParent, Throwable aThrowable) {
		this(aParent);
		setStatusNOTOK(aThrowable);
	}

	@Override
	public Appendable addDescriptionInBuffer(Appendable aBuffer) {
		super.addDescriptionInBuffer(aBuffer);
		CXStringUtils.appendKeyValInBuff(aBuffer, "OK", pOK);

		pLogRecords.addDescriptionInBuffer(aBuffer);
		return aBuffer;
	}

	void addInfoMessage(String aMessage) {
		if (!isInfoMessagesInitialized())
			newInfoMessages();
		pInfoMessages.add(aMessage);
	}

	/**
	 * @param aLogRecord
	 */
	void addLogRecord(CActivityRequestRecord aLogRecord) {
		pLogRecords.addLogRecord(aLogRecord);
	}

	@Override
	public void destroy() {
		pLogRecords.destroy();
	}

	String getErrMessage() {
		return pErrMessage;
	}

	/**
	 * @return
	 */
	public CActivityRequestRecords getLogRecords() {
		return pLogRecords;
	}

	Throwable getThrowable() {
		return pThrowable;
	}

	boolean hasInfoMessages() {
		return isInfoMessagesInitialized() && pInfoMessages.size() > 0;
	}

	/**
	 * @return
	 */
	public boolean hasRecords() {
		return getLogRecords().hasRecords();
	}

	private boolean isInfoMessagesInitialized() {
		return pInfoMessages != null;
	}

	public boolean isOk() {
		return pOK;
	}

	private void newInfoMessages() {

		pInfoMessages = new ArrayList<String>();
	}

	void setStatusNOTOK(String aErrMessage) {
		pOK = false;
		pErrMessage = aErrMessage;
	}

	void setStatusNOTOK(Throwable aThrowable) {
		pOK = false;
		pThrowable = aThrowable;
		pErrMessage = getThrowable().getMessage();
	}

	/**
	 * @return
	 */
	public int size() {
		return getLogRecords().size();
	}

}
