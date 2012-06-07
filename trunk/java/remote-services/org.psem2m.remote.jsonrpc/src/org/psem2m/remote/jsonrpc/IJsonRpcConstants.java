/**
 * File:   IJsonRpcConstants.java
 * Author: Thomas Calmant
 * Date:   19 déc. 2011
 */
package org.psem2m.remote.jsonrpc;

/**
 * @author Thomas Calmant
 * 
 */
public interface IJsonRpcConstants {

    /** Default Jabsorb servlet name */
    String DEFAULT_SERVLET_NAME = "/JSON-RPC";

    /** The exported configuration : json-rpc */
    String[] EXPORT_CONFIGS = { "jsonrpc", "json-rpc", "*" };

    /** HTTP protocol name */
    String EXPORT_PROTOCOL = "http";
}
