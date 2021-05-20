package com.neocoretechs.powerspaces;
import java.io.*;
import java.lang.reflect.*;
/**
* Packets are composed of instances of this Field class.  Each Field
* can be a Formal type (a la Linda) or Value.  Fields are "Domain OODBMS aware"
* and provide a compareTo method for comparison to another Field.
* @author Groff (C) NeoCoreTechs, Inc. 1998-2000
*/
public class Field implements Serializable
{
    static final long serialVersionUID = 6516057593319644836L;
    protected String Name;
    protected Class Type;
    protected Serializable Value;
    protected boolean isFormal;
    protected boolean noMethod;
    // compareTo method invokee
    protected transient Method compareMethod = null;
    private boolean compareNotSet = true;
    // this used in the case where there is no compareTo method
    // we Serialize the object and compare it to antoher serial object
    protected byte[] serializedObject;
    //
    public Field(Serializable v) throws PowerSpaceException {
        this(null, null, v, false);
    }

    public Field(Class t, Serializable v) throws PowerSpaceException {
        this(null, t, v, false);
    }

    public Field(String name, Class t, Serializable v)  throws PowerSpaceException {
        this(name, t, v, false);
    }

    public Field(Class t) throws PowerSpaceException {
        this(null, t, null, true);
    }

    public Field(String name, Class t) throws PowerSpaceException {
        this(name, t, null, true);
    }

    public Field(String name, Serializable v) throws PowerSpaceException {
        this(name, null, v, false);
    }

    public Field() {}

    private Field(String name, Class t, Serializable v, boolean isFormal) throws PowerSpaceException {
        isFormal = false;
        if (v != null && t != null && !t.isInstance(v))
            throw new PowerSpaceException("Packet field class mismatch", null);
        if (t == null && v == null)
            throw new PowerSpaceException("Packet field is null", null);
        if (isFormal && v != null)
            throw new Error("Formal field has value");
        if (t == null)
            t = v.getClass();
        Name = name;
        Value = v;
        isFormal = isFormal;
    }

    private synchronized void setupCompare(Class t) throws PowerSpaceException
    {
        if (t instanceof Serializable) {
                Type = t;
                // see if we have compareTo method
                try {
                        Class args[] = { java.io.Serializable.class };
                        compareMethod = Type.getMethod("compareTo",args);
                } catch(Exception e) {
                        // at this point we have no compareTo method, serialize the
                        // object to byte array for comparison to another
                        noMethod = true;
                        try {
                                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                                ObjectOutputStream oo = new ObjectOutputStream(bo);
                                oo.writeObject(Value);
                                oo.flush();
                                serializedObject = bo.toByteArray();
                        } catch(IOException ioe) { throw new PowerSpaceException(e.getMessage(), null); }
                }
        } else  {
                throw new PowerSpaceException("Attempt to use a non-Serializable object for Field",PowerSpaceException.makeArray(Name, Type, Value));
        }
    }


    public Field(Field f) {
        isFormal = false;
        Type = f.Type;
        Value = f.Value;
        isFormal = f.isFormal;
        Name = f.Name;
    }


    public Field(float f) throws PowerSpaceException {
        this(new Float(f));
    }

    public Field(String name, float f) throws PowerSpaceException {
        this(name, new Float(f));
    }

    public Field(double d) throws PowerSpaceException {
        this(new Double(d));
    }

    public Field(String name, double d) throws PowerSpaceException {
        this(name, new Double(d));
    }

    public Field(int i) throws PowerSpaceException {
        this(new Integer(i));
    }

    public Field(String name, int i) throws PowerSpaceException {
        this(name, new Integer(i));
    }

    public Field(long l) throws PowerSpaceException {
        this(new Long(l));
    }

    public Field(String name, long l) throws PowerSpaceException {
        this(name, new Long(l));
    }

    public synchronized void setName(String name) {
        Name = name;
    }

    public synchronized void setType(Class t) throws PowerSpaceException {
        if (t instanceof Serializable)
            Type = t;
        else
            throw new PowerSpaceException("Attempt to set type to non-Serializable Class",PowerSpaceException.makeArray(t));
    }

