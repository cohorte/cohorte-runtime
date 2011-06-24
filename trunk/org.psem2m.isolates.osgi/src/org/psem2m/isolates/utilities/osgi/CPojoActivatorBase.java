package org.psem2m.isolates.utilities.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.psem2m.utilities.CXException;
import org.psem2m.utilities.CXObjectBase;
import org.psem2m.utilities.CXStringUtils;
import org.psem2m.utilities.files.CXFile;
import org.psem2m.utilities.files.CXFileDir;
import org.psem2m.utilities.logging.CActivityLoggerBasic;
import org.psem2m.utilities.logging.IActivityLogger;

/**
 * @author isandlatech (www.isandlatech.com) - ogattaz
 * 
 */
public abstract class CPojoActivatorBase extends CXObjectBase implements
		BundleActivator {

	public static String LIB_BNDL_ID = "BundleId";

	private BundleContext context;

	/**
	 * Explicit default constructor
	 */
	public CPojoActivatorBase() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.psem2m.utilities.IXDescriber#addDescriptionInBuffer(java.lang.Appendable
	 * )
	 */
	@Override
	public Appendable addDescriptionInBuffer(final Appendable aBuffer) {
		CXStringUtils.appendKeyValInBuff(aBuffer, LIB_BNDL_ID, getContext()
				.getBundle().getBundleId());
		return aBuffer;
	}

	/**
	 * @return the id of the bundle
	 */
	public abstract String getBundleId();

	/**
	 * Retrieves the activity logger of the bundle
	 * 
	 * @return The activity logger
	 */
	public abstract IActivityLogger getBundleLogger();

	/**
	 * @return the BundleContext of the bundle
	 */
	public BundleContext getContext() {
		return context;
	}

	/**
	 * @return the logService of the framework
	 */
	protected abstract void logInLogService(int aLevel, String aLine);

	/**
	 * @return
	 */
	protected IActivityLogger newBundleLogger() {
		try {
			// -Dorg.psem2m.platform.base=${workspace_loc}/psem2m/platforms/felix.user.dir/logs
			CXFileDir wLogDir = new CXFileDir(
					System.getProperty("org.psem2m.platform.base"));
			if (!wLogDir.exists()) {
				wLogDir.createHierarchy();
			}
			CXFile wLogFile = new CXFile(wLogDir, getBundleId() + "_%g.log");
			return CActivityLoggerBasic.newLogger(getBundleId(),
					wLogFile.getAbsolutePath(), IActivityLogger.ALL,
					1024 * 1024 * 100, 5);

		} catch (Throwable e) {
			System.err.println(CXException.eInString(e));
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public final void start(final BundleContext bundleContext) throws Exception {
		context = bundleContext;

	}

	/**
	 * @param bundleContext
	 * @throws Exception
	 */
	public abstract void startPojo(final BundleContext bundleContext)
			throws Exception;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public final void stop(final BundleContext bundleContext) throws Exception {
		context = null;
	}

	/**
	 * @param bundleContext
	 * @throws Exception
	 */
	public abstract void stopPojo(final BundleContext bundleContext)
			throws Exception;

}
