package org.psem2m.isolates.loggers.impl;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.loggers.ILoggingCondition;
import org.psem2m.isolates.loggers.ILoggingConditions;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;
import org.psem2m.utilities.CXStringUtils;
import org.psem2m.utilities.files.CXFileUtf8;
import org.psem2m.utilities.json.JSONArray;
import org.psem2m.utilities.json.JSONException;
import org.psem2m.utilities.json.JSONObject;

/**
 * CASE 0000222: Logging conditions to control the content of the trace log
 *
 * @author ogattaz
 *
 */
@Component(name = "psem2m-logging-conditions-factory")
@Instantiate
@Provides(specifications = ILoggingConditions.class)
public class CLoggingConditions implements ILoggingConditions {

	private static final String FILE_NAME = "LoggingConditions.js";
	private static final String ID_NULL = "<Null id condition>";

	private static final String MESS_ALREADY_EXISTS = "The logging condition [%s] already exixts.";

	private static final String MESS_CANT_FIND = "Unable to retreive the logging condition using the id [%s]";

	private static final String MESS_ClASS_ID_NULL = String.format(
			"The given Class conditon Id is null. Use [%s] ", ID_NULL);

	private static final String MESS_STRING_ID_NULL = String
			.format("The given String conditon Id is null or empty. Use [%s] ",
					ID_NULL);

	private static final String PROP_CONDITIONS = "conditions";

	private static final String PROP_DOCS_CONDITION = "condition_docs";

	private static final String PROP_SIZE = "size";

	private final Map<String, ILoggingCondition> pConditions = new TreeMap<String, ILoggingCondition>();

	private CXFileUtf8 pFileMemoDefs = null;

	/**
	 * Service reference managed by iPojo (see metadata.xml)
	 *
	 * This service is the general logger of the current isolate
	 **/
	@Requires
	private IIsolateLoggerSvc pIsolateLoggerSvc;

	/**
	 * Service reference managed by iPojo (see metadata.xml)
	 *
	 * This service is the knowledge of the locations of the current isolate
	 **/
	@Requires
	private IPlatformDirsSvc pPlatformDirsSvc;

	/**
	 *
	 */
	public CLoggingConditions() {
		super();
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
		CXStringUtils
				.appendKeyValInBuff(aBuffer, PROP_SIZE, pConditions.size());

		final StringBuilder wSB = new StringBuilder();
		int wIdx = 0;
		for (final Entry<String, ILoggingCondition> wEntry : pConditions
				.entrySet()) {
			if (wIdx > 0) {
				wSB.append(',');
			}
			CXStringUtils.appendFormatStrInBuff(wSB, "(%02)=[", wIdx);
			wEntry.getValue().addDescriptionInBuffer(wSB);
			wSB.append(']');
			wIdx++;
		}
		CXStringUtils.appendKeyValInBuff(aBuffer, PROP_CONDITIONS,
				wSB.toString());

		return aBuffer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.loggers.ILoggingConditions#getCondition(java.lang
	 * .Class)
	 */
	@Override
	public ILoggingCondition getCondition(Class<?> aConditionId) {

		return getCondition(validConditionId(aConditionId));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.loggers.ILoggingConditions#getCondition(java.lang
	 * .String)
	 */
	@Override
	public ILoggingCondition getCondition(String aConditionId) {

		final String wConfitionId = validConditionId(aConditionId);

		final ILoggingCondition wCondition = pConditions.get(wConfitionId);

		if (wCondition == null) {
			pIsolateLoggerSvc.logWarn(this, "getCondition", MESS_CANT_FIND,
					aConditionId);
			return null;
		}

		return wCondition;
	}

