/**
 * File:   CXTimedoutCall.java
 * Author: Thomas Calmant
 * Date:   22 juil. 2011
 */
package org.psem2m.utilities;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Thomas Calmant
 * 
 */
public class CXTimedoutCall {

	/**
	 * Calls the given method and returns at last after the given timeout.
	 * 
	 * @param <V>
	 *            Method return type
	 * @param aCallable
	 *            Method to call
	 * @param aTimeout
	 *            Result timeout in milliseconds
	 * @return The method result
	 * @throws TimeoutException
	 *             The timeout raised before the called method returns
	 * @throws InterruptedException
	 *             The thread was interrupter before the called method returns
	 * @throws ExecutionException
	 *             The method raised an exception
	 */
	public static <V> V call(final Callable<V> aCallable, final int aTimeout)
			throws TimeoutException, InterruptedException, ExecutionException {

		ExecutorService callExecutor = Executors.newSingleThreadExecutor();
		Future<V> future = callExecutor.submit(aCallable);

		try {
			return future.get(aTimeout, TimeUnit.MILLISECONDS);

		} finally {
			// Always cancel the call and
			future.cancel(true);
		}
	}
}
