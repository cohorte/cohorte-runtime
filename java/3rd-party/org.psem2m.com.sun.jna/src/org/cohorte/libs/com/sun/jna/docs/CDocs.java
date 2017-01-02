package org.cohorte.libs.com.sun.jna.docs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.cohorte.libs.com.sun.jna.Activator;

/**
 * Returns the availables documentations
 *
 * @author ogattaz
 *
 */
public class CDocs {

	private final static int ONE_MEGA_BYTES = 1024 * 1024;

	/**
	 * @param aFileName
	 * @return
	 */
	private static String buildDocResourcePath(final String aFileName) {

		return String.format("%s/%s", CDocs.class.getPackage().getName()
				.replace('.', '/'), aFileName);
	}

	/**
	 * @param aResourcePath
	 * @return
	 */
	private static URL buildDocResourceUrl(final String aResourcePath) {

		if (Activator.getContext() == null || aResourcePath == null) {
			return null;
		}

		return Activator.getContext().getBundle().getResource(aResourcePath);
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public static String getLicence() throws IOException {
		return readResourceText(buildDocResourcePath("LICENCE.txt"));
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public static String getReadme() throws IOException {
		return readResourceText(buildDocResourcePath("README.txt"));
	}

	/**
	 * @param aResourcePath
	 * @param aMax
	 * @return
	 * @throws IOException
	 */
	private static byte[] readResourceBin(final String aResourcePath,
			final int aMax) throws IOException {

		InputStream wStream = null;
		try {
			// The url maybe like this:
			// bundle://2.0:2/com/my/weager/impl/test.txt
			// But this url is not a real file path :(, you could't use it as a
			// file.
			final URL wResourceUrl = buildDocResourceUrl(aResourcePath);

			wStream = wResourceUrl.openConnection().getInputStream();

			final int wSize = wStream.available();
			if (wSize > aMax) {
				throw new IOException(
						String.format(
								"Resource size too large exeeds limit : [%s] > max [%s] bytes",
								wSize, aMax));
			}
			final byte[] wData = new byte[wSize];
			wStream.read(wData);

			return wData;

		} finally {
			if (wStream != null) {
				wStream.close();

			}
		}

	}

	/**
	 * @param aResourcePath
	 * @return
	 * @throws IOException
	 */
	private static String readResourceText(final String aResourcePath)
			throws IOException {

		BufferedReader br = null;
		try {
			// The url maybe like this:
			// bundle://2.0:2/com/my/weager/impl/test.txt
			// But this url is not a real file path :(, you could't use it as a
			// file.
			final URL wResourceUrl = buildDocResourceUrl(aResourcePath);

			// This url should be handled by the specific
			// URLHandlersBundleStreamHandler, you can look up details in
			// BundleRevisionImpl.createURL(int port,String path)
			br = new BufferedReader(new InputStreamReader(wResourceUrl
					.openConnection().getInputStream()));

			final StringBuilder wSB = new StringBuilder();
			while (br.ready()) {
				if (wSB.length() > 0) {
					wSB.append('\n');
				}
				wSB.append(br.readLine());
			}
			return wSB.toString();
		} finally {
			if (br != null) {
				br.close();
			}
		}
	}
}
