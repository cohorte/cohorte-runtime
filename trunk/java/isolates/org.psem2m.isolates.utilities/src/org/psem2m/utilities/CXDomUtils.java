/*******************************************************************************
 * Copyright (c) 2011 www.isandlatech.com (www.isandlatech.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ogattaz (isandlaTech) - initial API and implementation
 *******************************************************************************/
package org.psem2m.utilities;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author isandlaTech - ogattaz
 * 
 */
public class CXDomUtils implements ErrorHandler {

	public static final String ATTR_ID = "id";

	private static final int ERR_TEXT_LEN = 50;

	public static final boolean INDENT = true;

	private static DocumentBuilderFactory pBuilderFactory = DocumentBuilderFactory.newInstance();

	private static TransformerFactory pTransformerFactory = TransformerFactory.newInstance();

	public static final boolean WITH_SUB_TREE = true;

	public static final String YES = "yes";

	/**
	 * @param aElmt
	 * @param aChildName
	 * @return
	 */
	public static Element appendChildElmt(Element aElmt, String aChildName) {

		Element wChild = aElmt.getOwnerDocument().createElement(aChildName);
		aElmt.appendChild(wChild);
		return wChild;
	}

	/**
	 * @param aElmt
	 * @param aChildName
	 * @param aInteger
	 * @return
	 */
	public static Element appendIntChildElmt(Element aElmt, String aChildName, int aInteger) {

		Element wChild = appendChildElmt(aElmt, aChildName);
		appendTextNode(wChild, String.valueOf(aInteger));
		return wChild;
	}

	/**
	 * @param aElmt
	 * @param aChildName
	 * @param aBool
	 * @return
	 */
	public static Element appendOkKoChildElmt(Element aElmt, String aChildName, boolean aBool) {

		Element wChild = appendChildElmt(aElmt, aChildName);
		appendTextNode(wChild, CXStringUtils.boolToOkKo(aBool));
		return wChild;
	}

	/**
	 * @param aElmt
	 * @param aChildName
	 * @param aBool
	 * @return
	 */
	public static Element appendOnOffChildElmt(Element aElmt, String aChildName, boolean aBool) {

		Element wChild = appendChildElmt(aElmt, aChildName);
		appendTextNode(wChild, CXStringUtils.boolToOnOff(aBool));
		return wChild;
	}

	/**
	 * @param aElmt
	 * @param aChildName
	 * @param aText
	 * @return
	 */
	public static Element appendTextChildElmt(Element aElmt, String aChildName, String aText) {

		Element wChild = appendChildElmt(aElmt, aChildName);
		if (aText == null || aText.length() != 0) {
			appendTextNode(wChild, aText);
		}
		return wChild;
	}

	/**
	 * @param aElmt
	 * @param aText
	 * @return
	 */
	public static Node appendTextNode(Element aElmt, String aText) {

		aText = (aText != null) ? aText : CXStringUtils.EMPTY;

		Node wNode = aElmt.getOwnerDocument().createTextNode(aText);
		aElmt.appendChild(wNode);
		return wNode;
	}

	/**
	 * @param aElmt
	 * @param aChildName
	 * @param aBool
	 * @return
	 */
	public static Element appendTrueFalseChildElmt(Element aElmt, String aChildName, boolean aBool) {

		Element wChild = appendChildElmt(aElmt, aChildName);
		appendTextNode(wChild, CXStringUtils.boolToTrueFalse(aBool));
		return wChild;
	}

	/**
	 * @param aElmt
	 * @param aChildName
	 * @param aBool
	 * @return
	 */
	public static Element appendYesNoChildElmt(Element aElmt, String aChildName, boolean aBool) {

		Element wChild = appendChildElmt(aElmt, aChildName);
		appendTextNode(wChild, CXStringUtils.boolToYesNo(aBool));
		return wChild;
	}

