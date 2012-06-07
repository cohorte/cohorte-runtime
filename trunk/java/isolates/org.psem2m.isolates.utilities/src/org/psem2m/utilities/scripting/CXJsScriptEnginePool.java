package org.psem2m.utilities.scripting;

import java.util.LinkedList;

public class CXJsScriptEnginePool {

	static private final int DEFAULT_SIZE = 10;

	private final LinkedList<CXJsEngine> pool = new LinkedList<CXJsEngine>();
	private int engines = 0;
	private int capacity = 0;
	private final CXJsScriptFactory factory;
	private boolean isMultithreaded = false;

	public CXJsScriptEnginePool(CXJsScriptFactory factory, int capacity) {
		this.factory = factory;
		this.capacity = capacity;
		this.isMultithreaded = factory.isMultiThreaded();
	}

	public CXJsScriptEnginePool(CXJsScriptFactory factory) {
		this(factory, DEFAULT_SIZE);
	}

	public synchronized void free(CXJsEngine eng) {
		pool.add(eng); // should I clear the engine namespaces ..
		notifyAll();
	}

	public synchronized CXJsEngine get() {
		if (isMultithreadingSupported()) {
			if (pool.isEmpty())
				pool.add(factory.getScriptEngine());
			return pool.getFirst();
		} else {
			if (!pool.isEmpty()) {
				return pool.removeFirst();
			} else {
				if (engines < capacity) {
					engines++;
					return factory.getScriptEngine();
				}
				while (!pool.isEmpty()) {
					waiting();
				}
				return pool.removeFirst();
			}
		}
	}

	public boolean isMultithreadingSupported() {
		return this.isMultithreaded;
	}

	public void waiting() {
		try {
			wait();
		} catch (InterruptedException ie) {
		}
	}
}
