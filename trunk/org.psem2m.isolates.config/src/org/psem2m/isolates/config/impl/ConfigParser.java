/**
 * File:   ConfigParser.java
 * Author: Thomas Calmant
 * Date:   21 juin 2011
 */
package org.psem2m.isolates.config.impl;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * XML Configuration parser
 * 
 * @author Thomas Calmant
 */
public class ConfigParser {

    public Document parseFile(final File aConfFile)
	    throws ParserConfigurationException, SAXException, IOException {

	// Get a parser
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder = factory.newDocumentBuilder();

	// Parse the doc
	Document document = builder.parse(aConfFile);

	return document;
    }
}
