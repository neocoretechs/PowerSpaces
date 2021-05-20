package com.neocoretechs.powerspaces.server.handler;

import com.neocoretechs.powerspaces.*;

public class PSStationException extends PowerSpaceException
{
    public PSStationException(String msg)
    {
        super(msg);
    }

    public PSStationException(String messageKey, Object messageParameters[])
    {
        super(messageKey, messageParameters);
    }

    public PSStationException(String messageKey, Object p1)
    {
        super(messageKey, p1);
    }

    public PSStationException(String messageKey, Object p1, Object p2)
    {
        super(messageKey, p1, p2 );
    }

    public PSStationException(String messageKey, Object p1, Object p2, Object p3)
    {
        super(messageKey, p1, p2, p3 );
    }

    public PSStationException(String messageKey, Object p1, Object p2, Object p3, Object p4)
    {
        super(messageKey, p1, p2, p3, p4);
    }

    public PSStationException(String messageKey, Object p1, Object p2, Object p3, Object p4, Object p5)
    {
        super(messageKey, p1, p2, p3, p4, p5 );
    }
}
