package org.psem2m.utilities.scripting;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

/**
 * @author ogattaz
 * 
 */
public class CXJsScriptFactory extends CXJsObjectBase {

	private final static String ENGINE_RHINO = "Mozilla Rhino";

	// A reserved key, THREADING, whose value describes the behavior of the
	// engine with respect to concurrent
	// execution of scripts and maintenance of state is also defined
	private final static String PARAM_THREADING = "THREADING";

	static private final int POOL_DEFAULT_SIZE = 10;

	// The implementation satisfies the requirements of "MULTITHREADED", and
	// also, the engine maintains
	// independent values for symbols in scripts executing on different threads.
	private final static String THREADING_ISOLATED = "THREAD-ISOLATED";

	// The implementation satisfies the requirements of "THREAD-ISOLATED". In
	// addition, script executions
	// do not alter the mappings in the Bindings which is the engine scope of
	// the ScriptEngine.
	// In particular, the keys in the Bindings and their associated values are
	// the same before and after the execution of the script.
	private final static String THREADING_STATELESS = "STATELESS";

	// The engine implementation is internally thread-safe and scripts may
	// execute concurrently
	// although effects of script execution on one thread may be visible to
	// scripts on other threads.
	private final static String THREADING_STD = "MULTITHREADED";

	// Private

