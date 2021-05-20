package com.neocoretechs.powerspaces;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
/**
* General purpose special exception
*/
public class PowerSpaceException extends Exception
{
    private static final Locale supportedLocales[] = { Locale.US, new Locale("DE", "DE") };
    private static Locale currentLocale;
    private static final String RESOURCE_BUNDLE_NAME = "com.neocoretechs.PowerSpaces.PowerSpaceERB";
    private static ResourceBundle resourceBundle;

    public PowerSpaceException(String msg)
    {
        super(msg);
    }

    public PowerSpaceException(String messageKey, Object messageParameters[])
    {
//        super(MessageFormat.format(getBundle().getString(messageKey), removeNulls(messageParameters)));
        super(MessageFormat.format(messageKey, removeNulls(messageParameters)));
    }
    public static Object[] makeArray(Object p1) {
        Object r[] = new Object[1];
        r[0] = p1;
        return r;
    }
    public static Object[] makeArray(Object p1, Object p2) {
        Object r[] = new Object[2];
        r[0] = p1; r[1] = p2;
        return r;
    }
    public static Object[] makeArray(Object p1, Object p2, Object p3) {
        Object r[] = new Object[3];
        r[0] = p1; r[1] = p2; r[2] = p3;
        return r;
    }
    public static Object[] makeArray(Object p1, Object p2, Object p3, Object p4) {
        Object r[] = new Object[4];
        r[0] = p1; r[1] = p2; r[2] = p3; r[3] = p4;
        return r;
    }
    public static Object[] makeArray(Object p1, Object p2, Object p3, Object p4, Object p5) {
        Object r[] = new Object[5];
        r[0] = p1; r[1] = p2; r[2] = p3; r[3] = p4; r[4] = p5;
        return r;
    }
    public PowerSpaceException(String messageKey, Object p1)
    {
        this(messageKey+" {0}", makeArray(p1) );
    }

    public PowerSpaceException(String messageKey, Object p1, Object p2)
    {
        this(messageKey+" {0} {1}", makeArray(p1, p2));
    }

    public PowerSpaceException(String messageKey, Object p1, Object p2, Object p3)
    {
        this(messageKey+" {0} {1} {2}", makeArray(p1, p2, p3));
    }

    public PowerSpaceException(String messageKey, Object p1, Object p2, Object p3, Object p4)
    {
        this(messageKey+" {0} {1} {2} {3}", makeArray(p1, p2, p3, p4));
    }

    public PowerSpaceException(String messageKey, Object p1, Object p2, Object p3, Object p4, Object p5)
    {
        this(messageKey+" {0} {1} {2} {3} {4}", makeArray(p1, p2, p3, p4, p5));
    }

    public boolean equals(Object o)
    {
        return super.equals(o);
    }

    protected static Object[] removeNulls(Object params[])
    {
        if (params == null)
            return null;
        for (int i = 0; i < params.length; i++)
            params[i] = (params[i] == null) ? "null" : params[i];
        return params;
    }

    protected static ResourceBundle getBundle()
    {
        if (currentLocale == null)
            currentLocale = supportedLocales[0];
        if (resourceBundle == null)
            resourceBundle = ResourceBundle.getBundle("com.neocoretechs.PowerSpaces.PowerSpaceERB", currentLocale);
        return resourceBundle;
    }

    static 
    {
        currentLocale = supportedLocales[0];
        resourceBundle = null;
    }
}
