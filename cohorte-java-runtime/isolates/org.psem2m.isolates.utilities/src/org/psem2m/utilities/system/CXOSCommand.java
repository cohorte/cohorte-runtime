package org.psem2m.utilities.system;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;

import org.psem2m.utilities.CXDateTime;
import org.psem2m.utilities.CXException;
import org.psem2m.utilities.CXOSUtils;
import org.psem2m.utilities.CXStringUtils;
import org.psem2m.utilities.logging.CActivityLoggerNull;
import org.psem2m.utilities.logging.CLogLineBuffer;
import org.psem2m.utilities.logging.IActivityLogger;

/**
 * Represente une commande systeme
 * 
 * Permet d'executer une commande et d'obtenir le resultat de l'execution
 * 
 */
public class CXOSCommand implements IXOSCommand {

	// TimeOut au demarrage du service tomcat
	public final static int CMD_RUN_EXCEPTION = -4;

	private final static String LIB_LENGTH = "length";
	private final static String LIB_METHOD_RPS = "readProcessStream";
	private final static String LIB_NAME = "StreamName";
	private final static String LIB_SIZE = "Size";
	private Exception pBuffReadingException = null;
	private String pCommandLine = null;
	private IActivityLogger pLogger = null;

	// l'encodage par defaut
	private final String pRunBuffEncoding;
	private StringBuilder pRunBuffErr = null;
	private StringBuilder pRunBuffOutput = null;
	private Exception pRunException = null;

	private int pRunExitValue;

	private int pRunExitValueOk = CXOSLauncher.CMD_RUN_OK;

	private long pRunTimeOut = 0;

	private long pRunTimeStart = 0;

	private long pRunTimeStop = 0;

	/**
	 * @param aTracer
	 * @param aCommandLine
	 */
	public CXOSCommand(IActivityLogger aTracer, String aCommandLine) {
		this(aTracer, aCommandLine, CXOSLauncher.CMD_RUN_OK);
	}

	/**
	 * @param aCommandLine
	 * @param aExitValueOk
	 *            Valeur renvoyee par la commande si OK (0 par defaut)
	 */
	public CXOSCommand(IActivityLogger aTracer, String aCommandLine,
			int aExitValueOk) {
		super();
		pLogger = aTracer;
		pCommandLine = aCommandLine;
		pRunExitValueOk = aExitValueOk;
		pRunBuffEncoding = CXOSUtils.getStdOutEncoding();
	}

	/**
	 * 
	 * @param aCommand
	 *            La ligne de commande
	 */
	public CXOSCommand(String aCommand) {
		this(aCommand, CXOSLauncher.CMD_RUN_OK);
	}

	/**
	 * aExitValueOk : Valeur renvoyee par la commande si OK (0 par defaut)
	 * 
	 * @param aCommandLine
	 * @param aExitValueOk
	 */
	public CXOSCommand(String aCommandLine, int aExitValueOk) {
		this(CActivityLoggerNull.getInstance(), aCommandLine, aExitValueOk);
	}

	/**
	 * @param aSB
	 * @param aText
	 * @return
	 */
	private StringBuilder appenTextLinesInSB(StringBuilder aSB, String aText) {
		StringTokenizer wST = new StringTokenizer(aText, "\n");
		while (wST.hasMoreTokens()) {
			aSB.append('!');
			aSB.append(wST.nextToken());
			aSB.append('\n');
		}
		return aSB;
	}

	/**
	 * @return
	 */
	private String buffReadingErrToString() {
		return CXException.eMiniInString(pBuffReadingException);
	}

	public String getCommandLine() {
		return pCommandLine;
	}

	/**
	 * @return
	 */
	public String getErrMess() {
		StringBuffer wSB = new StringBuffer();
		if (hasRunStdOutputErr()) {
			wSB.append(getRunStdOutputErr());
		}
		if (isRunException()) {
			if (wSB.length() > 0)
				wSB.append(", ");
			wSB.append(getRunException().getClass().getName());
			wSB.append(", ");
			wSB.append(getRunException().getMessage());
		}

		return wSB.toString();
	}

	public long getRunElapsedTime() {
		if (pRunTimeStop != 0 && pRunTimeStart != 0)
			return pRunTimeStop - pRunTimeStart;
		else
			return 0;
	}

	public Exception getRunException() {
		return pRunException;
	}

