package org.psem2m.isolates.base.internal;

import java.util.ArrayList;
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
import org.psem2m.isolates.base.IIsolateBaseActivator;
import org.psem2m.isolates.base.IIsolateLoggerAdmin;
import org.psem2m.isolates.base.IIsolateLoggerSvc;
import org.psem2m.utilities.CXException;
import org.psem2m.utilities.CXStringUtils;
import org.psem2m.utilities.json.JSONArray;
import org.psem2m.utilities.json.JSONException;
import org.psem2m.utilities.json.JSONObject;

/**
 * MOD_OG_20160906
 *
 * Put in place the commands:
 * <ul>
 * <li>isolatelogger:infos</li>
 * <li>isolatelogger:setlevel</li>
 * </ul>
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
@Component(name = "cohorte-isolate-base-logger-gogocommand-factory")
@Instantiate(name = "cohorte-isolate-base-logger-gogocommand")
@Provides(specifications = { IIsolateLoggerAdmin.class, IMessageListener.class })
public class CIsolateLoggerGogoCommand implements IIsolateLoggerAdmin,
		IMessageListener {

	/*
	 * The Gogo commands name. ATTENTION : look at the name of the methods and
	 * the declaration
	 */
	private static final String COMMAND_INFOS = "infos";
	private static final String COMMAND_SETLEVEL = "setLevel";
	private static final String COMMAND_SETLEVELALL = "setLevelAll";

	private static final String[] COMMANDS = { COMMAND_INFOS, COMMAND_SETLEVEL,
		COMMAND_SETLEVELALL };

	private static final String HERALD_GROUP_ALL = "all";

	/* the name of the members of the json object sent to the otehr isolates */
	private static final String MEMBER_COMMANDARGS = "args";
	private static final String MEMBER_COMMANDID = "commandeid";

	/* The Gogo commands declaration. ATTENTION : look at the constants */
	@ServiceProperty(name = "osgi.command.function", value = "{infos,setLevel,setLevelAll}")
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

		// System.out.printf("isolate-base: %50s | instanciated \n", this
		// .getClass().getName());
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
	Message buildCommandMessage(final String aCommandId, final String... aArgs)
			throws JSONException {

		JSONObject wCommand = new JSONObject();
		wCommand.put(MEMBER_COMMANDID, aCommandId);
		JSONArray wArgs = new JSONArray();
		for (String wArg : aArgs) {
			wArgs.put(wArg);
		}
		wCommand.put(MEMBER_COMMANDARGS, wArgs);

		Message wMessage = new Message(
				IIsolateLoggerAdmin.ISOLATES_LOGGER_MESSAGE);
		wMessage.setContent(wCommand.toString(2));

		return wMessage;
	}

	/**
	 * @param aGroupId
	 */
	private boolean fireLoggerCommand(final String aGroupId,
			final Message aMessage) {

		pLogger.logInfo(this, "fireLoggerCommand", "group=[%s] Message:%s",
				aGroupId, aMessage);

		try {
			pHerald.fireGroup("all", aMessage);
			return true;
		} catch (NoTransport e) {
			pLogger.logSevere(this, "fireLoggerCommand", "ERROR: %s", e);
			return false;
		}
	}

	/**
	 * @return
	 */
	private CIsolateLoggerChannel getIsolateLoggerChannel() {
		return pIsolateBaseActivator.getIsolateLoggerChannel();
	}

	/**
	 * @param aMimeType
	 * @return
	 * @throws
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
	private String[] getValidCommandArgs(final JSONObject aCommand)
			throws Exception {

		validJSONObjectMember(aCommand, "command", MEMBER_COMMANDARGS);

		JSONArray wJSONArray = aCommand.getJSONArray(MEMBER_COMMANDARGS);
		int wMax = wJSONArray.length();
		List<String> wArgs = new ArrayList<String>();
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
	private String getValidCommandId(final JSONObject aCommand)
			throws Exception {

		validJSONObjectMember(aCommand, "command", MEMBER_COMMANDID);

		String wCommandId = aCommand.getString(MEMBER_COMMANDID);

		for (String wKnwonCommand : COMMANDS) {
			if (wKnwonCommand.equalsIgnoreCase(wCommandId)) {
				return wKnwonCommand;
			}
		}
		throw new Exception(String.format(
				"The command [%s] isn't a isolatelogger command. %s",
				wCommandId, CXStringUtils.stringTableToString(pCommands)));
	}

	/**
	 * receive the message having the subject :
	 * "cohorte/runtime/isolates/logger"
	 *
	 * @see IIsolateLoggerSvc.ISOLATES_LOGGER_MESSAGE
	 *
	 *      These message must have a content
	 *
	 * <pre>
	 *
	 * </pre>
	 *
	 *
	 *
	 * @see org.cohorte.herald.IMessageListener#heraldMessage(org.cohorte.herald.IHerald,
	 *      org.cohorte.herald.MessageReceived)
	 */
	@Override
	public void heraldMessage(final IHerald aHerald,
			final MessageReceived aMessage) throws HeraldException {

		try {
			String wSubject = aMessage.getSubject();
			Object wContent = aMessage.getContent();
			boolean wHasContent = wContent != null;
			String wMessageData = (wContent instanceof String) ? (String) wContent
					: null;
			pLogger.logInfo(
					this,
					"heraldMessage",
					"receiving a message: subject=[%s] HasContent=[%s] data=[%s]",
					wSubject, wHasContent, wMessageData);

			if (wMessageData == null) {
				if (!wHasContent) {
					throw new Exception("The message has no content");
				}
				String wContentClassName = wContent.getClass().getSimpleName();
				throw new Exception(
						String.format(
								"The content of the message is an instance of [%s] not a String",
								wContentClassName));
			}

			JSONObject wCommand;
			try {
				wCommand = new JSONObject(wMessageData);
			} catch (Exception e) {
				throw new Exception(
						"Unable to instanciate a JSONObject using the content of the message",
						e);

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
					throw new Exception(
							"Unable to launch the command setLevel, one argument needed");
				}
				setLevel(wArgs[0]);

			}
			default:
				pLogger.logSevere(this, "heraldMessage",
						"ERROR: CommandId [%s] not supported", wCommandId);
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
			String wMimeType = (aArgs.length > 0 && "application/json"
					.contains(aArgs[0].toLowerCase())) ? "application/json"
							: "text/plain";

			wOut.append(getLoggerInfos(wMimeType));

			System.out.println(wOut.toString());
			pLogger.logInfo(this, "infos", wOut.toString());

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
	 * Gogo command "setLevel"
	 */
	@Descriptor("Set the log level of the isolatelogger")
	@Override
	public void setLevel(
			@Descriptor("the name of the level to set") String aLevelName) {

		pLogger.logInfo(this, "setLevel", "set the level to [%s]", aLevelName);

		try {

			Level wLevelBefore = pLogger.getLevel();

			pLogger.setLevel(aLevelName);

			Level wLevelAfter = pLogger.getLevel();

			System.out.println(String.format(
					"IsolateLogger's level set to=[%s] (before=[%s])",
					wLevelAfter.getName(), wLevelBefore.getName()));

		} catch (Exception | Error e) {
			pLogger.logSevere(this, "setLevel", "ERROR: %s", e);
		}
	}

	/**
	 * Gogo command "setLevelAll"
	 */
	@Descriptor("Set the log level of the all the isolateloggers")
	@Override
	public void setLevelAll(
			@Descriptor("the name of the level to set") String aLevelName) {

		pLogger.logInfo(this, "setLevelAll",
				"set the level of all the isolateloggers to [%s]", aLevelName);

		try {
			// dispatch the command to all other isolates
			boolean wFired = fireLoggerCommand(HERALD_GROUP_ALL,
					buildCommandMessage(COMMAND_SETLEVEL, aLevelName));

			System.out.println(String.format(
					"Command [%s] fired (%s) to the group [%s]",
					COMMAND_SETLEVEL, wFired, HERALD_GROUP_ALL));

			setLevel(aLevelName);
		} catch (Exception | Error e) {
			pLogger.logSevere(this, "setLevelAll", "ERROR: %s", e);
		}
	}

	/**
	 *
	 */
	@Validate
	public void validate() {

		pLogger.logInfo(this, "validate", "validated Commands=[%s]",
				CXStringUtils.stringTableToString(pCommands));
	}

	/**
	 * @param aObject
	 * @param aMember
	 * @throws Exception
	 */
	private void validJSONObjectMember(final JSONObject aObject,
			final String aWhat, final String aMember) throws Exception {

		if (!aObject.has(aMember)) {
			throw new Exception(String.format(
					"The JSONObject '%s' doesn't have a member [%s]", aWhat,
					aMember));
		}
	}

}
