
package com.neocoretechs.powerspaces;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
/**
* A Packet is the primary transport for inter-powerplant communication and
* parallel computing.  It provides many features of a "tuple"
* (actual and formal types supported) and can be
* populated with any sort of object that can be searilized
* (primitives are wrapped automagically).  The multiple overloaded constructors
* can be used to create a tuple with up to 7 fields, then an addField call is necessary.
* A Packet is also "Domain OODBMS aware".
*
*/
public class Packet implements Serializable
{
    static final long serialVersionUID = -5315501807343666300L;
    Vector fieldNameandTypes;
    private Vector fieldItems;

    public Packet() {
        if( fieldItems == null ) fieldItems = new Vector();
        if( fieldNameandTypes == null ) fieldNameandTypes = new Vector(5);
    }

    public Packet(Serializable s) throws PowerSpaceException {
        fieldItems = new Vector();
        fieldNameandTypes = new Vector(5);
        addSerialField(s);
    }

    public Packet(Serializable s1, Serializable s2) throws PowerSpaceException {
        fieldItems = new Vector();
        fieldNameandTypes = new Vector(5);
        addSerialField(s1);
        addSerialField(s2);
    }

    public Packet(Serializable s1, Serializable s2, Serializable s3) throws PowerSpaceException {
        fieldItems = new Vector();
        fieldNameandTypes = new Vector(5);
        addSerialField(s1);
        addSerialField(s2);
        addSerialField(s3);
    }

    public Packet(Serializable s1, Serializable s2, Serializable s3, Serializable s4) throws PowerSpaceException {
        fieldItems = new Vector();
        fieldNameandTypes = new Vector(5);
        addSerialField(s1);
        addSerialField(s2);
        addSerialField(s3);
        addSerialField(s4);
    }

    public Packet(Serializable s1, Serializable s2, Serializable s3, Serializable s4, Serializable s5) throws PowerSpaceException {
        fieldItems = new Vector();
        fieldNameandTypes = new Vector(5);
        addSerialField(s1);
        addSerialField(s2);
        addSerialField(s3);
        addSerialField(s4);
        addSerialField(s5);
    }

    public Packet(Serializable s1, Serializable s2, Serializable s3, Serializable s4, Serializable s5, Serializable s6) throws PowerSpaceException {
        fieldItems = new Vector();
        fieldNameandTypes = new Vector(5);
        addSerialField(s1);
        addSerialField(s2);
        addSerialField(s3);
        addSerialField(s4);
        addSerialField(s5);
        addSerialField(s6);
    }

    public Packet(Serializable s1, Serializable s2, Serializable s3, Serializable s4, Serializable s5, Serializable s6, Serializable s7) throws PowerSpaceException {
        fieldItems = new Vector();
        fieldNameandTypes = new Vector(5);
        addSerialField(s1);
        addSerialField(s2);
        addSerialField(s3);
        addSerialField(s4);
        addSerialField(s5);
        addSerialField(s6);
        addSerialField(s7);
    }

    public int getNumberFields() { return fieldItems.size(); }

    /**
    * get the named indexes for DOMAIN.  this method reflected and
    * is expected to return Vector of index identifier objects
    * they can be whatever you like and are passed to DOMAINIndexCompareTo
    * during operation.
    * @return Vector of field names (any Object type)
    * @see DOMAINIndexCompareTo
    */
    public Vector getDOMAINNamedIndexes() {
	// create vector of Field names and types
        if( fieldNameandTypes == null ) {
                fieldNameandTypes = new Vector(fieldItems.size());
                Enumeration en = fieldItems.elements();
                int ipos = 0;
                while(en.hasMoreElements()) {
                        Field f =  ((Field)en.nextElement());
                        // in this case a String composed of name, type, and position
                        fieldNameandTypes.addElement( f.name()+" "+f.type().getName()+" "+String.valueOf(++ipos) );
                }
        }
	return fieldNameandTypes;
    }
    
    public int DOMAINIndexCompareTo(Packet p, Object o) throws PowerSpaceException {
//        System.out.println("IndexCompareTo "+o);
        return getFieldElement(o).compareTo((Serializable)p.getFieldElement(o));
    }

    public synchronized Field getFieldElement(Object o) {
	int fnum = fieldNameandTypes.indexOf(o);
//        System.out.println("getFieldElement "+String.valueOf(fnum));
        return (Field)fieldItems.elementAt(fnum);
    }
	
    private synchronized void addSerialField(Serializable s) throws PowerSpaceException {
        if (s instanceof Field)
            add((Field)s);
        else
            add(new Field(s));
    }

    public synchronized void add(Field f) {
        fieldItems.addElement(f);
        fieldNameandTypes.addElement( f.name()+" "+f.type().getName()+" "+String.valueOf(fieldItems.size()) );
    }

    public synchronized void add(Class t) throws PowerSpaceException {
        add(new Field(t));
    }

    public synchronized void add(String name, Class t) throws PowerSpaceException {
        add(new Field(name, t));
    }

    public synchronized void add(Serializable s) throws PowerSpaceException {
        add(new Field(s));
    }

    public synchronized void add(String name, Serializable s) throws PowerSpaceException {
        add(new Field(name, s));
    }

    public synchronized void add(String tvalue) throws PowerSpaceException {
        add(new Field(tvalue));
    }

    public synchronized void add(String name, String tvalue) throws PowerSpaceException {
        add(new Field(name, tvalue));
    }

    public synchronized void add(int ival) throws PowerSpaceException {
        add(new Field(ival));
    }

    public synchronized void add(String name, int ival) throws PowerSpaceException {
        add(new Field(name, ival));
    }

