package org.psem2m.isolates.loggers.impl;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import org.psem2m.isolates.loggers.ILoggingCondition;
import org.psem2m.utilities.CXStringUtils;
import org.psem2m.utilities.json.JSONException;
import org.psem2m.utilities.json.JSONObject;

/**
 * CASE 0000222: Logging conditions to control the content of the trace log
 *
 * Creation of a logging condition
 *
 * <pre>
 * / / declare a Logging condition identified by "ISubSystemStuff"
 * pLoggingConditions.newLoggingCondition(ISubSystemStuff.class,"SubSystem Stuff logging condition");
 *
 * </pre>
 *
 * Usage of the logging condition using a method "isLoggingConditionDebugOn()"
 * witch retreive the ILoggingConditions service.
 *
 * The method "logActivityDebug()" is also a method implemented in an Abstract
 * class to simplify the usage.
 *
 * <pre>
 * if (isLoggingConditionDebugOn(ISubSystemA.class){
 *
 *     logActivityDebug(this, "myMethod", "My message with an arg [%s]", aArg);
 * }
 * </pre>
 *
 * @author ogattaz
 *
 */
public class CLoggingCondition implements ILoggingCondition {

	private static final Level DEFAULT_LEVEL = Level.ALL;

	private static final String PROP_CMT = "cmt";

	private static final String PROP_ID = "id";

	private static final String PROP_LEVEL = "level";

	private final String pComment;

	private final AtomicReference<Level> pConfitionLevel = new AtomicReference<Level>(
			DEFAULT_LEVEL);

	private final String pId;

	/**
	 * @param aDef
	 * @throws JSONException
	 */
	public CLoggingCondition(final JSONObject aDef) throws JSONException {
		this(aDef.getString(PROP_ID), aDef.getString(PROP_CMT));

		setLevel(aDef.getString(PROP_LEVEL));
	}

	/**
	 * @param aId
	 * @param aComments
	 */
	public CLoggingCondition(final String aId, final String... aComments) {
		super();
		pId = aId;
		// convert to a sString even the aComments is null or empty
		pComment = CXStringUtils.stringTableToString(aComments, "\n");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.psem2m.utilities.IXDescriber#addDescriptionInBuffer(java.lang.Appendable
	 * )
	 */
	@Override
	public Appendable addDescriptionInBuffer(Appendable aBuffer) {

		CXStringUtils.appendKeyValInBuff(aBuffer, PROP_ID, getId());
		CXStringUtils.appendKeyValInBuff(aBuffer, PROP_LEVEL, getLevel()
				.getName());
		CXStringUtils.appendKeyValInBuff(aBuffer, PROP_CMT, getComment()
				.replace('\n', '§'));
		return aBuffer;
	}

	/**
	 * @return
	 */
	CLoggingCondition cloneMe() {

		final CLoggingCondition wNewLC = new CLoggingCondition(pId,
				pComment.split("\n"));

		wNewLC.setLevel(getLevel());

		return wNewLC;
	}

	@Override
	public String getComment() {
		return pComment;
	}

	@Override
	public String getId() {
		return pId;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.psem2m.isolates.loggers.ILoggingCondition#getLevel()
	 */
	@Override
	public Level getLevel() {
		return pConfitionLevel.get();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.psem2m.isolates.loggers.ILoggingCondition#isOn(java.util.logging.
	 * Level)
	 */
	@Override
	public boolean isOn(final Level aLevel) {

		final Level wCurrentLevel = pConfitionLevel.get();

		if (wCurrentLevel == Level.OFF) {
			return false;
		}
		// eg FINE (500) <= INFO (800)
		return wCurrentLevel.intValue() <= aLevel.intValue();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.psem2m.isolates.loggers.ILoggingCondition#setConditionLevel(java.
	 * util.logging.Level)
	 */
	@Override
	public Level setLevel(final Level aLevel) {
		return pConfitionLevel.getAndSet(aLevel);
	}

	/**
	 * @param aLevelName
	 * @return
	 */
	private Level setLevel(final String aLevelName) {
		return setLevel(Level.parse(aLevelName));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.psem2m.utilities.IXDescriber#toDescription()
	 */
	@Override
	public String toDescription() {
		return addDescriptionInBuffer(new StringBuilder()).toString();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.psem2m.isolates.loggers.ILoggingCondition#toJson()
	 */
	@Override
	public JSONObject toJson() throws JSONException {
		final JSONObject wObj = new JSONObject();
		wObj.put(PROP_ID, getId());
		wObj.put(PROP_CMT, getComment());
		wObj.put(PROP_LEVEL, getLevel().getName());
		return wObj;
	}
}