	@Invalidate
	void invalidate() {
		pIsolateLoggerSvc.logInfo(this, "validate", "validating...");
		try {

			// Store defs of conditions in ...Base/log/LoggingConditions.js
			storeLoggingConditions(pFileMemoDefs);

		} catch (Exception | Error e) {
			pIsolateLoggerSvc.logSevere(this, "validate", "ERROR: %s", e);

		}
		pIsolateLoggerSvc.logInfo(this, "invalidate", "invalidated");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.loggers.ILoggingConditions#isOn(java.lang.Class,
	 * java.util.logging.Level)
	 */
	@Override
	public boolean isOn(final Class<?> aConditionId, Level aLevel) {
		return isOn(validConditionId(aConditionId), aLevel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.loggers.ILoggingConditions#isOn(java.lang.String,
	 * java.util.logging.Level)
	 */
	@Override
	public boolean isOn(final String aConditionId, Level aLevel) {

		final String wConfitionId = validConditionId(aConditionId);

		final ILoggingCondition wCondition = pConditions.get(wConfitionId);

		if (wCondition == null) {
			pIsolateLoggerSvc.logWarn(this, "isOn", MESS_CANT_FIND,
					aConditionId);
			return false;
		}

		return wCondition.isOn(aLevel);
	}

	/**
	 * @param aFile
	 *            the file containing the previous stored conditions
	 * @return
	 * @throws JSONException
	 * @throws IOException
	 */
	private int loadStoredConditions(CXFileUtf8 aFile) throws JSONException,
			IOException {

		int wMax = 0;

		if (aFile.isFile()) {

			final JSONObject wConf = new JSONObject(aFile.readAll());

			final JSONArray wConditionDefs = wConf
					.getJSONArray(PROP_DOCS_CONDITION);

			// load stored Conditions
			wMax = wConditionDefs.length();
			for (int wIdx = 0; wIdx < wMax; wIdx++) {
				final JSONObject wConditionDef = wConditionDefs
						.getJSONObject(wIdx);

				final ILoggingCondition wCondition = new CLoggingCondition(
						wConditionDef);
				pConditions.put(wCondition.getId(), wCondition);
			}
		}
		return wMax;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.loggers.ILoggingConditions#newLoggingCondition(java
	 * .lang.Class, java.lang.String[])
	 */
	@Override
	public ILoggingCondition newLoggingCondition(Class<?> aConditionId,
			String... aComments) {
		return newLoggingCondition(validConditionId(aConditionId), aComments);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.loggers.ILoggingConditions#newLoggingCondition(java
	 * .lang.String, java.lang.String[])
	 */
	@Override
	public ILoggingCondition newLoggingCondition(final String aConditionId,
			String... aComments) {

		final String wConfitionId = validConditionId(aConditionId);

		ILoggingCondition wCondition = pConditions.get(wConfitionId);

		if (wCondition != null) {
			pIsolateLoggerSvc.logWarn(this, "setConditionLevel",
					MESS_ALREADY_EXISTS, aConditionId);
			return wCondition;
		}

		wCondition = new CLoggingCondition(wConfitionId, aComments);

		pConditions.put(wConfitionId, wCondition);

		return wCondition;
	}

	/**
	 * @param aConditionLevel
	 *            The level to set.
	 * @return the number of logging condition set.
	 */
	@Override
	public int setAllConditionsLevel(final Level aConditionLevel) {

		for (final Entry<String, ILoggingCondition> wEntry : pConditions
				.entrySet()) {
			wEntry.getValue().setLevel(aConditionLevel);
		}
		return pConditions.size();
	}

	/**
	 * @return the number of logging condition set to Level.ALL
	 */
	@Override
	public int setAllConditionsToAll() {
		return setAllConditionsLevel(Level.ALL);
	}

	/**
	 * @return the number of logging condition set to Level.OFF
	 */
	@Override
	public int setAllConditionsToOff() {
		return setAllConditionsLevel(Level.OFF);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.loggers.ILoggingConditions#setConditionLevel(java
	 * .lang.Class, java.util.logging.Level)
	 */
	@Override
	public Level setConditionLevel(Class<?> aConditionId, Level aConditionLevel) {

		return setConditionLevel(validConditionId(aConditionId),
				aConditionLevel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.loggers.ILoggingConditions#setConditionLevel(java
	 * .lang.String, java.util.logging.Level)
	 */
	@Override
	public Level setConditionLevel(String aConditionId, Level aConditionLevel) {

		final String wConfitionId = validConditionId(aConditionId);

		final ILoggingCondition wCondition = pConditions.get(wConfitionId);

		if (wCondition == null) {
			pIsolateLoggerSvc.logWarn(this, "setConditionLevel",
					MESS_CANT_FIND, aConditionId);
			return null;
		}

		return wCondition.setLevel(aConditionLevel);
	}

	/**
	 * @param aFile
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	private int storeLoggingConditions(CXFileUtf8 aFile) throws IOException,
			JSONException {

		aFile.writeAll(toJson().toString(2));

		return pConditions.size();
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
	 * @see org.psem2m.isolates.loggers.ILoggingConditions#toJson()
	 */
	@Override
	public JSONObject toJson() throws JSONException {
		final JSONObject wObj = new JSONObject();

		final JSONArray wArray = new JSONArray();

		for (final Entry<String, ILoggingCondition> wEntry : pConditions
				.entrySet()) {

			wArray.put(wEntry.getValue().toJson());
		}

		wObj.put(PROP_DOCS_CONDITION, wArray);
		return wObj;
	}

	/**
	 *
	 */
	@Validate
	void validate() {
		pIsolateLoggerSvc.logInfo(this, "validate", "validating...");
		try {

			// Read defs of conditions from ...Base/log/LoggingConditions.js

			pFileMemoDefs = new CXFileUtf8(pPlatformDirsSvc.getIsolateLogDir()
					.getAbsolutePath(), FILE_NAME);

			final int wNbLoadedConditions = loadStoredConditions(pFileMemoDefs);

			pIsolateLoggerSvc.logInfo(this, "validate",
					"NbLoadedConditions=[%s]", wNbLoadedConditions);

			pIsolateLoggerSvc.logInfo(this, "validate",
					"LoggingConditions:\n%s", toJson().toString(2));

		} catch (Exception | Error e) {
			pIsolateLoggerSvc.logSevere(this, "validate", "ERROR: %s", e);

		}
		pIsolateLoggerSvc.logInfo(this, "validate", "validated");
	}

	/**
	 * @param aConditionId
	 * @return
	 */
	private String validConditionId(final Class<?> aConditionId) {

		if (aConditionId != null) {
			return aConditionId.getName();
		}
		pIsolateLoggerSvc.logWarn(this, "validConditionId", MESS_ClASS_ID_NULL);
		return ID_NULL;
	}

	/**
	 * @param aConditionId
	 * @return
	 */
	private String validConditionId(final String aConditionId) {

		if ((aConditionId != null) && !aConditionId.isEmpty()) {
			return aConditionId;
		}

		pIsolateLoggerSvc
				.logWarn(this, "validConditionId", MESS_STRING_ID_NULL);
		return ID_NULL;

	}
}
