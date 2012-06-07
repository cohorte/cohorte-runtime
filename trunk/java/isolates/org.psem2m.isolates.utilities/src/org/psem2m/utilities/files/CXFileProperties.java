/*
 * Created on 28 oct. 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.psem2m.utilities.files;

import org.psem2m.utilities.CXStringUtils;

/**
 * Classe de gestion de fichiers de type "properties"
 * 
 * Prend en compte l'encodage ISO_8859_1 et la gestion des caract�res 
 * non encodables (> 255 ) sous forme de s�quence escape : "\ u 0 0 0 0"
 * 
 * !! MonoThread
 * 
 * @author Adonix Grenoble
 * @version 140_003
 */
public class CXFileProperties extends CXFileText
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3258688827626239288L;
	/**
	 * @param aText
	 * @return
	 */
	private static boolean containsUnicodeChar(String aText)
	{
		return (countUnicodeChar(aText)>0);
	}
	/**
	 * @param aText
	 * @return
	 */
	private static boolean containsUnicodeEscapes(String aText)
	{
		return (countUnicodeEscapes(aText)>0);
	}
	/**
	 * @param aText
	 * @return
	 */
	private static int countUnicodeChar(String aText)
	{
		int wNbUnicodeChar = 0;
		int wMax = aText.length();
		int wI = 0;
		while(wI<wMax)
		{
			if ( aText.charAt(wI) > 255 )
			{
				wNbUnicodeChar++;
			}
			wI++;
		}
		return wNbUnicodeChar;
	}
	/**
	 * @param aText
	 * @return
	 */
	private static int countUnicodeEscapes(String aText)
	{
		int wNbUnicodeEscapes = 0;
		int wMax = aText.length();
		int wI = 0;
		while(wI<wMax)
		{
			/*
			 * pr�sence d'une s�quence du type "\ u 0 0 0 0"
			 */
			if ( aText.charAt(wI) == '\\' && (wI+5<wMax) &&  aText.charAt(wI+1) == 'u' && nextFourCharAreDigits(aText,wI+1))
			{
				wNbUnicodeEscapes++;
				wI += 5;
			}
			wI++;
		}
		return wNbUnicodeEscapes;
	}
	/**
	 * retourne vrai si les 4 caract�res situ�s apr�s l'offset sont des chiffres
	 * @return
	 */
	private static boolean nextFourCharAreDigits(String aText,int aOffset)
	{
		int wI = aOffset+1;
		int wMax = wI + 4;
		boolean wAreDigits = (wMax < aText.length() );
		
		while(wI<wMax && wAreDigits)
		{
			wAreDigits = (wAreDigits & Character.isDigit(aText.charAt(wI)));
			wI++;
		}
		return wAreDigits;
	}

	/**
	 * @param aFile
	 */
	public CXFileProperties(CXFile aFile)
	{
		super(aFile);
		myInit();
	}
	/**
	 * @param aParentDir
	 * @param aFileName
	 */
	public CXFileProperties(CXFileDir aParentDir, String aFileName)
	{
		super(aParentDir, aFileName);
		myInit();
	}
	 /**
	 * @param aFullPath
	 */
	public CXFileProperties(String aFullPath)
	{
		super(aFullPath);
		myInit();
	}
	/**
	 * @param aParentDir
	 * @param aFileName
	 */
	public CXFileProperties(String aParentDir, String aFileName)
	{
		super(aParentDir, aFileName);
		myInit();
	}
	/**
	 * @param aText
	 * @return
	 */
	private  String convertNonIso8859ToUnicodeEscapes(String aText)
	{
		if (!containsUnicodeChar(aText))
		{
			return aText;
		}
		
		StringBuilder wSB = new StringBuilder();
		char wChar;
		int wMax = aText.length();
		int wI = 0;
		while(wI<wMax)
		{
			wChar = aText.charAt(wI);
			if ( wChar > 255 )
			{
				wSB.append( CXStringUtils.UNICODE_PREFIX);
				wSB.append( CXStringUtils.strAdjustRight(wChar,4) );
			}
			else
			{
				wSB.append(wChar);
			}
			wI++;
		}

		return wSB.toString();
	}
	/**
	 * @param aText
	 * @return
	 */
	private String convertUnicodeEscapesToNonIso8859(String aText)
	{
		if (!containsUnicodeEscapes(aText))
		{
			return aText;
		}
		
		StringBuilder wSB = new StringBuilder();
		char wChar;
		int wMax = aText.length();
		int wI = 0;
		while(wI<wMax)
		{
			wChar = aText.charAt(wI);
			/*
			 * pr�sence d'une s�quence du type "\ u 0 0 0 0"
			 */
			if ( wChar == '\\' && (wI+5<wMax) &&  aText.charAt(wI+1) == 'u' && nextFourCharAreDigits(aText,wI+1))
			{

				wSB.append( (char)Integer.parseInt(aText.substring(wI+2,wI+5)) );
				wI += 5;
			}
			else
			{
				wSB.append(wChar);
			}
			wI++;
		}
		
		return aText;
	}
	/**
	 * r�alise l'initialisation sp�cifique de ce type de fichier
	 */
	protected void myInit()
	{
		/*
		 * On force l'encdage par d�faut � ISO-8859-1
		 */
		setDefaultEncoding(ENCODING_ISO_8859_1);
	}
	/* --------------------------------------------------------------------
	 * 
	 * decode les "escape sequence Unicode" pr�sentes dans la chaine lue 
	 * dans le fichier sous-jascent
	 * 
	 * Cf.
	 * When saving properties to a stream or loading them from a stream, 
	 * the ISO 8859-1 character encoding is used. For characters that cannot 
	 * be directly represented in this encoding, Unicode escapes  are used; 
	 * however, only a single 'u' character is allowed in an escape sequence. 
	 * 
	 * (non-Javadoc)
	 * @see com.adonix.adminsrv.utils.CXFileText#readAll()
	 */
	@Override
	public String readAll() throws Exception
	{
		return convertUnicodeEscapesToNonIso8859 ( super.readAll( ) );
	}
	/* --------------------------------------------------------------------
	 * 
	 * decode les "escape sequence Unicode" pr�sentes dans la chaine lue 
	 * dans le fichier sous-jascent
	 * 
	 * Cf.
	 * When saving properties to a stream or loading them from a stream, 
	 * the ISO 8859-1 character encoding is used. For characters that cannot 
	 * be directly represented in this encoding, Unicode escapes  are used; 
	 * however, only a single 'u' character is allowed in an escape sequence. 
	 * 
	 * (non-Javadoc)
	 * @see com.adonix.adminsrv.utils.CXFileText#readLine()
	 */
	@Override
	public String readLine() throws Exception
	{
		return convertUnicodeEscapesToNonIso8859 ( super.readLine( ) );
	}
	/* --------------------------------------------------------------------
	 * 
	 * code en "escape sequence Unicode" les caract�res non encodables (cf. en ISO-8859)
	 * avant d'�crire la chaine dans le fichier sous-jascent
	 * 
	 * Cf.
	 * When saving properties to a stream or loading them from a stream, 
	 * the ISO 8859-1 character encoding is used. For characters that cannot 
	 * be directly represented in this encoding, Unicode escapes  are used; 
	 * however, only a single 'u' character is allowed in an escape sequence. 
	 *
	 * (non-Javadoc)
	 * @see com.adonix.adminsrv.utils.CXFileText#write(java.lang.String)
	 */
	@Override
	public void write(String aString) throws Exception
	{
		/*
		 * �crit la chaine de caract�re dans le fichier text sous jascent (cf ISO-8859-1) 
		 * en convertissant tous les caract�res non repr�sentable en ISO-8859-1
		 * (cf. > 255) en utilisant le codage "Unicode escapes" (cf. "\ u X X X X" )
		 * 
		 * voir : http://java.sun.com/docs/books/jls/second_edition/html/lexical.doc.html#44591
		 */
		super.write( convertNonIso8859ToUnicodeEscapes(aString) );
	}
