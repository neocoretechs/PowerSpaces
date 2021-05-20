
package com.neocoretechs.powerspaces;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
/**
* A SystemPacket is derived from Packet and provides the addition of
* a session and transport operation field for system-level communications
* betweem PowerPlants
*/
public class SystemPacket extends Packet implements Serializable
{
    private int transportOp;
    private String session;

    public void setOp(int top) { transportOp = top; }
    public int getOp() { return transportOp; }
    public void setSession(String tsession) { session = tsession; }
    public String getSession() { return session; }

    public SystemPacket()
    {
        super();
    }

    public SystemPacket(Serializable obj)
        throws PowerSpaceException
    {
        super(obj);
    }

    public SystemPacket(int top, String tsession, Serializable obj)
        throws PowerSpaceException
    {
        super(obj);
        transportOp = top;
        session = tsession;
    }

    public SystemPacket(int top, String tsession, Serializable obj1, Serializable obj2)
        throws PowerSpaceException
    {
        super(obj1, obj2);
        transportOp = top;
        session = tsession;
    }

    public SystemPacket(Serializable obj1, Serializable obj2)
        throws PowerSpaceException
    {
        super(obj1, obj2);
    }

    public SystemPacket(Serializable obj1, Serializable obj2, Serializable obj3)
        throws PowerSpaceException
    {
        super(obj1, obj2, obj3);
    }

    public SystemPacket(Serializable obj1, Serializable obj2, Serializable obj3, Serializable obj4)
        throws PowerSpaceException
    {
        super(obj1, obj2, obj3, obj4);
    }

    public SystemPacket(Serializable obj1, Serializable obj2, Serializable obj3, Serializable obj4, Serializable obj5)
        throws PowerSpaceException
    {
        super(obj1, obj2, obj3, obj4, obj5);
    }

    public SystemPacket(Serializable obj1, Serializable obj2, Serializable obj3, Serializable obj4, Serializable obj5, Serializable obj6)
        throws PowerSpaceException
    {
        super(obj1, obj2, obj3, obj4, obj5, obj6);
    }

    public SystemPacket(Serializable obj1, Serializable obj2, Serializable obj3, Serializable obj4, Serializable obj5, Serializable obj6, Serializable obj7)
        throws PowerSpaceException
    {
        super(obj1, obj2, obj3, obj4, obj5, obj6, obj7);
    }
}
