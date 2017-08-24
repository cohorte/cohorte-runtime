package org.psem2m.isolates.loggers.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
import org.psem2m.isolates.loggers.CLoggingConditionsException;
import org.psem2m.isolates.loggers.ILoggingCondition;
import org.psem2m.isolates.loggers.ILoggingConditions;
import org.psem2m.isolates.loggers.ILoggingConditionsManager;
import org.psem2m.isolates.services.dirs.IPlatformDirsSvc;
import org.psem2m.utilities.CXStringUtils;
import org.psem2m.utilities.json.JSONArray;
import org.psem2m.utilities.json.JSONException;
import org.psem2m.utilities.json.JSONObject;

/**
 * CASE 0000222: Logging conditions to control the content of the trace log.
 *
 * To be always present in all the isolates, the component
 * 'psem2m-logging-conditions-manager' is instanciated by iPOJO itself.
 *
 * The manager the manager implements the 'ILoggingConditions', it creates the
 * 'DEFAULT' loggingConditions and redirects all the methods of that interface
 * to it.
 *
 * The manager allows the creations of specific instance of 'ILoggingConditions'
 * to have specific set of conditions (eg. one per log activity channel).
 *
 * By default, a new instance of 'ILoggingConditions' is a clone of the default
 * one.
 *
 * When the manager is invalidated, all the instance of 'ILoggingConditions' are
 * stored.
 *
 * @author ogattaz
 *
 */
@Component(name = "psem2m-logging-conditions-manager-factory")
@Instantiate
@Provides(specifications = { ILoggingConditions.class,
		ILoggingConditionsManager.class })
