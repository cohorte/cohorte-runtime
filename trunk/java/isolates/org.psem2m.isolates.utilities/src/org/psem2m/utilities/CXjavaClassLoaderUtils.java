package org.psem2m.utilities;

import java.util.Set;

/**
 * @author ogattaz
 * 
 */
public class CXjavaClassLoaderUtils {

	private static boolean sIsOsgiEnv = calcOsgiEnv();

	/**
	 * @param aResourceId
	 *            the full qualified id of the resource (eg:
	 *            com.isandlatech.mytexts_fr).
	 * @return a package Id
	 */
	public static String extractPackageId(String aResourceId) {

		int wPos = aResourceId.lastIndexOf('.');
		return (wPos != -1) ? aResourceId.substring(0, wPos) : aResourceId;
	}

	/**
	 * @param aResourceId
	 *            the full qualified id of the resource (eg:
	 *            com.isandlatech.mytexts_fr).
	 * @return the classLoader which manages the package of the resource.
	 */
	public static ClassLoader getCallerClassLoader(String aResourceId) {

		try {
			String wPackageId = extractPackageId(aResourceId);

			Class<?> wCallerClass = CXJavaCallerContext.getCaller(wPackageId);

			return wCallerClass.getClassLoader();
		} catch (Exception e) {
			System.err
					.format("%s::getCallerClassLoader(): Can't get caller of package [%s]. \nException:\n%s\n%s",
							CXjavaClassLoaderUtils.class.getName(),
							aResourceId, getMessages(e),
							CXException.eInString(e));
			return null;
		}
	}

	/**
	 * Gestion automatique de la recherche du classLoader
	 * 
	 * @param aResourceId
	 *            the full qualified id of the resource (eg:
	 *            com.isandlatech.mytexts_fr).
	 * @return the classLoader which manages the package of the resource.
	 */
	public static ClassLoader getClassLoader(String aResourceId) {

		if (isOsgiEnv()) {
			return getCallerClassLoader(aResourceId);
		} else {
			return getTreadClassLoader();
		}

	}

	/**
	 * @param e
	 * @return
	 */
	private static String getMessages(Throwable e) {
		if (e == null)
			return "null";
		StringBuilder wSB = new StringBuilder();
		while (e != null) {
			String wMess = e.getLocalizedMessage();
			wSB.append(String.format("[%s]", (wMess != null) ? wMess : e
					.getClass().getSimpleName()));
		}
		return wSB.toString();
	}

	/**
	 * The context ClassLoader is provided by the creator of the thread for use
	 * by code running in this thread when loading classes and resources.
	 * 
	 * @return Returns the context ClassLoader of the current Thread.
	 */
	public static ClassLoader getTreadClassLoader() {

		return Thread.currentThread().getContextClassLoader();
	}

	/**
	 * 
	 * @return true if one key of the system properties starts whith the prefix
	 *         "osgi."
	 */
	public static boolean isOsgiEnv() {
		return sIsOsgiEnv;
	}

	/**
	 * @return true if one key of the system properties starts whith the prefix
	 *         "osgi."
	 */
	private static boolean calcOsgiEnv() {

		String wOsgiPrefix = "osgi.";
		Set<Object> wKeys = System.getProperties().keySet();
		for (Object wKey : wKeys) {
			if (wKey instanceof String
					&& ((String) wKey).toLowerCase().startsWith(wOsgiPrefix)) {
				return true;
			}
		}
		return false;
	}
}
