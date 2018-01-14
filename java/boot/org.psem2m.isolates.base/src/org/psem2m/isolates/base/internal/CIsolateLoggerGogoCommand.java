package org.psem2m.isolates.base.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.service.command.Descriptor;
import org.cohorte.herald.HeraldException;
import org.cohorte.herald.IHerald;
import org.cohorte.herald.IMessageListener;
import org.cohorte.herald.Message;
import org.cohorte.herald.MessageReceived;
import org.cohorte.herald.NoTransport;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.psem2m.isolates.base.IGoGoCommand;
import org.psem2m.isolates.base.IIsolateBaseActivator;
import org.psem2m.isolates.base.IIsolateLoggerAdmin;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.utilities.CXDateTime;
import org.psem2m.utilities.CXException;
import org.psem2m.utilities.CXStringUtils;
import org.psem2m.utilities.json.JSONArray;
import org.psem2m.utilities.json.JSONException;
import org.psem2m.utilities.json.JSONObject;

/**
 * MOD_OG_20160906
 * 
 * MOD_OG_1.0.17 change component name and adopt IGogoCommand model
 *
 * Put in place the commands:
 * 
 * <pre>
isolatelogger:infos
isolatelogger:lsdump
isolatelogger:lstest
isolatelogger:setLevel
isolatelogger:setLevelAll
 * </pre>
 * 
 * Heard the herald messages "isolatelogger/setlevel"
 *
 * @see http://kachel.biz/info/roots/usingGogo_en.html
 *
 *      For using command method annotations the following package import is
 *      required in bundle Manifest:
 *      org.apache.felix.service.command;status=provisional;version="0.10.0"
 *
 * @author ogattaz
 *
 */
@Component(name = "cohorte-isolate-base-isolatelogger-gogocommand-factory")
@Instantiate(name = "cohorte-isolate-base-isolatelogger-gogocommand")
@Provides(specifications = { IIsolateLoggerAdmin.class, IMessageListener.class, IGoGoCommand.class })
public class CIsolateLoggerGogoCommand implements IIsolateLoggerAdmin, IMessageListener, IGoGoCommand {

	private static final String COMMAND_INFOS = "infos";
	/*
	 * The Gogo commands name. ATTENTION : look at the name of the methods and the
	 * declaration
	 */
	private static final String COMMAND_LSDUMP = "lsdump";
	private static final String COMMAND_LSTEST = "lstest";
	private static final String COMMAND_SETLEVEL = "setLevel";
	private static final String COMMAND_SETLEVELALL = "setLevelAll";

	private static final String[] COMMANDS = { COMMAND_INFOS, COMMAND_LSDUMP, COMMAND_LSTEST, COMMAND_SETLEVEL,
			COMMAND_SETLEVELALL };

	private static final String HERALD_GROUP_ALL = "all";

	/* the name of the members of the json object sent to the otehr isolates */
	private static final String MEMBER_COMMANDARGS = "args";
	private static final String MEMBER_COMMANDID = "commandeid";

	private static final String SERVICE_DESCRIPTION = org.osgi.framework.Constants.SERVICE_DESCRIPTION;

	private final BundleContext pBundleContext;

	/* The Gogo commands declaration. ATTENTION : look at the constants */
	@ServiceProperty(name = "osgi.command.function", value = "{infos,lsdump,lstest,setLevel,setLevelAll}")
	private String[] pCommands;

	/** Herald message filters to only heard the logger messages */
	@ServiceProperty(name = org.cohorte.herald.IConstants.PROP_FILTERS, value = "{"
			+ IIsolateLoggerAdmin.ISOLATES_LOGGER_MESSAGE + "}")
	private String[] pFilters;
	/**
	 * Cohorte herald servie to be able to send logger command to the other java
	 * isolates
	 */
	@Requires
	private IHerald pHerald;

	/**
	 * LogService injected by IPojo => OSGi Ranking policy => best Ranking => iPOJO
	 * get the service having the highest value in its SERVICE_RANKING property.
	 *
	 * @see the registration of the LogService factory in CIsolateBaseActivator
	 * @see https ://osgi.org/javadoc/r4v42/org/osgi/framework/Constants.html
	 *      #SERVICE_RANKING
	 */
	@Requires
	private LogService pInjectedLogService1;

	/**
	 * LogService injected by IPojo = according the filter
	 */
	@Requires(filter = "(" + SERVICE_DESCRIPTION + "=cohorte)")
	private LogService pInjectedLogService2;

	/**
	 * Cohorte isolate activator to be able to retrieve the IsolateLoggerChannel
	 **/
	@Requires
	private IIsolateBaseActivator pIsolateBaseActivator;

