package org.psem2m.utilities.rsrc;

/**
 * Buffer d'octet - Tableau dynamique
 * 
 * @author ogattaz
 * 
 */
public class CXRsrcByteArray {
	private static final int H7F = (0x7F & 0xff);
	private static final int H80 = (0x80 & 0xff);
	private static final int HBF = (0xBF & 0xff);
	private static final int HC0 = (0xC0 & 0xff);
	private static final int HFD = (0xFD & 0xff);

	private byte[] pArray = null;
	private int pGrowthSize = 1024;
	private int pInitSize = 1024;
	private int pStackPointer = 0;

	/**
     * 
     */
	public CXRsrcByteArray() {
		this(1024);
	}

	/**
	 * @param aInitSize
	 */
	public CXRsrcByteArray(int aInitSize) {
		this(aInitSize, aInitSize / 4);
	}

	/**
	 * @param aInitSize
	 * @param aGrowthSize
	 */
	public CXRsrcByteArray(int aInitSize, int aGrowthSize) {
		pInitSize = aInitSize < 0 ? 1024 : aInitSize;
		pGrowthSize = aGrowthSize < 0 ? 256 : aGrowthSize;
		pArray = new byte[pInitSize];
	}

	/**
	 * @param aByte
	 */
	public void add(byte aByte) {
		if (pStackPointer >= pArray.length) {
			byte[] wTmp = new byte[pArray.length + pGrowthSize];
			System.arraycopy(pArray, 0, wTmp, 0, pArray.length);
			pArray = wTmp;
		}
		pArray[pStackPointer] = aByte;
		pStackPointer++;
	}

	/**
	 * @param aBytes
	 */
	public void add(byte[] aBytes) {
		if (aBytes != null && aBytes.length > 0) {
			add(aBytes, 0, aBytes.length);
		}
	}

	/**
	 * @param aBytes
	 * @param aSize
	 */
	public void add(byte[] aBytes, int aSize) {
		if (aBytes != null && aSize != 0) {
			add(aBytes, 0, aSize);
		}
	}

	/**
	 * @param aBytes
	 * @param aPos
	 * @param aSize
	 */
	public void add(byte[] aBytes, int aPos, int aSize) {
		if (aBytes != null && aBytes.length > 0 && aPos >= 0 && aPos < aSize && aSize > 0
				&& aSize <= aBytes.length) {
			if ((pStackPointer + aSize) >= pArray.length) // time to grow!
			{
				int wGrowth = ((aSize / pGrowthSize) + 1) * pGrowthSize;
				byte[] wTmp = new byte[pArray.length + wGrowth];
				System.arraycopy(pArray, 0, wTmp, 0, pArray.length);
				pArray = wTmp;
			}
			System.arraycopy(aBytes, aPos, pArray, pStackPointer, aSize);
			pStackPointer += aSize;
		}
	}

	/**
	 * Renvoie true si le buffer contient au moins une sequences UTF-8 et que
	 * toutes les sequences d'octet sont compatibles avec UTF-8 Renvoie false si
	 * pas de sequence UTF-8 ou si une sequence non compatible avec UTF-8 a ete
	 * detectee
	 * 
	 * @return true
	 */
	public boolean checkUTF_8() {
		// Detection sequence UTF-F8
		int nLen = getSize();
		int good_cnt = 0;
		int escaped = 0;
		for (int i = 0; i < nLen; i++) {
			int c = (pArray[i] & 0xff);
			if (((escaped == 1) && !(c >= H80 && c <= HBF))
					|| ((escaped == 0) && (c >= H80 && c <= HBF))) {
				// "Dead" combination ocuried - it is not UTF8
				good_cnt = -1;
				break;
			}
			if (c >= HC0 && c <= HFD) {
				escaped = 1;
			}
			if (c <= H7F) {
				escaped = 0;
			}
			if (c >= H80 && c <= HBF) {
				escaped++;
				good_cnt++;
			}
		}
		return (good_cnt > 0);
	}

	/**
	 * @param aBytes
	 * @return
	 */
	public byte[] copyTo(byte[] aBytes) {
		if (aBytes != null && aBytes.length != 0) {
			int wSize = aBytes.length > getSize() ? getSize() : aBytes.length;
			System.arraycopy(pArray, 0, aBytes, 0, wSize);
		}
		return aBytes;
	}

	/**
	 * @return
	 */
	public int getSize() {
		return pStackPointer;
	}

	/**
	 * @return
	 */
	public byte[] toArray() {
		return toArray(0, getSize());
	}

	/**
	 * @param aPos
	 * @return
	 */
	public byte[] toArray(int aPos) {
		return toArray(aPos, getSize() - aPos);
	}

	/**
	 * @param aPos
	 * @param aSize
	 * @return
	 */
	public byte[] toArray(int aPos, int aSize) {
		int wSize = aSize < 0 ? 0 : aSize >= getSize() ? getSize() : aSize;
		if (wSize > 0) {
			byte[] wTmp = new byte[wSize];
			System.arraycopy(pArray, aPos, wTmp, 0, wTmp.length);
			return wTmp;
		} else {
			return new byte[0];
		}
	}

	/**
	 * @param aCharSet
	 * @return
	 * @throws Exception
	 */
	public String toString(String aCharSet) throws Exception {
		return new String(toArray(), aCharSet);
	}
}