public class CLoggingConditionsManager implements ILoggingConditions,
		ILoggingConditionsManager {

	private static final String DEFAULT = "DEFAULT";

	private static final String PROP_CONDITIONS_DESCRS = "conditions_descriptions";

	private static final String PROP_CONDITIONS_IDS = "conditions_ids";

	private static final String PROP_SIZE = "size";

	CLoggingConditions pDefaultLoggingConditions = null;

	/**
	 * Service reference managed by iPojo (see metadata.xml)
	 *
	 * This service is the general logger of the current isolate
	 **/
	@Requires
	private IIsolateLoggerSvc pIsolateLoggerSvc;

	private final Map<String, CLoggingConditions> pLoggingConditionsMap = new TreeMap<String, CLoggingConditions>();

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
	public CLoggingConditionsManager() {
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

		CXStringUtils.appendKeyValInBuff(aBuffer, PROP_SIZE,
				pLoggingConditionsMap.size());

		final StringBuilder wSB = new StringBuilder();
		int wIdx = 0;
		for (final Entry<String, CLoggingConditions> wEntry : pLoggingConditionsMap
				.entrySet()) {
			if (wIdx > 0) {
				wSB.append(',');
			}
			CXStringUtils.appendFormatStrInBuff(wSB, "(%02)=[%s]", wIdx, wEntry
					.getValue().toDescription());

			wIdx++;
		}
		CXStringUtils.appendKeyValInBuff(aBuffer, PROP_CONDITIONS_DESCRS,
				wSB.toString());

		return aBuffer;
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
		return pDefaultLoggingConditions.cleanLoggingConditionsFile();
	}

	@Override
	public int cleanLoggingConditionsFile(String aConditionsId)
			throws CLoggingConditionsException {

		final CLoggingConditions wLoggingConditions = pLoggingConditionsMap
				.get(aConditionsId);

		if (wLoggingConditions == null) {
			throw new CLoggingConditionsException(
					"The logging conditions [%s] doesn't exist", aConditionsId);
		}
		// removes it from the map
		removeLoggingConditions(aConditionsId);

		final int wNbFile = wLoggingConditions.cleanFile();
		if (wNbFile == 0) {
			pIsolateLoggerSvc
					.logWarn(
							this,
							"cleanLoggingConditionsFiles",
							"No file to delete when clean the logging conditions [%s] ",
							aConditionsId);
		}
		return wNbFile;
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

		return pDefaultLoggingConditions.getCondition(aConditionId);
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

		return pDefaultLoggingConditions.getCondition(aConditionId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.loggers.ILoggingConditions#getConditions()
	 */
	@Override
	public List<ILoggingCondition> getConditions() {
		return pDefaultLoggingConditions.getConditions();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.psem2m.isolates.loggers.ILoggingConditions#getFile()
	 */
	@Override
	public File getFile() {
		return pDefaultLoggingConditions.getFile();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.loggers.ILoggingConditions#getId()
	 */
	@Override
	public String getId() {
		return pDefaultLoggingConditions.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.loggers.ILoggingConditionsManager#getLoggingConditions
	 * ()
	 */
	@Override
	public List<ILoggingConditions> getLoggingConditions() {
		final List<ILoggingConditions> wList = new ArrayList<ILoggingConditions>();
		for (final Entry<String, CLoggingConditions> wLoggerEntry : pLoggingConditionsMap
				.entrySet()) {
			wList.add(wLoggerEntry.getValue());
		}
		return wList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.loggers.ILoggingConditionsManager#getLoggingConditionsIds
	 * ()
	 */
	@Override
	public List<String> getLoggingConditionsIds() {

		final List<String> wIds = new ArrayList<String>();
		for (final Entry<String, CLoggingConditions> wLoggerEntry : pLoggingConditionsMap
				.entrySet()) {
			wIds.add(wLoggerEntry.getKey());
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.loggers.ILoggingConditionsManager#hasLoggingConditions
	 * (java.lang.String)
	 */
	@Override
	public boolean hasLoggingConditions(String aConditionsId) {
		return pLoggingConditionsMap.containsKey(aConditionsId);
	}

	@Invalidate
	void invalidate() {
		pIsolateLoggerSvc.logInfo(this, "invalidate", "invalidating...");
		try {

			removeAll();

		} catch (Exception | Error e) {
			pIsolateLoggerSvc.logSevere(this, "invalidate", "ERROR: %s", e);

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

		return pDefaultLoggingConditions.isOn(aConditionId, aLevel);
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

		return pDefaultLoggingConditions.isOn(aConditionId, aLevel);
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

		return pDefaultLoggingConditions.newLoggingCondition(aConditionId,
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

		return pDefaultLoggingConditions.newLoggingCondition(aConditionId,
				aInitialLevel, aComments);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.loggers.ILoggingConditionsManager#newLoggingConditions
	 * (java.lang.String)
	 */
	@Override
	public ILoggingConditions newLoggingConditions(String aConditionsId) {

		final CLoggingConditions wExistiongLCs = pLoggingConditionsMap
				.get(aConditionsId);

		if (wExistiongLCs != null) {
			return wExistiongLCs;
		}

		final CLoggingConditions pNewLCs = pDefaultLoggingConditions
				.cloneMe(aConditionsId);

		// reload the previous settings
		pNewLCs.read();

		pLoggingConditionsMap.put(aConditionsId, pNewLCs);
		return pNewLCs;
	}

	/**
	 *
	 */
	public void removeAll() {
		for (final Entry<String, CLoggingConditions> wEntry : pLoggingConditionsMap
				.entrySet()) {

			wEntry.getValue().store();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.isolates.loggers.ILoggingConditionsManager#removeLoggingConditions
	 * (java.lang.String)
	 */
	@Override
	public boolean removeLoggingConditions(String aConditionsId) {

		final CLoggingConditions wLoggingConditions = pLoggingConditionsMap
				.remove(aConditionsId);

		return (wLoggingConditions != null);
	}

	/**
	 * @param aConditionLevel
	 *            The level to set.
	 * @return the number of logging condition set.
	 */
	@Override
	public int setAllConditionsLevel(final Level aConditionLevel) {

		return pDefaultLoggingConditions.setAllConditionsLevel(aConditionLevel);
	}

	/**
	 * @return the number of logging condition set to Level.ALL
	 */
	@Override
	public int setAllConditionsToAll() {

		return pDefaultLoggingConditions.setAllConditionsToAll();
	}

	/**
	 * @return the number of logging condition set to Level.OFF
	 */
	@Override
	public int setAllConditionsToOff() {

		return pDefaultLoggingConditions.setAllConditionsToOff();
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

		return pDefaultLoggingConditions.setConditionLevel(aConditionId,
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

		return pDefaultLoggingConditions.setConditionLevel(aConditionId,
				aConditionLevel);
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

		for (final Entry<String, CLoggingConditions> wEntry : pLoggingConditionsMap
				.entrySet()) {

			wArray.put(wEntry.getValue().getId());
		}

		wObj.put(PROP_CONDITIONS_IDS, wArray);
		return wObj;
	}

	@Validate
	void validate() {
		pIsolateLoggerSvc.logInfo(this, "validate", "validating...");
		try {

			pDefaultLoggingConditions = new CLoggingConditions(DEFAULT,
					pIsolateLoggerSvc, pPlatformDirsSvc);

			pLoggingConditionsMap.put(DEFAULT, pDefaultLoggingConditions);

			pDefaultLoggingConditions.read();

		} catch (Exception | Error e) {
			pIsolateLoggerSvc.logSevere(this, "validate", "ERROR: %s", e);

		}
		pIsolateLoggerSvc.logInfo(this, "validate", "validated");
	}

}