	/**
	 * @param aFactory
	 * @param aName
	 * @return
	 */
	public static boolean checkName(ScriptEngineFactory aFactory, String aName) {
		List<String> wNames = aFactory.getNames();
		for (String xName : wNames) {
			if (xName.equalsIgnoreCase(aName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param aFactory
	 * @param aName
	 * @return
	 */
	public static CXJsScriptFactory newInstance(ScriptEngineFactory aFactory, String aName) {
		if (aFactory.getEngineName().equalsIgnoreCase(ENGINE_RHINO)) {
			return new CXJsScriptFactoryRhino(aFactory, aName);
		} else {
			return new CXJsScriptFactory(aFactory, aName);
		}
	}

	// Nom utilise pour instancier le factory
	private final String pCallName;
	private boolean pIsMultiThreaded = false;
	public ArrayList<String> pNamesLowCase = new ArrayList<String>();
	private int pPoolCapacity = 0;
	// Pool de scriptEngines
	private final LinkedList<ScriptEngine> pPoolEngines = new LinkedList<ScriptEngine>();
	private int pPoolSize = 0;

	private final ScriptEngineFactory pScriptEngineFactory;

	private final Object pThreading;

	/**
	 * @param aFactory
	 */
	public CXJsScriptFactory(ScriptEngineFactory aFactory) {
		this(aFactory, aFactory.getLanguageName());
	}

	/**
	 * @param aFactory
	 * @param aCallName
	 */
	public CXJsScriptFactory(ScriptEngineFactory aFactory, String aCallName) {
		this(aFactory, aCallName, POOL_DEFAULT_SIZE);
	}

	/**
	 * @param aFactory
	 * @param aCallName
	 * @param aPoolCapacity
	 */
	public CXJsScriptFactory(ScriptEngineFactory aFactory, String aCallName, int aPoolCapacity) {
		pPoolCapacity = aPoolCapacity > 0 ? aPoolCapacity : POOL_DEFAULT_SIZE;
		pScriptEngineFactory = aFactory;
		pThreading = pScriptEngineFactory.getParameter(PARAM_THREADING);
		pIsMultiThreaded = pThreading != null
				&& (pThreading.equals(THREADING_STD) || pThreading.equals(THREADING_ISOLATED) || pThreading
						.equals(THREADING_STATELESS));
		pCallName = aCallName;
		for (String xName : pScriptEngineFactory.getNames()) {
			pNamesLowCase.add(xName.toLowerCase());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.scripting.CXJsObjectBase#addDescriptionInBuffer(
	 * java.lang.Appendable)
	 */
	@Override
	public Appendable addDescriptionInBuffer(Appendable aSB) {
		aSB = super.addDescriptionInBuffer(aSB);
		descrAddProp(aSB, "Name", pCallName);
		descrAddLine(aSB, "Class", getClass().getSimpleName());
		StringBuilder wNames = new StringBuilder();
		for (String xName : pScriptEngineFactory.getNames()) {
			if (wNames.length() != 0) {
				wNames.append("/");
			}
			wNames.append(xName);
		}
		descrAddLine(aSB, "OtherNames", wNames.toString());
		descrAddProp(aSB, "LanguageName", pScriptEngineFactory.getLanguageName());
		descrAddProp(aSB, "LanguageVersion", pScriptEngineFactory.getLanguageVersion());
		descrAddLine(aSB, "EngineName", pScriptEngineFactory.getEngineName());
		descrAddProp(aSB, "EngineVersion", pScriptEngineFactory.getEngineVersion());
		descrAddProp(aSB, PARAM_THREADING, pThreading == null ? "NONE" : pThreading.toString());
		return aSB;
	}

	/**
	 * @param aName
	 * @return
	 */
	public boolean checkName(String aName) {
		return aName == null ? false : pNamesLowCase.contains(aName.toLowerCase());
	}

	/**
	 * @return
	 */
	public String getCallName() {
		return pCallName;
	}

	/**
	 * @return
	 */
	public CXJsEngine getScriptEngine() {
		// On utilise me pool (un seul ScriptEngine si multihread)
		// -> Un ScriptEngine multitread et peut traiter N scripts simultanement
		// en eval ou compile
		return newScriptEngine(poolGetScriptEngine());
	}

	/**
	 * @return
	 */
	public ScriptEngineFactory getScriptEngineFactory() {
		return pScriptEngineFactory;
	}

	/**
	 * @param aMainModule
	 * @return
	 */
	public CXJsEngineInvocable getScriptEngineInvocable(CXJsSourceMain aMainModule) {
		// On cree un nouveau ScriptEngine pour chaque instance
		// -> Invoke necessite un eval avant invoke fct/metode
		// -> Un engine par source
		return new CXJsEngineInvocable(aMainModule, pScriptEngineFactory.getScriptEngine(), this);
	}

	/**
	 * @return
	 */
	public boolean isMultiThreaded() {
		return pIsMultiThreaded;
	}

	/**
	 * @return
	 */
	public boolean isMultiThreadIsolated() {
		return pIsMultiThreaded && pThreading.equals(THREADING_ISOLATED);
	}

	// Gestion du pool des engines
	// - ScriptEnginePool - Apache - package org.apache.bsf.util;

	/**
	 * @return
	 */
	public boolean isMultiThreadNotIsolated() {
		return pIsMultiThreaded && pThreading.equals(THREADING_STD);
	}

	public boolean isMultiThreadStateless() {
		return pIsMultiThreaded && pThreading.equals(THREADING_STATELESS);
	}

	protected CXJsEngine newScriptEngine(ScriptEngine aScriptEngine) {
		return new CXJsEngine(aScriptEngine, this);
	}

	/**
	 * @param eng
	 */
	public synchronized void poolFreeScriptEngine(CXJsEngine eng) {
		if (!isMultiThreaded() && eng != null) {
			pPoolEngines.add(eng.getScriptEngine()); // should I clear the
														// engine namespaces ..
			notifyAll();
		}
	}

	/**
	 * @return
	 */
	public synchronized ScriptEngine poolGetScriptEngine() {
		if (isMultiThreaded()) {
			// Un seul engine est cree
			if (pPoolEngines.isEmpty()) {
				pPoolEngines.add(pScriptEngineFactory.getScriptEngine());
			}
			return pPoolEngines.getFirst();
		} else {
			if (!pPoolEngines.isEmpty()) {
				return pPoolEngines.removeFirst();
			} else {
				if (pPoolSize < pPoolCapacity) {
					// !! Fontionne avec le freeScriptEngine - Ajout effectif
					// sur le free
					pPoolSize++;
					return pScriptEngineFactory.getScriptEngine();
				}
				while (!pPoolEngines.isEmpty()) {
					poolWaiting();
				}
				return pPoolEngines.removeFirst();
			}
		}
	}

	/**
	 * 
	 */
	public void poolWaiting() {
		try {
			wait();
		} catch (InterruptedException ie) {
		}
	}
}
