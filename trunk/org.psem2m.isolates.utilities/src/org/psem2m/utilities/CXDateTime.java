package org.psem2m.utilities;

import java.util.Calendar;
import java.util.Date;

/**
 * @author ogattaz
 * 
 */
public class CXDateTime {
	private final static String DATE_SEP = "/";
	private final static String TIME_SEP = ":";

	/**
	 * @param aTime
	 * @return
	 */
	public static String time2DateHour(long aTime) {
		return time2StrAAAAMMJJHHMMSS(aTime, TIME_SEP, DATE_SEP);
	}

	/**
	 * @return
	 */
	public static String time2StrAAAAMMJJ() {
		return time2StrAAAAMMJJ(System.currentTimeMillis());
	}

	/**
	 * @param aSep
	 * @return
	 */
	public static String time2StrAAAAMMJJ(char aSep) {
		return time2StrAAAAMMJJ(System.currentTimeMillis(), aSep);
	}

	/**
	 * @param aTime
	 * @return
	 */
	public static String time2StrAAAAMMJJ(long aTime) {
		return time2StrAAAAMMJJ(aTime, null);
	}

	/**
	 * @param aTime
	 * @param aSep
	 * @return
	 */
	public static String time2StrAAAAMMJJ(long aTime, char aSep) {
		return time2StrAAAAMMJJ(aTime, String.valueOf(aSep));
	}

	/**
	 * @param aTime
	 * @param aSep
	 * @return
	 */
	public static String time2StrAAAAMMJJ(long aTime, String aSep) {
		return time2StrDate(aTime, aSep, true, true);
	}

	/**
	 * @return
	 */
	public static String time2StrAAAAMMJJHHMMSS() {
		return time2StrAAAAMMJJHHMMSS(System.currentTimeMillis());
	}

	/**
	 * @param aTime
	 * @return
	 */
	public static String time2StrAAAAMMJJHHMMSS(long aTime) {
		return time2StrAAAAMMJJHHMMSS(aTime, null);
	}

	/**
	 * @param aTime
	 * @param aSep
	 * @return
	 */
	public static String time2StrAAAAMMJJHHMMSS(long aTime, char aSep) {
		return time2StrAAAAMMJJHHMMSS(aTime, String.valueOf(aSep));
	}

	/**
	 * @param aTime
	 * @param aSep
	 * @return
	 */
	public static String time2StrAAAAMMJJHHMMSS(long aTime, String aSep) {
		if (aSep != null)
			return time2StrAAAAMMJJ(aTime, aSep) + aSep
					+ time2StrHHMMSS(aTime, aSep);
		else
			return time2StrAAAAMMJJ(aTime, aSep) + time2StrHHMMSS(aTime, aSep);
	}

	/**
	 * @param aTime
	 * @param aSepTime
	 * @param aSepDate
	 * @return
	 */
	public static String time2StrAAAAMMJJHHMMSS(long aTime, String aSepTime,
			String aSepDate) {
		return time2StrAAAAMMJJ(aTime, (aSepDate == null ? "" : aSepDate))
				+ " "
				+ time2StrHHMMSS(aTime, (aSepTime == null ? "" : aSepTime));
	}

	/**
	 * @return
	 */
	public static String time2StrAAMMJJ() {
		return time2StrAAMMJJ(System.currentTimeMillis());
	}

	/**
	 * @param aSep
	 * @return
	 */
	public static String time2StrAAMMJJ(char aSep) {
		return time2StrAAMMJJ(System.currentTimeMillis(), aSep);
	}

	/**
	 * @param aTime
	 * @return
	 */
	public static String time2StrAAMMJJ(long aTime) {
		return time2StrAAMMJJ(aTime, null);
	}

	/**
	 * @param aTime
	 * @param aSep
	 * @return
	 */
	public static String time2StrAAMMJJ(long aTime, char aSep) {
		return time2StrAAMMJJ(aTime, String.valueOf(aSep));
	}

	// DATE TO STRING JJMMAAAA

	/**
	 * @param aTime
	 * @param aSep
	 * @return
	 */
	public static String time2StrAAMMJJ(long aTime, String aSep) {
		return time2StrDate(aTime, aSep, true, false);
	}

	/**
	 * DATE TO STRING
	 * 
	 * aUs --> AAAAMMJJ else JJMMMAAAA
	 * 
	 * aCentury --> AAAA else AA
	 * 
	 * @param aTime
	 * @param aSep
	 * @param aUs
	 * @param aCentury
	 * @return
	 */
	public static String time2StrDate(long aTime, String aSep, boolean aUs,
			boolean aCentury) {
		Calendar wRightNow = Calendar.getInstance();
		wRightNow.setTime(new Date(aTime < 0 ? 0 : aTime));
		String wYear = CXStringUtils.strAdjustRight(
				wRightNow.get(Calendar.YEAR), aCentury ? 4 : 2);
		String wMonth = CXStringUtils.strAdjustRight(
				wRightNow.get(Calendar.MONTH) + 1, 2); // mois + 1 car de 0 a 11
		String wDay = CXStringUtils.strAdjustRight(
				wRightNow.get(Calendar.DAY_OF_MONTH), 2);
		String wSep = aSep == null ? "" : aSep;
		if (aUs)
			return wYear + wSep + wMonth + wSep + wDay;
		else
			return wDay + wSep + wMonth + wSep + wYear;
	}

