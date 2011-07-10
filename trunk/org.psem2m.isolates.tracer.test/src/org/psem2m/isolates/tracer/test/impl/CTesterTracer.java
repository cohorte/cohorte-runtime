package org.psem2m.isolates.tracer.test.impl;

import java.lang.management.ManagementFactory;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.psem2m.isolates.tracer.ITraceLine;
import org.psem2m.isolates.tracer.ITraceLinesProvider;
import org.psem2m.isolates.tracer.essais.TraceLine;
import org.psem2m.isolates.tracer.essais.Tracer;
import org.psem2m.isolates.tracer.impl.CTraceLine;
import org.psem2m.utilities.teststools.CConsoleTester;

/**
 * @author ogattaz
 * 
 */
public class CTesterTracer extends CConsoleTester implements
		ITraceLinesProvider {

	private final static String CMDE_TRACE = "trace";
	private final static String CMDE_TRACE_OFF = "traceoff";

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		try {
			CConsoleTester.main(new CTesterTracer(args));
		} catch (Throwable e) {
			System.out.println("main(): EXCEPTION\n");
			e.printStackTrace();
			System.exit(1);
		}
	}

	private Runnable pCurrentTrace = null;

	private int pLineIdx = 0;

	private final MBeanServer pMBeanServer = ManagementFactory
			.getPlatformMBeanServer();

	private int pNbTrace = -1;

	private final Stack<ITraceLine> pStackedLines = new Stack<ITraceLine>();

	private final ObjectName pTraceLineMBeanName = new ObjectName(
			"org.psem2m.tracer:type=TraceLine");

	private final ObjectName pTracerMBeanName = new ObjectName(
			"org.psem2m.tracer:type=Tracer");

	/**
	 * @param args
	 * @throws Exception
	 */
	public CTesterTracer(final String[] args) throws Exception {
		super(args);

		pMBeanServer.registerMBean(new Tracer(this), pTracerMBeanName);
		pMBeanServer.registerMBean(new TraceLine(this), pTraceLineMBeanName);

	}

	@Override
	protected void buildHelp(final StringBuilder aHelp) {
		this.addHelpTitle(aHelp, "CTesterTracer");
		this.addHelpLine(aHelp, "trace");
		this.addHelpSubLine(aHelp, "ex: trace ... ");
	}

	/**
	 * @param aLineIdx
	 * @return
	 */
	private ITraceLine buildLine(final int aLineIdx) {
		ITraceLine wLine = new CTraceLine(System.currentTimeMillis(),
				buildTestWho(), "buildLine", Thread.currentThread().getName(),
				buildTestLine(aLineIdx));

		return wLine;
	}

	private String buildTestLine(final int aLineIdx) {
		return String.format(" LineIdx(%d) date=[]", aLineIdx,
				System.currentTimeMillis());
	}

	private String buildTestWho() {
		return getClass().getSimpleName() + '_' + hashCode();
	}

	/**
	 * @return true : the dommand is done!
	 */
	private boolean doCmdeTrace(final StringTokenizer aST) throws Exception {

		logInfo(CMDE_TRACE + " begin");

		if (pCurrentTrace == null) {

			pNbTrace = 100;
			if (aST.hasMoreTokens()) {
				pNbTrace = Integer.parseInt(aST.nextToken());
			}
			logInfo("NbTrace [%d]", pNbTrace);

			pCurrentTrace = new Runnable() {

				@Override
				public void run() {
					try {
						for (int wI = 0; wI < pNbTrace; wI++) {
							tempo(1000);

							pStackedLines.push(buildLine(getNexLineIdx()));
							CTesterTracer.this.logInfo(
									"trace [%d/%d] stackedlines=[%d]", wI,
									pNbTrace, pStackedLines.size());

						}
					} catch (Throwable e) {
						e.printStackTrace();
					}
					pCurrentTrace = null;
					pNbTrace = -1;
					CTesterTracer.this.logInfo("CurrentTrace raz, ok");
				}
			};

			new Thread(pCurrentTrace, "CurrentTrace").start();

		}

		logInfoAndPrompt(CMDE_TRACE + " end");
		return true;
	}

	/**
	 * @return true : the dommand is done!
	 */
	private boolean doCmdeTraceOff(final StringTokenizer aST) throws Exception {

		logInfo(CMDE_TRACE_OFF + " begin");
		pNbTrace = -1;
		logInfo("NbTrace [%d]", pNbTrace);

		logInfoAndPrompt(CMDE_TRACE_OFF + " end");
		return true;
	}

	@Override
	public int getNbStackedLines() {
		return pStackedLines.size();
	}

	/**
	 * 
	 */
	private int getNexLineIdx() {
		pLineIdx++;
		return pLineIdx;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.teststools.CConsoleTester#monitorCommand(java.lang
	 * .String, java.lang.String, java.util.StringTokenizer)
	 */
	@Override
	public boolean monitorCommand(final String aCommand, final String aLine,
			final StringTokenizer aST) throws Exception {

		if (CMDE_TRACE.equalsIgnoreCase(aCommand)) {
			return doCmdeTrace(aST);
		}
		if (CMDE_TRACE_OFF.equalsIgnoreCase(aCommand)) {
			return doCmdeTraceOff(aST);
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.tracer.IJmxTraceLinesProvider#retreiveFirstStackedLine()
	 */
	@Override
	public ITraceLine retreiveFirstStackedLine() {
		return pStackedLines.pop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.psem2m.tracer.IJmxTraceLinesProvider#retreiveStackedLines()
	 */
	@Override
	public ITraceLine[] retreiveStackedLines() {
		ITraceLine[] wLines;
		synchronized (pStackedLines) {
			wLines = pStackedLines.toArray(new ITraceLine[0]);
			pStackedLines.clear();
		}
		return wLines;
	}

	/**
	 * @param aSleepTime
	 */
	private void tempo(final long aSleepTime) {
		try {
			Thread.sleep(aSleepTime);
		} catch (Throwable e) {
			// nothing
		}
	}

}