	/**
	 * Cohorte isolate logger service
	 */
	@Requires
	private IIsolateLoggerSvc pLogger;

	/**
	 * The Gogo commands scope
	 */
	@ServiceProperty(name = "osgi.command.scope", value = "isolatelogger")
	private String pScope;

	/**
	 * Constructor
	 *
	 * @param aBundleContext
	 */
	public CIsolateLoggerGogoCommand(final BundleContext aBundleContext) {
		super();
		pBundleContext = aBundleContext;

		// System.out.printf("isolate-base: %50s | instanciated \n", this
		// .getClass().getName());
	}

	/**
	 * @param aSB
	 * @param aFormat
	 * @param aArgs
	 * @return
	 */
	private StringBuilder addLine(final StringBuilder aSB, final String aFormat, final Object... aArgs) {
		aSB.append('\n').append(String.format(aFormat, aArgs));
		return aSB;
	}

	/**
	 * build the message sent to the isolate loggers of the other java isolates
	 *
	 * @param aCommandId
	 *            the id if the command (eg. setLevel)
	 * @param aArgs
	 *            the arguments of the command
	 * @return the herald message
	 * @throws JSONException
	 */
	Message buildCommandMessage(final String aCommandId, final String... aArgs) throws JSONException {

		JSONObject wCommand = new JSONObject();
		wCommand.put(MEMBER_COMMANDID, aCommandId);
		JSONArray wArgs = new JSONArray();
		for (String wArg : aArgs) {
			wArgs.put(wArg);
		}
		wCommand.put(MEMBER_COMMANDARGS, wArgs);

		Message wMessage = new Message(IIsolateLoggerAdmin.ISOLATES_LOGGER_MESSAGE);
		wMessage.setContent(wCommand.toString(2));

		return wMessage;
	}