	/**
	 * Append les fils du noeud "aRootSource" comme fils de "aRootDest"
	 * 
	 * @param aRootSource
	 *            Source des noeuds à importer - Root est incluse dans l'import
	 * @param aRootDest
	 *            - Root sous laquelle seront rattachés les fils de aRootSource
	 */
	public static void attachChildElements(Element aRootSource, Element aRootDest) {

		if (aRootSource != null && aRootDest != null) {
			if (aRootSource.getOwnerDocument() == aRootDest.getOwnerDocument()) {
				Element wChild = CXDomUtils.getFirstChildElmt(aRootSource);
				Element wCNextChild;
				while (wChild != null) {
					wCNextChild = CXDomUtils.getFirstSiblingElmt(wChild);
					aRootDest.appendChild(wChild);
					wChild = wCNextChild;
				}
			} else {
				Node wImported = aRootDest.getOwnerDocument().importNode(aRootSource, true);
				Element wChild = CXDomUtils.getFirstChildElmt(wImported);
				Element wCNextChild;
				while (wChild != null) {
					wCNextChild = CXDomUtils.getFirstSiblingElmt(wChild);
					aRootDest.appendChild(wChild);
					wChild = wCNextChild;
				}
			}
		}
	}

	/**
	 * Append le noeud "aRootSource" comme fils de "aRootDest"
	 * 
	 * @param aRootSource
	 *            Source des noeuds e importer - Root est incluse dans l'import
	 * @param aRootDest
	 *            - Root sous laquelle sera rattachee aRootSource
	 */
	public static void attachElement(Element aRootSource, Element aRootDest) {

		if (aRootSource != null && aRootDest != null) {
			if (aRootSource.getOwnerDocument() == aRootDest.getOwnerDocument()) {
				aRootDest.appendChild(aRootSource);
			} else {
				aRootDest.appendChild(aRootDest.getOwnerDocument().importNode(aRootSource, true));
			}
		}
	}

	/**
	 * Renvoie la liste des noeuds Elements fils de aRoot la forme d'une
	 * properties Key=NodeName - Value=TextNode
	 * 
	 * @param aRoot
	 * @return
	 */
	public static Properties childTextToProperties(Element aRoot) {

		Properties wRes = new Properties();
		Element wElmt = getFirstChildElmt(aRoot);
		while (wElmt != null) {
			wRes.put(wElmt.getNodeName(), getTextNode(wElmt));
			wElmt = getFirstSiblingElmt(wElmt);
		}
		return wRes;
	}

	/**
	 * 
	 * Supprime l'indentation d'un flot xml en supprime les tous les noeuds de
	 * type texte fils d'un élément si celui-ci à au moins un élément fils.
	 * 
	 * Traite aussi les frères de l'éléments passé en paramètre.
	 * 
	 * Nettoie tout un document en passant le root élement.
	 * 
	 * <pre>
	 * CXDomUtils.cleanAllDescElements(wDocument.getDocumentElement());
	 * </pre>
	 * 
	 * @param wElement
	 * @throws Exception
	 */
	public static void cleanAllDescElements(Element wElement) throws Exception {

		Element wChild = CXDomUtils.getFirstChildElmt(wElement);
		if (wChild != null) {
			CXDomUtils.removeAllTextNodes(wElement);
			cleanAllDescElements(wChild);
		}
		Element wSibling = CXDomUtils.getFirstSiblingElmt(wElement);
		if (wSibling != null) {
			cleanAllDescElements(wSibling);
		}
	}

	/**
	 * @param aDestDom
	 * @param aElmt
	 * @param aCloneSubTree
	 * @return
	 */
	public static Element cloneElement(CXDomUtils aDestDom, Element aElmt, boolean aCloneSubTree) {

		return cloneElement(aDestDom.getDom(), aElmt, aCloneSubTree);
	}

	/**
	 * Duplique l'element aElmt
	 * 
	 * @param aDestDom
	 *            Dom dans lequel est cree le clone de aElmt
	 * @param aElmt
	 * @param aCloneSubTree
	 *            =true --> duplique la sous-arborescence
	 * @return
	 */
	public static Element cloneElement(Document aDestDom, Element aElmt, boolean aCloneSubTree) {

		// 16w_000 - Fiche 45147 - Corrections des utilitaires SHARED
		if (aDestDom != null && aElmt != null) {
			return (Element) aDestDom.importNode(aElmt, aCloneSubTree);
		} else {
			return null;
		}
	}

