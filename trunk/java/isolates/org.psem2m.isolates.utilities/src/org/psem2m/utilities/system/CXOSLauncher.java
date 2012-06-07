package org.psem2m.utilities.system;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

import org.psem2m.utilities.CXOSUtils;
import org.psem2m.utilities.logging.CLogLineBuffer;
import org.psem2m.utilities.logging.IActivityLogger;

public class CXOSLauncher {

	// Not launched - init value
	public final static int CMD_RUN_NO = -1;
	// Exit standard si launch OK
	public final static int CMD_RUN_OK = 0;
	// Exit en arret programme ou utilisateur (sur callback)
	public final static int CMD_RUN_STOPED = -3;
	// Exit en TimeOut
	public final static int CMD_RUN_TIMEOUT = -2;

	private final static String LIB_ELAPSED = "Elapsed";
	private final static String LIB_EXITVALUE = "ExitValue";

	private final static String LIB_FINISHED = "Finished";

	private final static String LIB_METHOD_WF = "waitFor";
	private final static long TRACE_WAIT_DELAY = 1000;

	/**
	 * 
	 * @param aExitValue
	 * @return
	 */
	public static String exitValueToStr(int aExitValue) {
		switch (aExitValue) {
		case CMD_RUN_NO:
			return "CMD_RUN_NO";
		case CMD_RUN_OK:
			return "CMD_RUN_OK";
		case CMD_RUN_STOPED:
			return "CMD_RUN_STOPED";
		case CMD_RUN_TIMEOUT:
			return "CMD_RUN_TIMEOUT";
		default:
			return String.format("exit(%d)", aExitValue);
		}
	}

	/**
	 * aTimeOut<=0 --> No timeout
	 * 
	 * @param aCmdLine
	 * @param aCallBack
	 * @param aTimeOut
	 * @return
	 * @throws Exception
	 */
	public static int launch(IActivityLogger aLogger, String aCmdLine,
			IXOSCommand aCallBack, long aTimeOut) throws Exception {
		Runtime wRunTime = Runtime.getRuntime();
		Process wProcess = wRunTime.exec(aCmdLine);
		return waitFor(aLogger, wProcess, aCallBack, aTimeOut);
	}

	/**
	 * 
	 * @param aMillis
	 */
	private static void sleep(long aMillis) {
		try {
			Thread.sleep(aMillis);
		} catch (InterruptedException e2) {
		}
	}

	/**
	 * @param aLogger
	 * @param aMethodName
	 * @param aFinished
	 * @param aExitValue
	 */
	private static void traceEndWait(IActivityLogger aLogger,
			String aMethodName, boolean aFinished, int aExitValue, long aElapsed) {
		if (aLogger.isLogDebugOn()) {
			CLogLineBuffer wLLB = aLogger.popLogLineBuffer();
			wLLB.appendDescr(LIB_FINISHED, aFinished);
			wLLB.appendDescr(LIB_ELAPSED, aElapsed);
			wLLB.appendDescr(LIB_EXITVALUE, aExitValue,
					exitValueToStr(aExitValue));
			aLogger.logDebug(CXOSUtils.class, LIB_METHOD_WF, wLLB);
			aLogger.pushLogLineBuffer(wLLB);
		}
	}

	/**
	 * @param aLogger
	 * @param aLastTrace
	 * @param aMethodName
	 * @param aId
	 * @param aValue
	 * @return
	 */
	private static long traceWait(IActivityLogger aLogger, long aLastTrace,
			long aElapsed) {
		long wNow = System.currentTimeMillis();
		if (aLogger.isLogDebugOn() && (wNow - aLastTrace > TRACE_WAIT_DELAY)) {
			aLastTrace = wNow;
			aLogger.logDebug(CXOSUtils.class, LIB_METHOD_WF, "%s=[%d]",
					LIB_ELAPSED, aElapsed);
		}
		return aLastTrace;
	}