//150_000 - Fiche 34468 - Support de Tomcat 5.5 et jvm 1.5
// supression de la m�thode
// dummy methode. pour contenir la table des caract�res ISO-8859-1
//	private void zz_table_ISO_8859_1()
//	{
		/*
		 * http://www.bbsinc.com/iso8859.html
		 * 
		 * <blockquote>
		 * <pre>
		 * Description                               Code            Entity name   
		 * ===================================       ============    ==============
		 * quotation mark                            &#34;  --> "    &quot;   --> "
		 * ampersand                                 &#38;  --> &    &amp;    --> &
		 * less-than sign                            &#60;  --> <    &lt;     --> <
		 * greater-than sign                         &#62;  --> >    &gt;     --> >
		 * 
		 * Description                          Char Code            Entity name   
		 * ===================================  ==== ============    ==============
		 * non-breaking space                        &#160; -->      &nbsp;   -->  
		 * inverted exclamation                 �    &#161; --> �    &iexcl;  --> �
		 * cent sign                            �    &#162; --> �    &cent;   --> �
		 * pound sterling                       �    &#163; --> �    &pound;  --> �
		 * general currency sign                �    &#164; --> �    &curren; --> �
		 * yen sign                             �    &#165; --> �    &yen;    --> �
		 * broken vertical bar                  �    &#166; --> �    &brvbar; --> �
		 *                                                           &brkbar; --> &brkbar;
		 * section sign                         �    &#167; --> �    &sect;   --> �
		 * umlaut (dieresis)                    �    &#168; --> �    &uml;    --> �
		 *                                                           &die;    --> &die;
		 * copyright                            �    &#169; --> �    &copy;   --> �
		 * feminine ordinal                     �    &#170; --> �    &ordf;   --> �
		 * left angle quote, guillemotleft      �    &#171; --> �    &laquo;  --> �
		 * not sign                             �    &#172; --> �    &not;    --> �
		 * soft hyphen                          �    &#173; --> �    &shy;    --> �
		 * registered trademark                 �    &#174; --> �    &reg;    --> �
		 * macron accent                        �    &#175; --> �    &macr;   --> �
		 *                                                           &hibar;  --> &hibar;
		 * degree sign                          �    &#176; --> �    &deg;    --> �
		 * plus or minus                        �    &#177; --> �    &plusmn; --> �
		 * superscript two                      �    &#178; --> �    &sup2;   --> �
		 * superscript three                    �    &#179; --> �    &sup3;   --> �
		 * acute accent                         �    &#180; --> �    &acute;  --> �
		 * micro sign                           �    &#181; --> �    &micro;  --> �
		 * paragraph sign                       �    &#182; --> �    &para;   --> �
		 * middle dot                           �    &#183; --> �    &middot; --> �
		 * cedilla                              �    &#184; --> �    &cedil;  --> �
		 * superscript one                      �    &#185; --> �    &sup1;   --> �
		 * masculine ordinal                    �    &#186; --> �    &ordm;   --> �
		 * right angle quote, guillemotright    �    &#187; --> �    &raquo;  --> �
		 * fraction one-fourth                  �    &#188; --> �    &frac14; --> �
		 * fraction one-half                    �    &#189; --> �    &frac12; --> �
		 * fraction three-fourths               �    &#190; --> �    &frac34; --> �
		 * inverted question mark               �    &#191; --> �    &iquest; --> �
		 * capital A, grave accent              �    &#192; --> �    &Agrave; --> �
		 * capital A, acute accent              �    &#193; --> �    &Aacute; --> �
		 * capital A, circumflex accent         �    &#194; --> �    &Acirc;  --> �
		 * capital A, tilde                     �    &#195; --> �    &Atilde; --> �
		 * capital A, dieresis or umlaut mark   �    &#196; --> �    &Auml;   --> �
		 * capital A, ring                      �    &#197; --> �    &Aring;  --> �
		 * capital AE diphthong (ligature)      �    &#198; --> �    &AElig;  --> �
		 * capital C, cedilla                   �    &#199; --> �    &Ccedil; --> �
		 * capital E, grave accent              �    &#200; --> �    &Egrave; --> �
		 * capital E, acute accent              �    &#201; --> �    &Eacute; --> �
		 * capital E, circumflex accent         �    &#202; --> �    &Ecirc;  --> �
		 * capital E, dieresis or umlaut mark   �    &#203; --> �    &Euml;   --> �
		 * capital I, grave accent              �    &#204; --> �    &Igrave; --> �
		 * capital I, acute accent              �    &#205; --> �    &Iacute; --> �
		 * capital I, circumflex accent         �    &#206; --> �    &Icirc;  --> �
		 * capital I, dieresis or umlaut mark   �    &#207; --> �    &Iuml;   --> �
		 * capital Eth, Icelandic               �    &#208; --> �    &ETH;    --> �
		 *                                                           &Dstrok; --> &Dstrok;
		 * capital N, tilde                     �    &#209; --> �    &Ntilde; --> �
		 * capital O, grave accent              �    &#210; --> �    &Ograve; --> �
		 * capital O, acute accent              �    &#211; --> �    &Oacute; --> �
		 * capital O, circumflex accent         �    &#212; --> �    &Ocirc;  --> �
		 * capital O, tilde                     �    &#213; --> �    &Otilde; --> �
		 * capital O, dieresis or umlaut mark   �    &#214; --> �    &Ouml;   --> �
		 * multiply sign                        �    &#215; --> �    &times;  --> �
		 * capital O, slash                     �    &#216; --> �    &Oslash; --> �
		 * capital U, grave accent              �    &#217; --> �    &Ugrave; --> �
		 * capital U, acute accent              �    &#218; --> �    &Uacute; --> �
		 * capital U, circumflex accent         �    &#219; --> �    &Ucirc;  --> �
		 * capital U, dieresis or umlaut mark   �    &#220; --> �    &Uuml;   --> �
		 * capital Y, acute accent              �    &#221; --> �    &Yacute; --> �
		 * capital THORN, Icelandic             �    &#222; --> �    &THORN;  --> �
		 * small sharp s, German (sz ligature)  �    &#223; --> �    &szlig;  --> �
		 * small a, grave accent                �    &#224; --> �    &agrave; --> �
		 * small a, acute accent                �    &#225; --> �    &aacute; --> �
		 * small a, circumflex accent           �    &#226; --> �    &acirc;  --> �
		 * small a, tilde                       �    &#227; --> �    &atilde; --> �
		 * small a, dieresis or umlaut mark     �    &#228; --> �    &auml;   --> �
		 * small a, ring                        �    &#229; --> �    &aring;  --> �
		 * small ae diphthong (ligature)        �    &#230; --> �    &aelig;  --> �
		 * small c, cedilla                     �    &#231; --> �    &ccedil; --> �
		 * small e, grave accent                �    &#232; --> �    &egrave; --> �
		 * small e, acute accent                �    &#233; --> �    &eacute; --> �
		 * small e, circumflex accent           �    &#234; --> �    &ecirc;  --> �
		 * small e, dieresis or umlaut mark     �    &#235; --> �    &euml;   --> �
		 * small i, grave accent                �    &#236; --> �    &igrave; --> �
		 * small i, acute accent                �    &#237; --> �    &iacute; --> �
		 * small i, circumflex accent           �    &#238; --> �    &icirc;  --> �
		 * small i, dieresis or umlaut mark     �    &#239; --> �    &iuml;   --> �
		 * small eth, Icelandic                 �    &#240; --> �    &eth;    --> �
		 * small n, tilde                       �    &#241; --> �    &ntilde; --> �
		 * small o, grave accent                �    &#242; --> �    &ograve; --> �
		 * small o, acute accent                �    &#243; --> �    &oacute; --> �
		 * small o, circumflex accent           �    &#244; --> �    &ocirc;  --> �
		 * small o, tilde                       �    &#245; --> �    &otilde; --> �
		 * small o, dieresis or umlaut mark     �    &#246; --> �    &ouml;   --> �
		 * division sign                        �    &#247; --> �    &divide; --> �
		 * small o, slash                       �    &#248; --> �    &oslash; --> �
		 * small u, grave accent                �    &#249; --> �    &ugrave; --> �
		 * small u, acute accent                �    &#250; --> �    &uacute; --> �
		 * small u, circumflex accent           �    &#251; --> �    &ucirc;  --> �
		 * small u, dieresis or umlaut mark     �    &#252; --> �    &uuml;   --> �
		 * small y, acute accent                �    &#253; --> �    &yacute; --> �
		 * small thorn, Icelandic               �    &#254; --> �    &thorn;  --> �
		 * small y, dieresis or umlaut mark     �    &#255; --> �    &yuml;   --> �
		 * </pre>
		 * </blockquote>
		 */
//	}
}