	/**
	 * 15w_013 - Fiche 43185 - Administration sous Linux RedHat5
	 * 
	 * @return
	 */
	public String getRunExitString() {
		// 16w_109 - Fiche 55216 - TimeOut au demarrage du service tomcat
		if (pRunExitValue == CMD_RUN_EXCEPTION)
			return "CMD_RUN_EXCEPTION";
		return CXOSLauncher.exitValueToStr(getRunExitValue());
	}

	public int getRunExitValue() {
		return pRunExitValue;
	}

	public String getRunStdOutput() {
		if (isLaunched() && hasRunStdOutput())
			return CXStringUtils.strFullTrim(pRunBuffOutput.toString());
		else
			return new String();
	}

	public String getRunStdOutputErr() {
		if (isLaunched() && hasRunStdOutputErr())
			return CXStringUtils.strFullTrim(pRunBuffErr.toString());
		else
			return new String();
	}

	/**
	 * 16w_104 - Fiche 50792 - Admin - Erreur de detection du fonctionnement de
	 * tomcat
	 * 
	 * @param aStream
	 * @return
	 */
	private String getStreamId(InputStream aStream) {
		StringBuilder wSB = new StringBuilder();
		wSB.append(aStream.getClass().getSimpleName());
		wSB.append('_');
		wSB.append(aStream.hashCode());
		return wSB.toString();
	}

	/**
	 * @return
	 */
	private boolean hasBuffReadingErr() {
		return (pBuffReadingException != null);
	}

	public boolean hasRunStdOutput() {
		return pRunBuffOutput != null && pRunBuffOutput.length() > 0;
	}

	/**
	 * @return
	 */
	public boolean hasRunStdOutputErr() {
		return pRunBuffErr != null && pRunBuffErr.length() > 0;
	}

	public boolean isExitOk() {
		return getRunExitValue() == pRunExitValueOk;
	}

	public boolean isLaunched() {
		return pRunTimeStop != 0 && pRunTimeStart != 0;
	}

	public boolean isRunException() {
		return getRunException() != null;
	}

	public boolean isRunOk() {
		return isLaunched()
				&& isExitOk()
				&& !(isRunException() || isRunTimeOut() || hasRunStdOutputErr());
	}

	public boolean isRunTimeOut() {
		return pRunExitValue == CXOSLauncher.CMD_RUN_TIMEOUT;
	}

	/**
	 * 16w_104 - Fiche 49679 - Admin - Le serveur Tomcat n'est pas arrete e tort
	 * 
	 * @return
	 */
	public void razRunStdOutputErr() {
		pRunBuffErr = null;
	}

	/**
	 * Lecture du buffer sur callback
	 * 
	 * @param aSB
	 * @param aStream
	 * @throws Exception
	 */
	protected void readProcessStream(StringBuilder aSB, InputStream aStream)
			throws Exception {
		int wSize = aStream.available();
		if (wSize > 0) {
			byte[] wReadBuffer = new byte[wSize];
			aStream.read(wReadBuffer, 0, wSize);
			aSB.append(readString(wReadBuffer));
			// 16w_104 - Fiche 50792 - Admin - Erreur de detection du
			// fonctionnement de tomcat
			traceReadProcessStream(getStreamId(aStream), wSize, aSB.length());
		}
	}

	/**
	 * @param aReadBuffer
	 * @return
	 * @correction 2005-05-11 gestion des erreurs d'encoding pendant des
	 *             lectures de buffer
	 */
	private String readString(byte[] aReadBuffer) {
		try {
			return new String(aReadBuffer, pRunBuffEncoding);
		} catch (Exception e) {
			pBuffReadingException = e;
			return new String(aReadBuffer);
		}
	}

	/**
	 * Ececute la commande
	 * 
	 * @param aTimeOut
	 *            si <=0 --> No timeout
	 * @return
	 */
	public boolean run(long aTimeOut) {
		traceRun(aTimeOut);
		try {
			if (runDoBefore(aTimeOut)) {
				pRunExitValue = CXOSLauncher.launch(pLogger, getCommandLine(),
						this, aTimeOut);
			}
		} catch (Exception e) {
			setRunException(e);
		}
		return runDoAfter();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.adonix.adminsrv.utils.IXOSCommand#runCallBack(java.io.OutputStream,
	 * java.io.InputStream, java.io.InputStream, long)
	 */
	@Override
	public boolean runCallBack(OutputStream aBufferInput,
			InputStream aBufferOutput, InputStream aBufferOutputErr,
			long aElaspedTime) throws Exception {
		// System.out.println(aElaspedTime);
		readProcessStream(pRunBuffOutput, aBufferOutput);
		readProcessStream(pRunBuffErr, aBufferOutputErr);
		return true;
	}