	/**
	 * @param aProcess
	 * @param aCallBack
	 * @param aTimeOut
	 *            si 0 --> No timeout
	 * @return
	 * @throws Exception
	 */
	private static int waitFor(IActivityLogger aLogger, Process aProcess,
			IXOSCommand aCallBack, long aTimeOut) throws Exception {
		BufferedOutputStream wProcBufferIn = new BufferedOutputStream(
				aProcess.getOutputStream());
		BufferedInputStream wProcBufferOut = new BufferedInputStream(
				aProcess.getInputStream());
		BufferedInputStream wProcBufferErr = new BufferedInputStream(
				aProcess.getErrorStream());

		int wExitValue = CMD_RUN_NO;
		boolean wFinished = false;
		long wStartTime = System.currentTimeMillis();
		long wLastTrace = 0;
		long wElapsed = 0;

		while (!wFinished) {
			wElapsed = System.currentTimeMillis() - wStartTime;
			wLastTrace = traceWait(aLogger, wLastTrace, wElapsed);
			try {
				// lecture des flux
				if (!aCallBack.runCallBack(wProcBufferIn, wProcBufferOut,
						wProcBufferErr, wElapsed)) {
					// runCallBack=false - Force l'arret du process
					wExitValue = CMD_RUN_STOPED;
					aProcess.destroy();
					wFinished = true;
				} else {
					// Demande la valeur de sortie du process - si pas
					// d'exception => valeur, donc fini !
					// throw IllegalThreadStateException if the subprocess
					// represented by this Process
					// object has not yet terminated.
					// the exit value of the subprocess represented by this
					// Process object.
					// by convention, the value 0 indicates normal termination.

					// Sous XP, aProcess.exitValue() ne retourne pas la valeur
					// de l'exit value !

					// Executer une application externe en Java
					// http://ydisanto.developpez.com/tutoriels/j2se/runtime/
					//

					// http://bugs.sun.com/view_bug.do?bug_id=6420270
					// Submitted On 15-JAN-2009
					// KyleCartmell
					// There are 20 uses of java.lang.InterruptedException in
					// Java 6.
					// http://java.sun.com/j2se/1.4.2/docs/api/java/lang/class-use/InterruptedException.html
					//
					// I took some time tonight to test each and every one of
					// them.
					// I hope I exercised each instance properly to make this
					// point, please correct me if I did not.
					// http://wakka.net/public_files/InterruptFlagResetTest.java
					//
					// 19 of these uses DO clear the interrupt flag prior to
					// throwing InterruptedException.
					// 1 of these uses does not clear the interrupt flag:
					// Process.waitFor
					//
					// I noticed the following quote on the Sun website...
					// "By convention, any method that exits by throwing an InterruptedException clears interrupt status when it does so."
					// http://java.sun.com/docs/books/tutorial/essential/concurrency/interrupt.html
					//
					// If Process.waitFor is the only method that throws
					// InterruptedException without clearing the interrupt flag,
					// it should be documented as an exceptional case.
					// While I agree that this is not a "defect", this certainly
					// is an inconsistancy and I strongly recommend that either
					// the code be changed to behave in the traditional manner
					// or the documentation be updated to reflect this oddity in
					// the interest of saving countless hours of debugging. ;)
					//
					// Thank You!

					wExitValue = aProcess.exitValue();
					wFinished = true;
				}
			} catch (IllegalThreadStateException e1) {
				// Sleep a little to save on CPU cycles
				sleep(50);
				if (aTimeOut > 0 && wElapsed > aTimeOut) {
					wExitValue = CMD_RUN_TIMEOUT;
					wFinished = true;
				}
			}
		}
		traceEndWait(aLogger, LIB_METHOD_WF, wFinished, wExitValue, wElapsed);
		// lecture des flux apres boucle au cas oe
		// wProcBufferIn force e null cal le stream est ferme
		aCallBack.runCallBack(null, wProcBufferOut, wProcBufferErr, wElapsed);
		return wExitValue;
	}
}
