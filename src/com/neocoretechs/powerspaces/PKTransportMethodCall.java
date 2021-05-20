package com.neocoretechs.powerspaces;
import java.io.*;
/**
* The PKTransportMethodCall object is used to return the
* methods a remote "handler" class has available for invocation.
* @author Groff Copyright (C) NeoCoreTechs, Inc. 1998-2000
*/ 
public final class PKTransportMethodCall implements Serializable {
       static final long serialVersionUID = 8649844374668828845L;
       private String session, className, objref, methodName;
       private Object[] paramArray;
       /**
       * Prep PKTransportMethodCall to send remote method call
       */
       public PKTransportMethodCall(String tsession, String tclass, String tobjref, String tmeth, Object[] o1) {
                session = tsession;
                className = tclass;
                objref = tobjref;
                methodName = tmeth;
                paramArray = o1;
       }
       public void setPKTransportMethodCall(String tsession, String tclass, String tobjref, String tmeth, Object[] o1) {
                session = tsession;
                className = tclass;
                objref = tobjref;
                methodName = tmeth;
                paramArray = o1;
       }
       public PKTransportMethodCall(String tsession, String tclass, String tobjref, String tmeth) {
                session = tsession;
                className = tclass;
                objref = tobjref;
                methodName = tmeth;
                paramArray = new Object[0];
       }
       public PKTransportMethodCall(String tsession, String tclass, String tobjref, String tmeth, Packet p) {
                session = tsession;
                className = tclass;
                objref = tobjref;
                methodName = tmeth;
                paramArray = new Object[1];
                paramArray[0] = p;
       }
       public String getClassName() { return className; }
       public String getSession() { return session; }
       public String getMethodName() { return methodName; }
       public String getObjref() { return objref; }
       public Object[] getParamArray() { return paramArray; }

       /**
       * @return An array of Class objects for the parameters of the remote method
       */
       public Class[] getParams() {
                Class[] c = new Class[paramArray.length];
                for(int i = 0; i < paramArray.length; i++)
                        c[i] = paramArray[i].getClass();
                return c;
       }
       /**
       * the first arg in param array is a packet and we have a non-packet
       * we are trying to call, unwrap it
       */
       public void unwindPacket() throws PowerSpaceException {
                Packet p = (Packet)paramArray[0];
                paramArray = new Object[p.getNumberFields()];
                try {
                for(int i = 0 ; i < paramArray.length ; i++ ) {
                        paramArray[i] = p.getField(i).value();
                }
                } catch(Exception e) { throw new PowerSpaceException(e.getMessage()); }
       }

       public String toString() { return "<Method call transport> Session: "+
                session+" Class: "+className+" Method: "+methodName+" Arg: "+
                (paramArray == null || paramArray.length == 0 ? "nil" :
                (paramArray[0] == null ? "NULL PARAM!" : paramArray[0])); }

}
