package jargs.gnu;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Largely GNU-compatible command-line options parser. Has short (-v) and
 * long-form (--verbose) option support, and also allows options with associated
 * values (-d 2, --debug 2, --debug=2). Option processing can be explicitly
 * terminated by the argument '--'.
 * 
 * Update by Thomas Calmant: code update according to Sonar and PSEM2M
 * guidelines.
 * 
 * @author Steve Purcell
 * @version $Revision: 1.10 $
 * @see jargs.examples.gnu.OptionTest
 */
public class CmdLineParser {

    /**
     * Thrown when an illegal or missing value is given by the user for an
     * option that takes a value. <code>getMessage()</code> returns an error
     * string suitable for reporting the error to the user (in English).
     */
    public static class IllegalOptionValueException extends OptionException {

        /** Serialization version */
        private static final long serialVersionUID = 1L;

        /** Erroneous option */
        private final Option option;

        /** Illegal value */
        private final String value;

        /**
         * Sets up the exception
         * 
         * @param opt
         *            Erroneous option
         * @param value
         *            Illegal value
         */
        public IllegalOptionValueException(final Option opt, final String value) {

            super(MessageFormat
                    .format("Illegal value ''{0}'' for option {1}--{2}", value,
                            (opt.shortForm() != null ? "-" + opt.shortForm()
                                    + "/" : ""), opt.longForm()));
            this.option = opt;
            this.value = value;
        }

        /**
         * @return the name of the option whose value was illegal (e.g. "-u")
         */
        public Option getOption() {

            return this.option;
        }

        /**
         * @return the illegal value
         */
        public String getValue() {

            return this.value;
        }
    }

    /**
     * Thrown when the parsed command line contains multiple concatenated short
     * options, such as -abcd, where one or more requires a value.
     * <code>getMessage()</code> returns an English human-readable error string.
     * 
     * @author Vidar Holen
     */
    public static class NotFlagException extends UnknownOptionException {

        /** Serialization version */
        private static final long serialVersionUID = 1L;

        /** The one-char parameter that must have a value */
        private final char notflag;

        /**
         * Sets up the exception
         * 
         * @param option
         *            The option corresponding to the parameter
         * @param unflaggish
         *            The one-char parameter that must have a value
         */
        public NotFlagException(final String option, final char unflaggish) {

            super(option, MessageFormat.format(
                    "Illegal option: ''{0}'', ''{1}'' requires a value",
                    option, unflaggish));
            notflag = unflaggish;
        }

        /**
         * @return the first character which wasn't a boolean (e.g 'c')
         */
        public char getOptionChar() {

            return notflag;
        }
    }

    /**
     * Representation of a command-line option
     */
    public abstract static class Option {

        public static class BooleanOption extends Option {

            public BooleanOption(final char shortForm, final String longForm) {

                super(shortForm, longForm, false);
            }

            public BooleanOption(final String longForm) {

                super(longForm, false);
            }
        }

        /**
         * An option that expects a floating-point value
         */
        public static class DoubleOption extends Option {

            public DoubleOption(final char shortForm, final String longForm) {

                super(shortForm, longForm, true);
            }

            public DoubleOption(final String longForm) {

                super(longForm, true);
            }

            @Override
            protected Object parseValue(final String arg, final Locale locale)
                    throws IllegalOptionValueException {

                try {
                    final NumberFormat format = NumberFormat
                            .getNumberInstance(locale);
                    final Number num = format.parse(arg);
                    return new Double(num.doubleValue());
                } catch (final ParseException e) {
                    throw new IllegalOptionValueException(this, arg);
                }
            }
        }

        /**
         * An option that expects an integer value
         */
        public static class IntegerOption extends Option {

            public IntegerOption(final char shortForm, final String longForm) {

                super(shortForm, longForm, true);
            }

            public IntegerOption(final String longForm) {

                super(longForm, true);
            }

