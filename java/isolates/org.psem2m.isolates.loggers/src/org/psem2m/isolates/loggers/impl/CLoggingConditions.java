package org.psem2m.isolates.loggers.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;

import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.isolates.loggers.CLoggingConditionsException;
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
public class CLoggingConditions implements ILoggingConditions {

	private static final String FILE_NAME_FORMAT = "LoggingConditions_%s.js";

	private static final String ID_NULL = "<Null id condition>";

	private static final String LIB_FILENAME = "filename";

	private static final String LIB_ID = "id";

	private static final String MESS_ALREADY_EXISTS = "The logging condition [%s] already exixts.";

	private static final String MESS_CANT_FIND = "Unable to retreive the logging condition using the id [%s]";

	private static final String MESS_ClASS_ID_NULL = String.format(
			"The given Class conditon Id is null. Use [%s] ", ID_NULL);

	private static final String MESS_STRING_ID_NULL = String
			.format("The given String conditon Id is null or empty. Use [%s] ",
					ID_NULL);

	private static final String PROP_CONDITION_DESCRS = "condition_descriptions";

	private static final String PROP_CONDITION_DOCS = "condition_docs";

	private static final String PROP_SIZE = "size";

	private final Map<String, ILoggingCondition> pConditions = new TreeMap<String, ILoggingCondition>();

	private final CXFileUtf8 pFileMemoDefs;

	private final String pId;

	/**
	 * Service reference managed by iPojo (see metadata.xml)
	 *
	 * This service is the general logger of the current isolate
	 **/
	private final IIsolateLoggerSvc pIsolateLoggerSvc;
	/**
	 * Service reference managed by iPojo (see metadata.xml)
	 *
	 * This service is the knowledge of the locations of the current isolate
	 **/
	private final IPlatformDirsSvc pPlatformDirsSvc;

	/**
	 * injection by constructor
	 */
	public CLoggingConditions(final String aConditionsId,
			final IIsolateLoggerSvc aLogger,
			final IPlatformDirsSvc aPlatformDirsSvc) {
		super();

		pId = aConditionsId;
		pIsolateLoggerSvc = aLogger;
		pPlatformDirsSvc = aPlatformDirsSvc;

		pFileMemoDefs = new CXFileUtf8(pPlatformDirsSvc.getIsolateLogDir()
				.getAbsolutePath(), getFileName());
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

		CXStringUtils.appendKeyValInBuff(aBuffer, LIB_ID, getId());

		CXStringUtils.appendKeyValInBuff(aBuffer, LIB_FILENAME, getFileName());

		CXStringUtils
				.appendKeyValInBuff(aBuffer, PROP_SIZE, pConditions.size());

		final StringBuilder wSB = new StringBuilder();
		int wIdx = 0;
		for (final Entry<String, ILoggingCondition> wEntry : pConditions
				.entrySet()) {
			if (wIdx > 0) {
				wSB.append(',');
			}
			CXStringUtils.appendFormatStrInBuff(wSB, "(%02d)=[", wIdx);
			wEntry.getValue().addDescriptionInBuffer(wSB);
			wSB.append(']');
			wIdx++;
		}
		CXStringUtils.appendKeyValInBuff(aBuffer, PROP_CONDITION_DESCRS,
				wSB.toString());

		return aBuffer;
	}

	/**
	 * @return 1 if a file is deleted
	 */
	int cleanFile() {

		if (pFileMemoDefs != null && pFileMemoDefs.exists()) {
			return pFileMemoDefs.delete() ? 1 : 0;
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.loggers.ILoggingConditions#cleanLoggingConditionsFile
	 * ()
	 */
	@Override
	public int cleanLoggingConditionsFile() throws CLoggingConditionsException {

		return pFileMemoDefs.delete() ? 1 : 0;
	}

	/**
	 * @param aConditionsId
	 * @return
	 */
	CLoggingConditions cloneMe(final String aConditionsId) {

		final CLoggingConditions wNewLCs = new CLoggingConditions(
				aConditionsId, pIsolateLoggerSvc, pPlatformDirsSvc);

		for (final Entry<String, ILoggingCondition> wEntry : pConditions
				.entrySet()) {

			wNewLCs.pConditions.put(wEntry.getKey(),
					((CLoggingCondition) wEntry.getValue()).cloneMe());
		}

		return wNewLCs;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.loggers.ILoggingConditions#getConditions()
	 */
	@Override
	public List<ILoggingCondition> getConditions() {

		final List<ILoggingCondition> wList = new ArrayList<ILoggingCondition>();

		for (final Entry<String, ILoggingCondition> wEntry : pConditions
				.entrySet()) {
			wList.add(wEntry.getValue());
		}

		return wList;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.psem2m.isolates.loggers.ILoggingConditions#getFile()
	 */
	@Override
	public File getFile() {
		return pFileMemoDefs;
	}

	/**
	 * @return
	 */
	String getFileName() {
		return String.format(FILE_NAME_FORMAT, getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.loggers.ILoggingConditions#getId()
	 */
	@Override
	public String getId() {
		return pId;
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
					.getJSONArray(PROP_CONDITION_DOCS);

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
			final Level aInitialLevel, String... aComments) {
		return newLoggingCondition(validConditionId(aConditionId),
				aInitialLevel, aComments);
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
			final Level aInitialLevel, String... aComments) {

		final String wConfitionId = validConditionId(aConditionId);

		ILoggingCondition wCondition = pConditions.get(wConfitionId);

		if (wCondition != null) {
			pIsolateLoggerSvc.logWarn(this, "newLoggingCondition",
					MESS_ALREADY_EXISTS, aConditionId);
			return wCondition;
		}

		wCondition = new CLoggingCondition(wConfitionId, aComments);

		wCondition.setLevel(aInitialLevel);

		pConditions.put(wConfitionId, wCondition);

		return wCondition;
	}

	/**
	 *
	 */
	void read() {
		pIsolateLoggerSvc.logInfo(this, "read", "Reading...");
		try {

			// Read defs of conditions from
			// ...Base/log/LoggingConditions_[conditionsId].js

			final int wNbLoadedConditions = loadStoredConditions(pFileMemoDefs);

			pIsolateLoggerSvc.logInfo(this, "read", "NbLoadedConditions=[%s]",
					wNbLoadedConditions);

			pIsolateLoggerSvc.logInfo(this, "read", "LoggingConditions:\n%s",
					toJson().toString(2));

			pIsolateLoggerSvc.logInfo(this, "read", "Read ok.");

		} catch (Exception | Error e) {
			pIsolateLoggerSvc.logSevere(this, "read",
					"Reading NOT OK : ERROR: %s", e);
		}
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
	 *
	 */
	boolean store() {

		pIsolateLoggerSvc.logInfo(this, "store", "Storing...");
		try {

			// Store defs of conditions in ...Base/log/LoggingConditions.js
			storeLoggingConditions(pFileMemoDefs);

			pIsolateLoggerSvc.logInfo(this, "store", "Stored");
			return true;

		} catch (Exception | Error e) {
			pIsolateLoggerSvc.logSevere(this, "store",
					"Storing NOT OK : ERROR: %s", e);
			return false;
		}
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

		wObj.put(PROP_CONDITION_DOCS, wArray);
		return wObj;
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
