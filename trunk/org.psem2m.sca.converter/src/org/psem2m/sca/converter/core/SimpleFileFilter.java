/**
 * File:   SimpleFileFilter.java
 * Author: Thomas Calmant
 * Date:   6 janv. 2012
 */
package org.psem2m.sca.converter.core;

import java.io.File;
import java.io.FileFilter;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * A simple filename filter
 * 
 * @author Thomas Calmant
 */
public class SimpleFileFilter implements FileFilter {

    /** The pattern */
    private Pattern pPattern;

    /**
     * Default constructor, matching all files
     */
    public SimpleFileFilter() {

        this(null, false);
    }

    /**
     * Sets up the constructor with a pattern.
     * 
     * @param aPattern
     *            A simple filter pattern
     * @param aCaseSensitive
     *            If true, the filter will be case sensitive
     */
    public SimpleFileFilter(final String aPattern,
            final boolean aCaseSensitive) {

        setPattern(aPattern, aCaseSensitive);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.FileFilter#accept(java.io.File)
     */
    @Override
    public boolean accept(final File aFile) {

        if (!aFile.isFile()) {
            // Only accept files
            return false;
        }

        return pPattern.matcher(aFile.getName()).matches();
    }

    /**
     * Prepares the filter to test files against the given pattern
     * 
     * @param aPattern
     *            A simple filter pattern
     * @param aCaseSensitive
     *            If true, the filter will be case sensitive
     */
    public void setPattern(final String aPattern, final boolean aCaseSensitive) {

        // Normalize the pattern
        String pattern;
        if (aPattern == null || aPattern.isEmpty()) {
            pattern = "*";

        } else {
            pattern = aPattern;
        }

        // The built regex filter
        final StringBuilder regexFilter = new StringBuilder();

        // Convert the pattern to regex
        final StringTokenizer tokenizer = new StringTokenizer(pattern, "?*",
                true);

        while (tokenizer.hasMoreTokens()) {
            final String token = tokenizer.nextToken();
            if (token.equals("?")) {
                regexFilter.append(".");

            } else if (token.equals("*")) {
                regexFilter.append(".*");

            } else {
                regexFilter.append(Pattern.quote(token));
            }
        }

        // Prepare the pattern
        if (!aCaseSensitive) {
            pPattern = Pattern.compile(regexFilter.toString(),
                    Pattern.CASE_INSENSITIVE);

        } else {
            pPattern = Pattern.compile(regexFilter.toString());
        }
    }
}