	protected boolean runDoAfter() {
		pRunTimeStop = System.currentTimeMillis();
		return isRunOk();
	}

	/**
	 * @param aTimeOut
	 * @return
	 */
	protected boolean runDoBefore(long aTimeOut) {
		pRunTimeStart = System.currentTimeMillis();
		pRunTimeStop = 0;
		pRunTimeOut = aTimeOut;
		pRunBuffOutput = new StringBuilder();
		pRunBuffErr = new StringBuilder();
		pRunExitValue = CXOSLauncher.CMD_RUN_NO;
		pRunException = null;
		return true;
	}

	private void setRunException(Exception aExep) {
		pRunException = aExep;
		// TimeOut au demarrage du service tomcat
		pRunExitValue = CMD_RUN_EXCEPTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder wResult = new StringBuilder(2048);
		wResult.append("CommandLine   : ").append(getCommandLine())
				.append('\n');
		wResult.append("OutputEncoding: ").append(pRunBuffEncoding)
				.append(" (").append(CXOSUtils.getOsName()).append(',')
				.append(CXOSUtils.getOsFileEncoding()).append(')').append('\n');
		wResult.append("Launched      : ");
		if (isLaunched()) {
			wResult.append(CXDateTime.time2StrAAAAMMJJ(pRunTimeStart, '/'))
					.append(" at ")
					.append(CXDateTime.time2StrHHMMSS(pRunTimeStart, ':'))
					.append('\n');
		} else {
			wResult.append("Not launched.\n");
		}
		if (isLaunched()) {
			wResult.append("--> LaunchResult=")
					.append(CXStringUtils.boolToOkKo(isRunOk())).append('\n');
			wResult.append("--> ElapsedTime =").append(getRunElapsedTime())
					.append('\n');
			wResult.append("--> Timeout     =")
					.append((pRunTimeOut > 0) ? pRunTimeOut : "undefined")
					.append('\n');
			wResult.append("--> isRunOk     =").append(isRunOk()).append('\n');
			wResult.append("--> isExitOk    =").append(isExitOk()).append('\n');
			wResult.append("--> ExitValue   =").append(getRunExitString())
					.append('\n');

			if (isRunException()) {
				wResult.append("--> RunException=").append(isRunException())
						.append('\n');
				wResult.append("--> Name        =")
						.append(getRunException().getClass().getName())
						.append('\n');
				wResult.append("--> Message     =")
						.append(getRunException().getMessage()).append('\n');
				wResult.append(
						CXStringUtils.getExceptionStack(getRunException()))
						.append('\n');
			}
			if (isRunTimeOut()) {
				wResult.append("--> RunTimeOut  =").append(isRunTimeOut())
						.append('\n');
			}

			// 2005-05-11 gestion des erreurs d'encoding pendant des lectures de
			// buffer
			if (hasBuffReadingErr()) {
				wResult.append("--> ERR READING BUFFERS\n");
				appenTextLinesInSB(wResult, buffReadingErrToString());
			}
			if (hasRunStdOutput()) {
				wResult.append("--> BUFFER OUTPUT\n");
				appenTextLinesInSB(wResult, getRunStdOutput());
			}
			if (hasRunStdOutputErr()) {
				wResult.append("--> BUFFER ERROR\n");
				appenTextLinesInSB(wResult, getRunStdOutputErr());
			}
		}
		return wResult.toString();
	}

	/**
	 * @param aTimeOut
	 */
	private void traceReadProcessStream(String aStreamName, int wSize,
			int aLength) {
		if (pLogger.isLogDebugOn()) {
			CLogLineBuffer wLLB = pLogger.popLogLineBuffer();
			wLLB.appendDescr(LIB_NAME, aStreamName);
			wLLB.appendDescr(LIB_SIZE, wSize);
			wLLB.appendDescr(LIB_LENGTH, aLength);
			pLogger.logDebug(this, LIB_METHOD_RPS, wLLB);
			pLogger.pushLogLineBuffer(wLLB);
		}
	}

	/**
	 * @param aTimeOut
	 */
	private void traceRun(long aTimeOut) {
		if (pLogger.isLogDebugOn()) {
			CLogLineBuffer wLLB = pLogger.popLogLineBuffer();
			wLLB.appendDescr("TimeOut", aTimeOut);
			pLogger.logDebug(this, "run", wLLB);
			pLogger.pushLogLineBuffer(wLLB);
		}
	}
}