	/**
	 * @param aGroupId
	 */
	private boolean fireLoggerCommand(final String aGroupId, final Message aMessage) {

		pLogger.logInfo(this, "fireLoggerCommand", "group=[%s] Message:%s", aGroupId, aMessage);

		try {
			pHerald.fireGroup("all", aMessage);
			return true;
		} catch (NoTransport e) {
			pLogger.logSevere(this, "fireLoggerCommand", "ERROR: %s", e);
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.isolates.base.IGogoCommand#getCommands()
	 */
	@Override
	public String[] getCommands() {
		return pCommands;
	}

	/**
	 * @return
	 */
	private CIsolateLoggerChannel getIsolateLoggerChannel() {
		return pIsolateBaseActivator.getIsolateLoggerChannel();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.psem2m.isolates.base.IIsolateLoggerAdmin#getLevel()
	 */
	@Override
	public String getLevel() {
		// MOD_BD_20160919
		return pLogger.getLevel().getName();
	}

	/**
	 * @param aMimeType
	 * @return @throws
	 */
	@Override
	public String getLoggerInfos(final String aMimeType) {

		pLogger.logInfo(this, "getLoggerInfos", "aMimeType=[%s]", aMimeType);
		try {
			if ("application/json".equalsIgnoreCase(aMimeType)) {
				return getIsolateLoggerChannel().toJson().toString(2);
			}
			// text/plain
			{
				return getIsolateLoggerChannel().toString();
			}
		} catch (JSONException e) {
			return CXException.eInString(e);
		}
	}

	/**
	 * @param aCommand
	 * @return
	 * @throws Exception
	 */
	private String[] getValidCommandArgs(final JSONObject aCommand) throws Exception {

		validJSONObjectMember(aCommand, "command", MEMBER_COMMANDARGS);

		JSONArray wJSONArray = aCommand.getJSONArray(MEMBER_COMMANDARGS);
		int wMax = wJSONArray.length();
		List<String> wArgs = new ArrayList<>();
		for (int wIdx = 0; wIdx < wMax; wIdx++) {
			wArgs.add(String.valueOf(wJSONArray.get(wIdx)));
		}
		return wArgs.toArray(new String[wArgs.size()]);
	}

	/**
	 * @param aCommand
	 * @return
	 * @throws Exception
	 */
	private String getValidCommandId(final JSONObject aCommand) throws Exception {

		validJSONObjectMember(aCommand, "command", MEMBER_COMMANDID);

		String wCommandId = aCommand.getString(MEMBER_COMMANDID);

		for (String wKnwonCommand : COMMANDS) {
			if (wKnwonCommand.equalsIgnoreCase(wCommandId)) {
				return wKnwonCommand;
			}
		}
		throw new Exception(String.format("The command [%s] isn't a isolatelogger command. %s", wCommandId,
				CXStringUtils.stringTableToString(pCommands)));
	}

	/**
	 * receive the message having the subject : "cohorte/runtime/isolates/logger"
	 *
	 * @see IIsolateLoggerSvc.ISOLATES_LOGGER_MESSAGE
	 *
	 *      These message must have a content
	 *
	 *      <pre>
	 *
	 *      </pre>
	 *
	 *
	 *
	 * @see org.cohorte.herald.IMessageListener#heraldMessage(org.cohorte.herald.IHerald,
	 *      org.cohorte.herald.MessageReceived)
	 */
	@Override
	public void heraldMessage(final IHerald aHerald, final MessageReceived aMessage) throws HeraldException {

		try {
			String wSubject = aMessage.getSubject();
			Object wContent = aMessage.getContent();
			boolean wHasContent = wContent != null;
			String wMessageData = (wContent instanceof String) ? (String) wContent : null;
			pLogger.logInfo(this, "heraldMessage", "receiving a message: subject=[%s] HasContent=[%s] data=[%s]",
					wSubject, wHasContent, wMessageData);

			if (wMessageData == null) {
				if (!wHasContent) {
					throw new Exception("The message has no content");
				}
				String wContentClassName = wContent.getClass().getSimpleName();
				throw new Exception(String.format("The content of the message is an instance of [%s] not a String",
						wContentClassName));
			}

			JSONObject wCommand;
			try {
				wCommand = new JSONObject(wMessageData);
			} catch (Exception e) {
				throw new Exception("Unable to instanciate a JSONObject using the content of the message", e);

			}

			String wCommandId = getValidCommandId(wCommand);

			switch (wCommandId) {
			case COMMAND_SETLEVEL: {
				/**
				 * <pre>
				 * {
				 *   "commandid":"setLevel",
				 *   "args": [
				 *     "FINE"
				 *   ]
				 * }
				 * </pre>
				 */
				String[] wArgs = getValidCommandArgs(wCommand);

				if (wArgs.length < 1) {
					throw new Exception("Unable to launch the command setLevel, one argument needed");
				}
				setLevel(wArgs[0]);

			}
			default:
				pLogger.logSevere(this, "heraldMessage", "ERROR: CommandId [%s] not supported", wCommandId);
			}

		} catch (Exception | Error e) {
			pLogger.logSevere(this, "heraldMessage", "ERROR: %s", e);

		}
	}

	/**
	 * Gogo command "infos"
	 */
	@Descriptor("Dump logger infos")
	public void infos(final String... aArgs) {

		pLogger.logInfo(this, "infos", "dump logger infos");
		StringBuilder wOut = new StringBuilder();
		try {
			String wMimeType = (aArgs.length > 0 && "application/json".contains(aArgs[0].toLowerCase()))
					? "application/json"
					: "text/plain";

			wOut.append(getLoggerInfos(wMimeType));

			String wResult = wOut.toString();
			System.out.println(wResult);
			pLogger.logInfo(this, "infos", wResult);

		} catch (Exception | Error e) {
			pLogger.logSevere(this, "infos", "ERROR: %s", e);
		}
	}

	/**
	 *
	 */
	@Invalidate
	public void invalidate() {

		pLogger.logInfo(this, "invalidate", "invalidated");
	}

	/**
	 * Gogo command "dumpls"
	 */
	@Descriptor("Dump the available LogService")
	public void lsdump() {
		pLogger.logInfo(this, "lsdump", "Dump the available LogService");

		try {

			printConsole("Dump LogService:");

			StringBuilder wSB = new StringBuilder();

			Collection<ServiceReference<LogService>> wLogserviceRefs = pBundleContext
					.getServiceReferences(LogService.class, null);

			LogService wLogService;
			for (ServiceReference<LogService> wLogserviceRef : wLogserviceRefs) {
				addLine(wSB, "- LogserviceRef: %s", wLogserviceRef);

				wLogService = pBundleContext.getService(wLogserviceRef);

				addLine(wSB, "  + LogService: %s", wLogService.toString());

				addLine(wSB, "  + Properties: %s", wLogserviceRef.getPropertyKeys().length);

				for (String wKey : wLogserviceRef.getPropertyKeys()) {
					Object wValue = wLogserviceRef.getProperty(wKey);

					if (wValue != null && wValue.getClass().isArray()) {
						addLine(wSB, "    - %s={", wKey);
						for (Object wObj : (Object[]) wValue) {
							addLine(wSB, "           [%s]", String.valueOf(wObj));
						}
						addLine(wSB, "           }");
					}
					// not an array
					else {
						addLine(wSB, "    - %s=[%s]", wKey, wValue);
					}
				}
			}
			String wResult = wSB.toString();
			printConsole(wResult);
			pLogger.logInfo(this, "lsdump", wResult);

		} catch (Exception | Error e) {
			pLogger.logSevere(this, "lsdump", "ERROR: %s", e);
		}
	}

	/**
	 * Gogo command "testls"
	 */
	@Descriptor("Test LogService redirections")
	public void lstest() {
		pLogger.logInfo(this, "lstest", "Test LogService redirections");

		try {
			printConsole("Test LogServices:");

			// --- A --- log in the 2 LogServices injected by iPOJO :

			// 1) injection managed by the OSGi Ranking policy
			testlogInLogService(pInjectedLogService1);

			// 2) injection managed by the LDAP filter
			testlogInLogService(pInjectedLogService2);

			// --- B --- log in the 2 LogServices retreived in the registry

			// get all the ServiceReferences of the "LogService" services
			Collection<ServiceReference<LogService>> wLogserviceRefs = pBundleContext
					.getServiceReferences(LogService.class, null);
			// cycle
			LogService wLogService;
			for (ServiceReference<LogService> wLogserviceRef : wLogserviceRefs) {
				wLogService = pBundleContext.getService(wLogserviceRef);

				testlogInLogService(wLogService);
			}
			printConsole("Test LogServices OK");

		} catch (Exception | Error e) {
			pLogger.logSevere(this, "lstest", "ERROR: %s", e);
		}
	}

	/**
	 * @param aFormat
	 * @param aArgs
	 */
	private void printConsole(final String aFormat, final Object... aArgs) {
		System.out.println(String.format(aFormat, aArgs));
	}

	/**
	 * Gogo command "setLevel"
	 */
	@Descriptor("Set the log level of the isolatelogger")
	@Override
	public String setLevel(@Descriptor("the name of the level to set") final String aLevelName) {
		// MOD_BD_20160919 return old log level
		String wOldLevel = getLevel();
		pLogger.logInfo(this, "setLevel", "set the level to [%s], old level [%s]", aLevelName, wOldLevel);

		try {
			Level wLevelBefore = pLogger.getLevel();

			pLogger.setLevel(aLevelName);

			Level wLevelAfter = pLogger.getLevel();

			System.out.println(String.format("IsolateLogger's level set to=[%s] (before=[%s])", wLevelAfter.getName(),
					wLevelBefore.getName()));

		} catch (Exception | Error e) {
			pLogger.logSevere(this, "setLevel", "ERROR: %s", e);
		}
		return wOldLevel;
	}

	/**
	 * Gogo command "setLevelAll"
	 */
	@Descriptor("Set the log level of the all the isolateloggers")
	@Override
	public void setLevelAll(@Descriptor("the name of the level to set") final String aLevelName) {

		pLogger.logInfo(this, "setLevelAll", "set the level of all the isolateloggers to [%s]", aLevelName);

		try {
			// dispatch the command to all other isolates
			boolean wFired = fireLoggerCommand(HERALD_GROUP_ALL, buildCommandMessage(COMMAND_SETLEVEL, aLevelName));

			System.out.println(String.format("Command [%s] fired (%s) to the group [%s]", COMMAND_SETLEVEL, wFired,
					HERALD_GROUP_ALL));

			setLevel(aLevelName);
		} catch (Exception | Error e) {
			pLogger.logSevere(this, "setLevelAll", "ERROR: %s", e);
		}
	}

	/**
	 * @param aLogService
	 */
	private void testlogInLogService(final LogService aLogService) {

		printConsole("Test LogService in : %s", aLogService);

		String wLine = String.format("$$$ testlog in [%s] (%s)", aLogService, CXDateTime.getIso8601TimeStamp());

		aLogService.log(LogService.LOG_INFO, wLine);
		aLogService.log(LogService.LOG_ERROR, wLine);
		aLogService.log(LogService.LOG_ERROR, wLine);
		aLogService.log(LogService.LOG_WARNING, wLine);
	}

	/**
	 *
	 */
	@Validate
	public void validate() {

		pLogger.logInfo(this, "validate", "validated Commands=[%s]", CXStringUtils.stringTableToString(pCommands));
	}

	/**
	 * @param aObject
	 * @param aMember
	 * @throws Exception
	 */
	private void validJSONObjectMember(final JSONObject aObject, final String aWhat, final String aMember)
			throws Exception {

		if (!aObject.has(aMember)) {
			throw new Exception(String.format("The JSONObject '%s' doesn't have a member [%s]", aWhat, aMember));
		}
	}

}