	/**
	 * @param aElmt
	 * @param aCloneSubTree
	 * @return
	 */
	public static Element cloneElement(Element aElmt, boolean aCloneSubTree) {

		if (aElmt != null) {
			return (Element) aElmt.getOwnerDocument().importNode(aElmt, aCloneSubTree);
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @param aNode
	 * @return
	 */
	public static Node findTextNode(Node aNode) {

		if (aNode == null) {
			return null;
		}
		Node wNode = aNode.getFirstChild();
		while ((wNode != null) && (wNode.getNodeType() != Node.TEXT_NODE)) {
			wNode = wNode.getNextSibling();
		}

		return wNode;
	}

	/**
	 * Renvoie la valeur de l'attribut aAttrib de aElmt sous la forme d'un
	 * Integer Renvoie aDefValue si erreur
	 */
	public static int getAttribInt(Element aElmt, String aAttrib, int aDefValue) {

		if (aElmt == null) {
			return aDefValue;
		} else {
			return CXStringUtils.strToInt(aElmt.getAttribute(aAttrib), aDefValue);
		}
	}

	/**
	 * @param aNode
	 * @param aTagName
	 * @return
	 */
	public static List<Element> getElementsByTagName(Element aElement, String aTagName) {

		if (aElement == null) {
			return null;
		}
		List<Element> wElmts = new ArrayList<Element>();
		NodeList wList = aElement.getElementsByTagName(aTagName);
		for (int wI = 0; wI < wList.getLength(); wI++) {
			if (wList.item(wI).getNodeType() == Node.ELEMENT_NODE) {
				wElmts.add((Element) wList.item(wI));
			}
		}
		return wElmts;
	}

	/**
	 * @param aNode
	 * @return
	 */
	public static Element getFirstChildElmt(Node aNode) {

		if (aNode == null) {
			return null;
		}
		Node wNode = aNode.getFirstChild();
		if (wNode == null || wNode.getNodeType() == Node.ELEMENT_NODE) {
			return (Element) wNode;
		} else {
			return getFirstSiblingElmt(wNode);
		}
	}

	/**
	 * @param aNode
	 * @param aTagName
	 * @return
	 */
	public static Element getFirstChildElmtByTag(Node aNode, String aTagName) {

		if (aNode == null) {
			return null;
		}
		Node wNode = aNode.getFirstChild();
		if (wNode == null
				|| (wNode.getNodeType() == Node.ELEMENT_NODE && wNode.getNodeName()
						.equals(aTagName))) {
			return (Element) wNode;
		} else {
			return getFirstSiblingElmtByTag(wNode, aTagName);
		}
	}

	/**
	 * @param aNode
	 * @param aTagName
	 * @param aAttrId
	 * @param aAttrValue
	 * @return
	 */
	public static Element getFirstChildElmtByTagAndAttribut(Node aNode, String aTagName,
			String aAttrId, String aAttrValue) {

		if (aNode == null || aTagName == null || aAttrId == null || aAttrValue == null) {
			return null;
		}
		Node wNode = aNode.getFirstChild();
		if (wNode == null
				|| (wNode.getNodeType() == Node.ELEMENT_NODE
						&& wNode.getNodeName().equals(aTagName) && aAttrValue
							.equals(((Element) wNode).getAttribute(aAttrId)))) {
			return (Element) wNode;
		} else {
			return getFirstSiblingElmtByTagAndAttribut(wNode, aTagName, aAttrId, aAttrValue);
		}
	}

	/**
	 * retourne le premier noeud fils verifaint le tagname et le contenu de
	 * l'attribut "id"
	 * 
	 * @param aNode
	 * @param aTagName
	 * @param aId
	 * @return
	 */
	public static Element getFirstChildElmtByTagAndId(Node aNode, String aTagName, String aIdValue) {

		return getFirstChildElmtByTagAndAttribut(aNode, aTagName, ATTR_ID, aIdValue);
	}

	/**
	 * @param aNode
	 * @return
	 */
	public static Node getFirstChildText(Node aNode) {

		if (aNode == null) {
			return null;
		}
		Node wNode = aNode.getFirstChild();
		if (wNode == null || wNode.getNodeType() == Node.TEXT_NODE) {
			return wNode;
		} else {
			return getFirstSiblingElmt(wNode);
		}
	}

	/**
	 * 
	 * Renvoie le 1er descendant de aNode dont le TagName=aTagName Algorithme
	 * complexe -- A voir
	 * 
	 * @param aNode
	 * @param aTagName
	 * @return
	 */
	public static Element getFirstDescElmtByTag(Element aElement, String aTagName) {

		Element wRes = null;
		NodeList wList = aElement.getElementsByTagName(aTagName);
		for (int wI = 0; wI < wList.getLength() && wRes == null; wI++) {
			if (wList.item(wI).getNodeType() == Node.ELEMENT_NODE) {
				wRes = (Element) wList.item(wI);
			}
		}
		return wRes;
	}

	/**
	 * 
	 * @param aNode
	 * @param aTagName
	 * @param aAttName
	 * @param aValue
	 * @return
	 */
	public static Element getFirstDescElmtByTagAndAttribut(Element aElement, String aTagName,
			String aAttName, String aValue) {

		Element wRes = null;
		NodeList wList = aElement.getElementsByTagName(aTagName);
		for (int wI = 0; wI < wList.getLength() && wRes == null; wI++) {
			if (wList.item(wI).getNodeType() == Node.ELEMENT_NODE
					&& ((Element) wList.item(wI)).getAttribute(aAttName).equals(aValue)) {
				wRes = (Element) wList.item(wI);
			}
		}
		return wRes;
	}

	/**
	 * @param aNode
	 * @return
	 */
	public static Element getFirstParentElmt(Node aNode) {

		if (aNode == null) {
			return null;
		}
		Node wNode = aNode.getParentNode();
		while (wNode != null && wNode.getNodeType() != Node.ELEMENT_NODE) {
			wNode = wNode.getParentNode();
		}
		return (Element) wNode;
	}

	/**
	 * @param aNode
	 * @param aTagName
	 * @return
	 */
	public static Element getFirstParentElmtByTag(Node aNode, String aTagName) {

		if (aNode == null) {
			return null;
		}
		Node wNode = aNode.getParentNode();
		while (wNode != null
				&& (wNode.getNodeType() != Node.ELEMENT_NODE || !wNode.getNodeName().equals(
						aTagName))) {
			wNode = wNode.getParentNode();
		}
		return (Element) wNode;
	}

	/**
	 * @param aNode
	 * @return
	 */
	public static Element getFirstSiblingElmt(Node aNode) {

		if (aNode == null) {
			return null;
		}
		Node wNode = aNode.getNextSibling();
		while (wNode != null && wNode.getNodeType() != Node.ELEMENT_NODE) {
			wNode = wNode.getNextSibling();
		}
		return (Element) wNode;
	}

	/**
	 * @param aNode
	 * @param aTagName
	 * @return
	 */
	public static Element getFirstSiblingElmtByTag(Node aNode, String aTagName) {

		if (aNode == null) {
			return null;
		}
		Node wNode = aNode.getNextSibling();
		while (wNode != null
				&& (wNode.getNodeType() != Node.ELEMENT_NODE || !wNode.getNodeName().equals(
						aTagName))) {
			wNode = wNode.getNextSibling();
		}
		return (Element) wNode;
	}

	/**
	 * @param aNode
	 * @param aTagName
	 * @param aAttrId
	 * @param aAttrValue
	 * @return
	 */
	public static Element getFirstSiblingElmtByTagAndAttribut(Node aNode, String aTagName,
			String aAttrId, String aAttrValue) {

		if (aNode == null || aTagName == null || aAttrId == null || aAttrValue == null) {
			return null;
		}
		Node wNode = aNode.getNextSibling();
		while (wNode != null
				&& (wNode.getNodeType() != Node.ELEMENT_NODE
						|| !wNode.getNodeName().equals(aTagName) || !aAttrValue
							.equals(((Element) wNode).getAttribute(aAttrId)))) {
			wNode = wNode.getNextSibling();
		}
		return (Element) wNode;
	}

	/**
	 * @param aNode
	 * @param aTagName
	 * @param aAttrIdValue
	 * @return
	 */
	public static Element getFirstSiblingElmtByTagAndId(Node aNode, String aTagName,
			String aAttrIdValue) {

		return getFirstSiblingElmtByTagAndAttribut(aNode, aTagName, ATTR_ID, aAttrIdValue);
	}

	/**
	 * @param aNode
	 * @param aTagName
	 * @return
	 */
	public static int getNbChildElmtByTag(Node aNode, String aTagName) {

		int wNbElmt = 0;
		if (aNode != null) {
			Node wNode = getFirstChildElmtByTag(aNode, aTagName);

			while (wNode != null) {
				wNbElmt++;
				wNode = getFirstSiblingElmtByTag(wNode, aTagName);
			}
		}
		return wNbElmt;
	}

	/**
	 * 
	 * @param aNode
	 * @return
	 */
	public static String getTextNode(Node aNode) {

		if (aNode == null) {
			return CXStringUtils.EMPTY;
		}

		Node wNode = findTextNode(aNode);

		if (wNode != null) {
			return wNode.getNodeValue();
		} else {
			return CXStringUtils.EMPTY;
		}
	}

	/**
	 * Renvoie la valeur du noeud texte ('yes' ou 'on') de aElmt sous la forme
	 * d'un Boolean Renvoie null si erreur
	 */
	public static Boolean getValueBool(Element aElmt) {

		try {
			return new Boolean(CXStringUtils.strToBoolean(getTextNode(aElmt)));
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Renvoie la valeur du noeud texte de aElmt sous la forme d'un Integer
	 * Renvoie null si erreur
	 */
	public static Integer getValueInt(Element aElmt) {

		if (aElmt == null) {
			return null;
		}
		try {
			return new Integer(getTextNode(aElmt));
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Renvoie la valeur du noeud texte de aElmt sous la forme d'un Integer
	 * Renvoie aDefValue si erreur
	 */
	public static int getValueInt(Element aElmt, int aDefValue) {

		if (aElmt == null) {
			return aDefValue;
		} else {
			return CXStringUtils.strToInt(getTextNode(aElmt), aDefValue);
		}
	}

	/**
	 * @param aElmt
	 * @return
	 */
	public static String getValueStr(Element aElmt) {

		if (aElmt == null) {
			return null;
		}
		try {
			return getTextNode(aElmt);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 
	 * @param aNode
	 * @return
	 */
	public static boolean hasTextNode(Node aNode) {

		if (aNode == null) {
			return false;
		}

		return (findTextNode(aNode) != null);
	}

	/**
	 * 
	 * Supprime les toud les noeuds texte d'un élément
	 * 
	 * @param aElmt
	 * @return
	 */
	public static String removeAllTextNodes(Element aElmt) {

		StringBuilder wSB = new StringBuilder();
		Node wTextNode = removeTextNode(aElmt);
		while (wTextNode != null) {
			wSB.append(wTextNode.getTextContent());
			wTextNode = removeTextNode(aElmt);
		}
		return wSB.toString();
	}

	/**
	 * 
	 * @param aNode
	 * @return
	 */
	public static int removeChildren(Node aNode) {

		if (aNode == null) {
			return 0;
		}
		int wNbRemoved = 0;
		Node wNode;
		while (aNode.hasChildNodes()) {
			wNode = aNode.getFirstChild();
			aNode.removeChild(wNode);
		}
		return wNbRemoved;
	}

	/**
	 * @param aElmt
	 * @return
	 */
	public static Node removeTextNode(Element aElmt) {

		Node wNode = getFirstChildText(aElmt);
		if (wNode != null) {
			aElmt.removeChild(wNode);
		}
		return wNode;
	}

	/**
	 * 
	 * @param aNode
	 * @param aValue
	 */
	public static void setTextValue(Node aNode, String aValue) {

		Document wDocument = aNode.getOwnerDocument();
		Text wText = wDocument.createTextNode(aValue);
		aNode.appendChild(wText);
	}

	/**
	 * @param aNode
	 * @return
	 * @throws Exception
	 */
	public static String toXml(Node aNode) throws Exception {

		if (aNode == null) {
			return CXStringUtils.EMPTY;
		}
		Transformer wProcessor = pTransformerFactory.newTransformer();
		StreamResult wResult = new StreamResult(new StringWriter());
		wProcessor.transform(new DOMSource(aNode), wResult);
		return wResult.getWriter().toString();
	}

	private DocumentBuilder pDocBuilder;

	private Document pDom = null;

	private CXmlTextLines pXmlText = null;

	/**
	 * @throws Exception
	 */
	public CXDomUtils() throws Exception {

		super();
		pDom = getDocBuilder().newDocument();
	}

	/**
	 * @param aText
	 * @param aEncoding
	 * @throws Exception
	 */
	public CXDomUtils(byte[] aText, String aEncoding) throws Exception {

		super();
		parse(aText, aEncoding);
	}

	/**
	 * @param aDom
	 * @throws Exception
	 */
	public CXDomUtils(Document aDom) throws Exception {

		super();
		pDom = aDom;
	}

	/**
	 * @param aFile
	 * @throws Exception
	 */
	public CXDomUtils(File aFile) throws Exception {

		super();
		parse(aFile);
	}

	/**
	 * @param aStream
	 * @throws Exception
	 */
	public CXDomUtils(InputStream aStream) throws Exception {

		super();
		parse(aStream);
	}

	/**
	 * @param aString
	 * @throws Exception
	 */
	public CXDomUtils(String aString) throws Exception {

		super();
		parse(aString);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {

		try {
			CXDomUtils wDomUtils = new CXDomUtils();

			Element wElmt = CXDomUtils
					.cloneElement(wDomUtils.pDom, pDom.getDocumentElement(), true);

			wDomUtils.pDom.appendChild(wElmt);

			return wDomUtils;
		} catch (Exception e) {
			throw new CloneNotSupportedException("Can't clone CXDomUtils \n"
					+ CXException.eMiniInString(e));
		}
	}

	/**
	 * @param aNodeName
	 * @return
	 */
	public Element createElement(String aNodeName) {

		return getDom().createElement(aNodeName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
	 */
	@Override
	public void error(SAXParseException aExcep) throws SAXException {

		throw new SAXException("DOM parsing error. " + saxEcepToStr(aExcep));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
	 */
	@Override
	public void fatalError(SAXParseException aExcep) throws SAXException {

		throw new SAXException("DOM parsing fatal error. " + saxEcepToStr(aExcep));
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public DocumentBuilder getDocBuilder() throws Exception {

		if (pDocBuilder == null) {
			pDocBuilder = pBuilderFactory.newDocumentBuilder();
			pDocBuilder.setErrorHandler(this);
		}
		return pDocBuilder;
	}

	/**
	 * @return
	 */
	public Document getDom() {

		return pDom;
	}

	/**
	 * @param aTagName
	 * @return
	 */
	public List<Element> getElementsByTagName(String aTagName) {

		return getElementsByTagName(this.getRootElmt(), aTagName);
	}

	/**
	 * @param aDom
	 * @param aXPath
	 * @return
	 */
	public Node getNodeByTagNameAndOneAtt(String aTagName, String aAttribId, String aAttribValue) {

		String wXPath = "//" + aTagName + "[@" + aAttribId + "=\"" + aAttribValue + "\"]";

		// Set up an identity transformer to use as serializer.
		// This one can write input to output stream

		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			return (Node) xpath.evaluate(wXPath, getDom(), XPathConstants.NODE);
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * @return
	 */
	public Element getRootElmt() {

		return getDom().getDocumentElement();
	}

	/**
	 * @param aText
	 * @param aEncoding
	 * @throws Exception
	 */
	protected void parse(byte[] aText, String aEncoding) throws Exception {

		ByteArrayInputStream wStream = new ByteArrayInputStream(aText);
		parse(wStream);
	}

	/**
	 * @param aFile
	 * @throws Exception
	 */
	protected void parse(File aFile) throws Exception {

		try {
			pDom = getDocBuilder().parse(aFile);
		} catch (Exception e) {
			throw (e);
		}
	}

	/**
	 * @param aInput
	 * @throws Exception
	 */
	protected void parse(InputSource aInput) throws Exception {

		try {
			pDom = getDocBuilder().parse(aInput);
		} catch (Exception e) {
			throw (e);
		}
	}

	/**
	 * @param aStream
	 * @throws Exception
	 */
	protected void parse(InputStream aStream) throws Exception {

		try {
			pDom = getDocBuilder().parse(aStream);
		} catch (Exception e) {
			throw (e);
		}
	}

	/**
	 * @param aString
	 * @throws Exception
	 */
	protected void parse(String aString) throws Exception {

		if (aString != null && aString.length() > 0) {
			pXmlText = new CXmlTextLines(aString);
			parse(new InputSource(new StringReader(aString)));
		} else {
			pDom = getDocBuilder().newDocument();
		}
	}

	/**
	 * @param aExcep
	 * @return
	 */
	protected String saxEcepToStr(SAXParseException aExcep) {

		StringBuilder wMsg = new StringBuilder(aExcep.getMessage());
		if (aExcep.getLineNumber() != -1) {
			wMsg.append(" Line[").append(aExcep.getLineNumber()).append("]");
		}
		if (aExcep.getColumnNumber() != -1) {
			wMsg.append(" Column[").append(aExcep.getColumnNumber()).append("]");
		}
		if (aExcep.getSystemId() != null) {
			wMsg.append(" SystemId[").append(aExcep.getSystemId()).append("]");
		}
		if (aExcep.getPublicId() != null) {
			wMsg.append(" PublicId[").append(aExcep.getPublicId()).append("]");
		}
		if (pXmlText != null) {
			String wText = pXmlText.getXmlTag(aExcep, ERR_TEXT_LEN);
			if (wText != null && wText.length() != 0) {
				wMsg.append(" Xml[").append(wText).append("]");
			}
		}
		return wMsg.toString();
	}

	/**
	 * 
	 * @param aRootElement
	 * @return
	 */
	public Element setRootElmt(Element aRootElement) {

		if (getRootElmt() == null) {
			getDom().appendChild(aRootElement);
			return getRootElmt();
		} else {
			return null;
		}
	}

	/**
	 * @param aNodeName
	 * @return
	 */
	public Element setRootElmt(String aNodeName) {

		return setRootElmt(createElement(aNodeName));
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public String toXml() throws Exception {

		return toXml(getRootElmt());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
	 */
	@Override
	public void warning(SAXParseException aExcep) throws SAXException {

	}
}

/**
 * @author ogattaz
 * 
 */
class CXmlTextLines {

	private String pTextLines[] = null;
	private String pXmlText = null;

	/**
	 * @param aXmlText
	 */
	public CXmlTextLines(String aXmlText) {

		pXmlText = aXmlText;
	}

	/**
	 * @return
	 */
	private int getNbLines() {

		return getTextLines().length;
	}

	/**
	 * @return
	 */
	private String[] getTextLines() {

		if (pTextLines == null) {
			pTextLines = tokenize();
		}
		return pTextLines;
	}

	// Renvoie les aMaxLen caracteres autour de l'erreur
	public String getXmlTag(SAXParseException aExcep, int aMaxLen) {

		String wResult = null;
		try {
			int wLineIdx = aExcep.getLineNumber() - 1;
			int wColIdx = aExcep.getColumnNumber() - 1;
			if (wColIdx >= 0 && wLineIdx >= 0 && getNbLines() > wLineIdx) {
				String wLineStr = getTextLines()[wLineIdx];
				if (wLineStr != null) {
					int wLen = wLineStr.length();
					if (wLen != 0) {
						if (wLen <= aMaxLen) {
							StringBuilder wTmp = new StringBuilder();
							for (int i = 0; i < wLen; i++) {
								char wChar = wLineStr.charAt(i);
								if (!Character.isISOControl(wChar)) {
									wTmp.append(wLineStr.charAt(i));
								}
							}
							wResult = wTmp.toString();
						} else {
							int wMaxLen = aMaxLen;
							int wNbLoops = 0;
							boolean wOk = false;
							while (!wOk) {
								int wMinIdx = wColIdx - (wMaxLen / 2);
								int wMaxIdx = wColIdx + (wMaxLen / 2);
								if (wMinIdx < 0) {
									wMaxIdx = wColIdx - wMinIdx + (wMaxLen / 2);
									wMinIdx = 0;
								}
								wMaxIdx = (wMaxIdx >= wLen) ? wLen - 1 : wMaxIdx;
								for (int i = wMinIdx; i <= wMaxIdx; i++) {
									char wChar = wLineStr.charAt(i);
									if (Character.isISOControl(wChar)) {
										wMaxLen++;
									}
								}
								wNbLoops++;
								if (wMaxLen == aMaxLen || wNbLoops >= 2) {
									wOk = true;
									StringBuilder wTmp = new StringBuilder();
									for (int i = wMinIdx; i <= wMaxIdx; i++) {
										char wChar = wLineStr.charAt(i);
										if (!Character.isISOControl(wChar)) {
											wTmp.append(wChar);
										}
									}

									wResult = wTmp.toString();// .substring(wMinIdx,wMaxIdx);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			return null;
		}
		return wResult;

	}

	/**
	 * @return
	 */
	private String[] tokenize() {

		if (pXmlText != null && pXmlText.length() != 0) {
			StringTokenizer wTok = new StringTokenizer(pXmlText, "\r\n");
			pTextLines = new String[wTok.countTokens()];
			int wI = 0;
			while (wTok.hasMoreTokens()) {
				pTextLines[wI] = wTok.nextToken();
				wI++;
			}
		} else {
			pTextLines = new String[0];
		}
		return pTextLines;
	}
}