    public synchronized void setValue(Serializable v) throws PowerSpaceException {
        if (v == null)
            throw new PowerSpaceException("Packet field is null", null);
        Type = v.getClass();
        Value = v;
        isFormal = false;
    }

    public synchronized void setFormal() {
        Value = null;
        isFormal = true;
    }

    public synchronized void assign(Field f) {
        Type = f.Type;
        Value = f.Value;
        isFormal = f.isFormal;
        Name = f.Name;
    }

    public synchronized String name() {
        return Name;
    }

    public synchronized Class type() {
        if (Value != null) return Value.getClass();
        if (Type != null) return Type;
        return null;
    }

    public synchronized Serializable value() {
        return Value;
    }

    public synchronized boolean formal() {
        return isFormal;
    }

    public synchronized boolean matches(Field f) {
        if (formal() && f.formal())
            return false;
        if ((formal() || f.formal()) && type().isAssignableFrom(f.type()))
            return true;
        if (formal() || f.formal())
            return false;
        if (Value == null)
            return f.Value == null;
        if (Value instanceof Packet && f.Value != null && f.Value instanceof Packet)
            return ((Packet)Value).matches((Packet)f.Value);
        else
            return Value.equals(f.Value);
    }

    public synchronized String toString()
    {
        String retValue = null;
        if (!isFormal)
        {
            if (Value instanceof String)
                retValue = "\"" + Value.toString() + "\"";
            else
              if (Value == null)
                retValue = "null";
              else
                retValue = Value.toString();
        }
        else
            if (Type != null)
                retValue = Type.getName() + ">";
        if (Name != null)
            return ": " + retValue;
        else
            return retValue;
    }

    public synchronized static Field makeField(String className) throws PowerSpaceException {
        try
        {
            return new Field(Class.forName(className));
        }
        catch (IllegalArgumentException e1)
        {
            throw new PowerSpaceException("Field class not found", className);
        }
        catch (ClassNotFoundException e2)
        {
            throw new PowerSpaceException("Field class not found", className);
        }
    }

    public int compareTo(Serializable ti) throws PowerSpaceException {
                int compareRes;
//                System.out.println("Field compareTo");
                //
                if( compareNotSet ) {
                        compareNotSet = false;
                        setupCompare(Value.getClass());
                }
                // if the compare method for the other field is null
                // it wraps an object without a compareTo method
                // if the compare method for this one is null, this wraps
                // such an object.  If this method null we have to
                // compare the bytes.  If this null and other not
                // we have to serialize that object first
                try {
                    if( noMethod ) {
                        byte[] so;
                        if( !((Field)ti).noMethod ) {
                                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                                ObjectOutputStream oo = new ObjectOutputStream(bo);
                                oo.writeObject(((Field)ti).Value);
                                oo.flush();
                                so = bo.toByteArray();
                        } else
                                so = ((Field)ti).serializedObject;
                        // compare the bytes
                        for(int i = 0 ; i < serializedObject.length ; i++ ) {
                                if( i == so.length) return 1;
                                if( serializedObject[i] > so[i] ) return 1;
                                if( serializedObject[i] < so[i] ) return -1;
                        }
                        // all = ?
                        if( serializedObject.length < ((Field)ti).serializedObject.length ) return -1;
                        return 0;
                    }
                    // may be a method somewhere, noMethod is false
                    // may not have been intialized yet from Serial state
                    if( compareMethod == null ) setupCompare(Type);
                    //
                    // if noMethod problem, since it should have been saved in Serialized state on
                    // init of Field and we should have gotten it above
                    if( noMethod ) throw new PowerSpaceException("compareTo method inconsistency",null); 
                    Object[] args = {((Field)ti).Value};
                    compareRes = ((Integer)compareMethod.invoke(Value,args)).intValue();
                    return compareRes;
                    //
                } catch(Exception oop) { throw new PowerSpaceException(oop.getMessage(), null); }
   }

}