            @Override
            protected Object parseValue(final String arg, final Locale locale)
                    throws IllegalOptionValueException {

                try {
                    return Integer.valueOf(arg);

                } catch (final NumberFormatException e) {
                    throw new IllegalOptionValueException(this, arg);
                }
            }
        }

        /**
         * An option that expects a long integer value
         */
        public static class LongOption extends Option {

            public LongOption(final char shortForm, final String longForm) {

                super(shortForm, longForm, true);
            }

            public LongOption(final String longForm) {

                super(longForm, true);
            }

            @Override
            protected Object parseValue(final String arg, final Locale locale)
                    throws IllegalOptionValueException {

                try {
                    return new Long(arg);
                } catch (final NumberFormatException e) {
                    throw new IllegalOptionValueException(this, arg);
                }
            }
        }

        /**
         * An option that expects a string value
         */
        public static class StringOption extends Option {

            public StringOption(final char shortForm, final String longForm) {

                super(shortForm, longForm, true);
            }

            public StringOption(final String longForm) {

                super(longForm, true);
            }

            @Override
            protected Object parseValue(final String arg, final Locale locale) {

                return arg;
            }
        }

        private String longForm = null;

        private String shortForm = null;

        private boolean wantsValue = false;

        protected Option(final char shortForm, final String longForm,
                final boolean wantsValue) {

            this(new String(new char[] { shortForm }), longForm, wantsValue);
        }

        protected Option(final String longForm, final boolean wantsValue) {

            this(null, longForm, wantsValue);
        }

        private Option(final String shortForm, final String longForm,
                final boolean wantsValue) {

            if (longForm == null) {
                throw new IllegalArgumentException("Null longForm not allowed");
            }
            this.shortForm = shortForm;
            this.longForm = longForm;
            this.wantsValue = wantsValue;
        }

        public final Object getValue(final String arg, final Locale locale)
                throws IllegalOptionValueException {

            if (this.wantsValue) {
                if (arg == null) {
                    throw new IllegalOptionValueException(this, "");
                }
                return this.parseValue(arg, locale);
            } else {
                return Boolean.TRUE;
            }
        }

        public String longForm() {

            return this.longForm;
        }

        /**
         * Override to extract and convert an option value passed on the
         * command-line
         */
        protected Object parseValue(final String arg, final Locale locale)
                throws IllegalOptionValueException {

            return null;
        }

        public String shortForm() {

            return this.shortForm;
        }

        /**
         * Tells whether or not this option wants a value
         */
        public boolean wantsValue() {

            return this.wantsValue;
        }
    }

    /**
     * Base class for exceptions that may be thrown when options are parsed
     */
    public abstract static class OptionException extends Exception {

        /** Serialization version */
        private static final long serialVersionUID = 1L;

        /**
         * Sets up the exception
         * 
         * @param msg
         *            The exception message
         */
        public OptionException(final String msg) {

            super(msg);
        }
    }

    /**
     * Thrown when the parsed command-line contains an option that is not
     * recognized. <code>getMessage()</code> returns an error string suitable
     * for reporting the error to the user (in English).
     */
    public static class UnknownOptionException extends OptionException {

        /** Serialization version */
        private static final long serialVersionUID = 1L;

        /** Unrecognized option string */
        private String optionName = null;

        /**
         * Sets up the exception, with a default message
         * 
         * @param optionName
         *            The unrecognized option string
         */
        public UnknownOptionException(final String optionName) {

            this(optionName, MessageFormat.format("Unknown option ''{0}''",
                    optionName));
        }

        /**
         * Sets up the exception, with the given message
         * 
         * @param optionName
         *            The unrecognized option string
         * @param msg
         *            A description of the error
         */
        public UnknownOptionException(final String optionName, final String msg) {

            super(msg);
            this.optionName = optionName;
        }

        /**
         * @return the name of the option that was unknown (e.g. "-u")
         */
        public String getOptionName() {

            return this.optionName;
        }
    }

