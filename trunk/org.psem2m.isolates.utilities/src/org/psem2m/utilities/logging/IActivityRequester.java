package org.psem2m.utilities.logging;


import org.psem2m.utilities.IXDescriber;


public interface IActivityRequester extends IXDescriber
{

  /**
   * 
   */
  public void close();
  
  /**
   * @param aMax
   * @return
   */
  public CActivityRequestReply getLogRecords(int aMax);
  /**
   * @param aMethodName
   * @param aMax
   * @return
   */
  public CActivityRequestReply getLogMethodRecords(String aMethodName,int aMax);
  
  /**
   * @param aLevelName
   * @param aMax
   * @return
   */
  public CActivityRequestReply getLogLevelRecords(String aLevelName,int aMax);
  
  /**
   * @param aActivityRequestFilter
   * @return
   */
  public CActivityRequestReply getLogRecords(CActivityRequestFilter aActivityRequestFilter);
}
