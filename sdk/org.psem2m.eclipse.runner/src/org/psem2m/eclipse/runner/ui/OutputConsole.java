/*******************************************************************************
 * Copyright (c) 2011 isandlaTech, Thomas Calmant
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Thomas Calmant (isandlaTech) - initial API and implementation
 *******************************************************************************/

package org.psem2m.eclipse.runner.ui;

import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Represents an Eclipse output console
 * 
 * @author Thomas Calmant
 */
public class OutputConsole {

	/**
	 * Stream listener for console output
	 * 
	 * @author Thomas Calmant
	 */
	protected class StreamListener implements IStreamListener {

		/** Console stream */
		private MessageConsoleStream pStream;

		/**
		 * Prepares the stream output
		 * 
		 * @param aStream
		 *            Console output stream
		 */
		public StreamListener(final MessageConsoleStream aStream) {
			pStream = aStream;
		}

		/*
		 * @see
		 * org.eclipse.debug.core.IStreamListener#streamAppended(java.lang.String
		 * , org.eclipse.debug.core.model.IStreamMonitor)
		 */
		@Override
		public void streamAppended(final String aText,
				final IStreamMonitor aMonitor) {
			pStream.print(aText);
		}
	}

	/** Error stream color */
	public static final Color ERROR_COLOR = new Color(Display.getDefault(),
			new RGB(255, 0, 0));

	/** The console */
	private MessageConsole pConsole;

	/** Error stream */
	private MessageConsoleStream pStderr;

	/** Standard output stream */
	private MessageConsoleStream pStdout;

	/**
	 * Prepares the console
	 * 
	 * @param aName
	 *            The console title
	 */
	public OutputConsole(final String aName) {

		pConsole = findConsole(aName);
		pStdout = pConsole.newMessageStream();
		pStderr = pConsole.newMessageStream();

		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				pStderr.setColor(ERROR_COLOR);
			}
		});
	}

	/**
	 * Activates the console
	 */
	public void activate() {
		pConsole.activate();
	}

	/**
	 * Prepares a console with the given name
	 * 
	 * @param aConsoleName
	 *            The console name
	 * @return A console
	 */
	private MessageConsole findConsole(final String aConsoleName) {

		ConsolePlugin consolePlugin = ConsolePlugin.getDefault();
		IConsoleManager consoleManager = consolePlugin.getConsoleManager();
		IConsole[] existingConsoles = consoleManager.getConsoles();

		for (int i = 0; i < existingConsoles.length; i++) {

			if (aConsoleName.equals(existingConsoles[i].getName())) {
				return (MessageConsole) existingConsoles[i];
			}
		}

		// no console found, so create a new one
		MessageConsole newConsole = new MessageConsole(aConsoleName, null);
		consoleManager.addConsoles(new IConsole[] { newConsole });
		return newConsole;
	}

	/**
	 * Adds a new stream listener on those streams
	 * 
	 * @param aStreamsProxy
	 *            The streams to listen to
	 */
	public void listenStreams(final IStreamsProxy aStreamsProxy) {

		aStreamsProxy.getOutputStreamMonitor().addListener(
				new StreamListener(pStdout));

		aStreamsProxy.getErrorStreamMonitor().addListener(
				new StreamListener(pStderr));
	}
}