    /**
     * Thrown when the parsed command line contains multiple concatenated short
     * options, such as -abcd, where one is unknown. <code>getMessage()</code>
     * returns an English human-readable error string.
     * 
     * @author Vidar Holen
     */
    public static class UnknownSuboptionException extends
            UnknownOptionException {

        /** Serialization version */
        private static final long serialVersionUID = 1L;

        /** The unknown one-char parameter */
        private final char suboption;

        /**
         * Sets up the exception
         * 
         * @param option
         *            The parsed option string (concatenated one-char
         *            parameters)
         * @param suboption
         *            The unknown one-char parameter in the option string
         */
        public UnknownSuboptionException(final String option,
                final char suboption) {

            super(option, MessageFormat.format(
                    "Illegal option: ''{0}'' in ''{1}''", suboption, option));
            this.suboption = suboption;
        }

        public char getSuboption() {

            return suboption;
        }
    }

    /** The available program options */
    private final Map<String, Option> options = new HashMap<String, Option>();

    /** The extra arguments of the command line */
    private String[] remainingArgs = null;

    /** The parsed values */
    private Map<String, List<Object>> values = new HashMap<String, List<Object>>();

    /**
     * Convenience method for adding a boolean option.
     * 
     * @return the new Option
     */
    public final Option addBooleanOption(final char shortForm,
            final String longForm) {

        return addOption(new Option.BooleanOption(shortForm, longForm));
    }

    /**
     * Convenience method for adding a boolean option.
     * 
     * @return the new Option
     */
    public final Option addBooleanOption(final String longForm) {

        return addOption(new Option.BooleanOption(longForm));
    }

    /**
     * Convenience method for adding a double option.
     * 
     * @return the new Option
     */
    public final Option addDoubleOption(final char shortForm,
            final String longForm) {

        return addOption(new Option.DoubleOption(shortForm, longForm));
    }

    /**
     * Convenience method for adding a double option.
     * 
     * @return the new Option
     */
    public final Option addDoubleOption(final String longForm) {

        return addOption(new Option.DoubleOption(longForm));
    }

    /**
     * Convenience method for adding an integer option.
     * 
     * @return the new Option
     */
    public final Option addIntegerOption(final char shortForm,
            final String longForm) {

        return addOption(new Option.IntegerOption(shortForm, longForm));
    }

    /**
     * Convenience method for adding an integer option.
     * 
     * @return the new Option
     */
    public final Option addIntegerOption(final String longForm) {

        return addOption(new Option.IntegerOption(longForm));
    }

    /**
     * Convenience method for adding a long integer option.
     * 
     * @return the new Option
     */
    public final Option addLongOption(final char shortForm,
            final String longForm) {

        return addOption(new Option.LongOption(shortForm, longForm));
    }

    /**
     * Convenience method for adding a long integer option.
     * 
     * @return the new Option
     */
    public final Option addLongOption(final String longForm) {

        return addOption(new Option.LongOption(longForm));
    }

    /**
     * Add the specified Option to the list of accepted options
     */
    public final Option addOption(final Option opt) {

        if (opt.shortForm() != null) {
            this.options.put("-" + opt.shortForm(), opt);
        }
        this.options.put("--" + opt.longForm(), opt);
        return opt;
    }

    /**
     * Convenience method for adding a string option.
     * 
     * @return the new Option
     */
    public final Option addStringOption(final char shortForm,
            final String longForm) {

        return addOption(new Option.StringOption(shortForm, longForm));
    }

    /**
     * Convenience method for adding a string option.
     * 
     * @return the new Option
     */
    public final Option addStringOption(final String longForm) {

        return addOption(new Option.StringOption(longForm));
    }

    /**
     * Stores a parsed value for the given option
     * 
     * @param aOption
     *            A parameter
     * @param aValue
     *            The parsed value
     */
    private void addValue(final Option aOption, final Object aValue) {

        final String longForm = aOption.longForm();

        List<Object> list = values.get(longForm);

        if (list == null) {
            list = new ArrayList<Object>();
            values.put(longForm, list);
        }

        list.add(aValue);
    }