	/**
	 * @param aTime
	 * @param aSep
	 * @param aWithMillisecs
	 * @return
	 */
	private static String time2StrHHFull(long aTime, String aSep,
			boolean aWithMillisecs) {

		StringBuilder wResult = new StringBuilder();
		if (aTime > 0) {
			Calendar wRightNow = Calendar.getInstance();
			wRightNow.setTime(new Date(aTime));
			wResult.append(CXStringUtils.strAdjustRight(
					wRightNow.get(Calendar.HOUR_OF_DAY), 2));
			if (aSep != null)
				wResult.append(aSep);
			wResult.append(CXStringUtils.strAdjustRight(
					wRightNow.get(Calendar.MINUTE), 2));
			if (aSep != null)
				wResult.append(aSep);
			wResult.append(CXStringUtils.strAdjustRight(
					wRightNow.get(Calendar.SECOND), 2));
			if (aSep != null)
				wResult.append(aSep);
			wResult.append(CXStringUtils.strAdjustRight(
					wRightNow.get(Calendar.MILLISECOND), 3));
		}
		return wResult.toString();
	}

	/**
	 * @return
	 */
	public static String time2StrHHMMSS() {
		return time2StrHHMMSS(System.currentTimeMillis());
	}

	/**
	 * @param aSep
	 * @return
	 */
	public static String time2StrHHMMSS(char aSep) {
		return time2StrHHMMSS(System.currentTimeMillis(), aSep);
	}

	/**
	 * @param aTime
	 * @return
	 */
	public static String time2StrHHMMSS(long aTime) {
		return time2StrHHMMSS(aTime, null);
	}

	/**
	 * @param aTime
	 * @param aSep
	 * @return
	 */
	public static String time2StrHHMMSS(long aTime, char aSep) {
		return time2StrHHMMSS(aTime, String.valueOf(aSep));
	}

	/**
	 * @param aTime
	 * @param aSep
	 * @return
	 */
	public static String time2StrHHMMSS(long aTime, String aSep) {
		return time2StrHHFull(aTime, aSep, false);
	}

	/**
	 * @param aTime
	 * @param aSep
	 * @return
	 */
	public static String time2StrHHMMSSmmm(long aTime, String aSep) {
		return time2StrHHFull(aTime, aSep, true);
	}

	/**
	 * @return
	 */
	public static String time2StrJJMMAA() {
		return time2StrJJMMAA(System.currentTimeMillis());
	}

	/**
	 * @param aSep
	 * @return
	 */
	public static String time2StrJJMMAA(char aSep) {
		return time2StrJJMMAA(System.currentTimeMillis(), aSep);
	}

	/**
	 * @param aTime
	 * @return
	 */
	public static String time2StrJJMMAA(long aTime) {
		return time2StrJJMMAA(aTime, null);
	}

	/**
	 * @param aTime
	 * @param aSep
	 * @return
	 */
	public static String time2StrJJMMAA(long aTime, char aSep) {
		return time2StrJJMMAA(aTime, String.valueOf(aSep));
	}

	/**
	 * @param aTime
	 * @param aSep
	 * @return
	 */
	public static String time2StrJJMMAA(long aTime, String aSep) {
		return time2StrDate(aTime, aSep, false, false);
	}

	/**
	 * @return
	 */
	public static String time2StrJJMMAAAA() {
		return time2StrJJMMAAAA(System.currentTimeMillis());
	}

	/**
	 * @param aSep
	 * @return
	 */
	public static String time2StrJJMMAAAA(char aSep) {
		return time2StrJJMMAAAA(System.currentTimeMillis(), aSep);
	}

	/**
	 * @param aTime
	 * @return
	 */
	public static String time2StrJJMMAAAA(long aTime) {
		return time2StrJJMMAAAA(aTime, null);
	}

	/**
	 * @param aTime
	 * @param aSep
	 * @return
	 */
	public static String time2StrJJMMAAAA(long aTime, char aSep) {
		return time2StrJJMMAAAA(aTime, String.valueOf(aSep));
	}

	/**
	 * @param aTime
	 * @param aSep
	 * @return
	 */
	public static String time2StrJJMMAAAA(long aTime, String aSep) {
		return time2StrDate(aTime, aSep, false, true);
	}
}