    public synchronized void add(long lval) throws PowerSpaceException {
        add(new Field(lval));
    }

    public synchronized void add(String name, long lval) throws PowerSpaceException {
        add(new Field(name, lval));
    }

    public synchronized void add(double dval) throws PowerSpaceException {
        add(new Field(dval));
    }

    public synchronized void add(String name, double dval) throws PowerSpaceException {
        add(new Field(name, dval));
    }

    public synchronized void add(float fval) throws PowerSpaceException {
        add(new Field(fval));
    }

    public synchronized void add(String name, float fval) throws PowerSpaceException {
        add(new Field(name, fval));
    }

    public synchronized void setName(String name, int i)
        throws PowerSpaceException
    {
        getField(i).setName(name);
    }

    public synchronized void setType(String name, Class t) throws PowerSpaceException {
        getField(name).setType(t);
    }

    public synchronized void setValue(String name, Serializable v)
        throws PowerSpaceException
    {
        getField(name).setValue(v);
    }

    public synchronized void setValue(int i, Serializable v)
        throws PowerSpaceException
    {
        getField(i).setValue(v);
    }

    public synchronized void setFormal(int i)
        throws PowerSpaceException
    {
        getField(i).setFormal();
    }

    public synchronized void setFormal(String name)
        throws PowerSpaceException
    {
        getField(name).setFormal();
    }

    public synchronized void changeName(String oldName, String newName)
        throws PowerSpaceException
    {
        getField(oldName).setName(newName);
    }

    public synchronized void putField(Field f, int i)
        throws PowerSpaceException
    {
        try
        {
            fieldItems.insertElementAt(f, i);
            fieldNameandTypes.insertElementAt( f.name()+" "+f.type().getName()+" "+String.valueOf(i), i );
            return;
        }
        catch (ArrayIndexOutOfBoundsException e)
        {            
        }
        throw new PowerSpaceException("Packet field index out of bounds",
         PowerSpaceException.makeArray(new Integer(i), new Integer(fieldItems.size())) );
    }

    public synchronized Field getField(int i) throws PowerSpaceException {
        try
        {
            return (Field)fieldItems.elementAt(i);
        } catch (ArrayIndexOutOfBoundsException aioob) {}
        throw new PowerSpaceException("Packet field index out of bounds",
         PowerSpaceException.makeArray(new Integer(i), new Integer(fieldItems.size())) );
    }

    public synchronized boolean fieldExists(String name) {
        boolean retValue = true;
        try
        {
            if (getField(name) != null)
                retValue = true;
        }
        catch (PowerSpaceException e)
        {
            retValue = false;
        }
        return retValue;
    }

    public synchronized Field getField(String name)
        throws PowerSpaceException
    {
        for (Enumeration e = fields(); e.hasMoreElements(); ) {
            Field f = (Field)e.nextElement();
            if (name.equals(f.name()))
                return f;
        }
        Object args[] = { name };
        throw new PowerSpaceException("Packet field name out of bounds", args);
    }

    public synchronized Enumeration fields() {
        return fieldItems.elements();
    }

    public synchronized boolean template() {
        boolean retValue = false;
        for (Enumeration e = fields(); e.hasMoreElements(); ) {
            Field f = (Field)e.nextElement();
            if (f.formal()) {
                retValue = true;
                break;
            }
        }
        return retValue;
    }

    public synchronized int numberOfFields() {
        return fieldItems.size();
    }

    public synchronized boolean matches(Packet p) {
        Enumeration e1;
        Enumeration e2;
        Field f1;
        Field f2;
        boolean retValue = true;
        int fis = fieldItems.size();
        if (!getClass().isInstance(p)) return false;
        if (fis == 0) return true;
        if (fis == p.fieldItems.size()) {
            e1 = fields();
            for (e2 = p.fields(); e1.hasMoreElements() && e2.hasMoreElements(); ) {
                f1 = (Field)e1.nextElement();
                f2 = (Field)e2.nextElement();
                if (!f1.matches(f2)) {
                    retValue = false;
                    break;
                }
            }
        }
        else
            retValue = false;
        return retValue;
    }


    public synchronized String toStructureString() {
        StringBuffer sb = new StringBuffer("[ ");
        int count = 0;
        int limit = fieldItems.size();
        for (Enumeration e = fields(); e.hasMoreElements(); )
        {
            Field f = (Field)e.nextElement();
            if (count < limit - 1)
                sb.append(new StringBuffer(String.valueOf(f.type().toString())).append(", ").toString());
            else
                sb.append(f.type().toString());
            count++;
        }
        sb.append(" ]");
        return sb.toString();
    }

    public synchronized String toString() {
        StringBuffer sb = new StringBuffer("[ ");
        int count = 0;
        int limit = fieldItems.size();
        for (Enumeration e = fields(); e.hasMoreElements(); )
        {
            Field f = (Field)e.nextElement();
            if (count < limit - 1)
                sb.append(new StringBuffer(String.valueOf(f.toString())).append(", ").toString());
            else
                sb.append(f.toString());
            count++;
        }
        sb.append(" ]");
        return sb.toString();
    }

    public synchronized void printOn(PrintStream pout) {
        int count = 0;
        int limit = fieldItems.size();
        pout.println("[");
        for (Enumeration e = fields(); e.hasMoreElements(); )
        {
            Field f = (Field)e.nextElement();
            if (count < limit - 1)
                pout.println(new StringBuffer(String.valueOf(f.toString())).append(", ").toString());
            else
                pout.println(f.toString());
            count++;
        }
        pout.println("]");
    }

}