    /**
     * Equivalent to {@link #getOptionValue(Option, Object) getOptionValue(o,
     * null)}.
     */
    public final Object getOptionValue(final Option aOption) {

        return getOptionValue(aOption, null);
    }

    /**
     * @return the parsed value of the given Option, or null if the option was
     *         not set
     */
    public final Object getOptionValue(final Option aOption,
            final Object aDefault) {

        final List<Object> list = values.get(aOption.longForm());

        if (list == null) {
            return aDefault;

        } else if (list.isEmpty()) {
            return null;

        } else {
            final Object result = list.get(0);
            list.remove(0);
            return result;
        }
    }

    /**
     * @return A Vector giving the parsed values of all the occurrences of the
     *         given Option, or an empty Vector if the option was not set.
     */
    public final List<Object> getOptionValues(final Option aOption) {

        final List<Object> result = new ArrayList<Object>();

        while (true) {
            final Object value = getOptionValue(aOption, null);

            if (value == null) {
                return result;

            } else {
                result.add(value);
            }
        }
    }

    /**
     * @return the non-option arguments
     */
    public final String[] getRemainingArgs() {

        return this.remainingArgs;
    }

    /**
     * Extract the options and non-option arguments from the given list of
     * command-line arguments. The default locale is used for parsing options
     * whose values might be locale-specific.
     */
    public final void parse(final String[] argv)
            throws IllegalOptionValueException, UnknownOptionException {

        // It would be best if this method only threw OptionException, but for
        // backwards compatibility with old user code we throw the two
        // exceptions above instead.

        parse(argv, Locale.getDefault());
    }

    /**
     * Extract the options and non-option arguments from the given list of
     * command-line arguments. The specified locale is used for parsing options
     * whose values might be locale-specific.
     */
    public final void parse(final String[] argv, final Locale locale)
            throws IllegalOptionValueException, UnknownOptionException {

        // It would be best if this method only threw OptionException, but for
        // backwards compatibility with old user code we throw the two
        // exceptions above instead.

        final List<Object> otherArgs = new ArrayList<Object>();
        int position = 0;
        this.values = new Hashtable<String, List<Object>>();
        while (position < argv.length) {
            String curArg = argv[position];
            if (curArg.startsWith("-")) {
                if (curArg.equals("--")) { // end of options
                    position += 1;
                    break;
                }
                String valueArg = null;
                if (curArg.startsWith("--")) { // handle --arg=value
                    final int equalsPos = curArg.indexOf('=');
                    if (equalsPos != -1) {
                        valueArg = curArg.substring(equalsPos + 1);
                        curArg = curArg.substring(0, equalsPos);
                    }
                } else if (curArg.length() > 2) { // handle -abcd
                    for (int i = 1; i < curArg.length(); i++) {
                        final Option opt = this.options.get("-"
                                + curArg.charAt(i));
                        if (opt == null) {
                            throw new UnknownSuboptionException(curArg,
                                    curArg.charAt(i));
                        }
                        if (opt.wantsValue()) {
                            throw new NotFlagException(curArg, curArg.charAt(i));
                        }
                        addValue(opt, opt.getValue(null, locale));

                    }
                    position++;
                    continue;
                }

                final Option opt = this.options.get(curArg);
                if (opt == null) {
                    throw new UnknownOptionException(curArg);
                }
                Object value = null;
                if (opt.wantsValue()) {
                    if (valueArg == null) {
                        position += 1;
                        if (position < argv.length) {
                            valueArg = argv[position];
                        }
                    }
                    value = opt.getValue(valueArg, locale);
                } else {
                    value = opt.getValue(null, locale);
                }

                addValue(opt, value);

                position += 1;
            } else {
                otherArgs.add(curArg);
                position += 1;
            }
        }
        for (; position < argv.length; ++position) {
            otherArgs.add(argv[position]);
        }

        this.remainingArgs = otherArgs.toArray(new String[otherArgs.size()]);
    }
}
