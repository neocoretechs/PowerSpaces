package com.neocoretechs.powerspaces.server;
import com.neocoretechs.powerspaces.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
/**
* This class handles reflection of the user "handlers" for designated methods,
* populates a table of those methods, creates a method call transport for client,
* and provides for server-side invocation of those methods.<p>
* It is important to note that the leg and CustomerConnectionPanel will be prepended
* to calls to handlers, and be passed as the first two arguments to any user-written handler.
* @author Groff Copyright (C) NeoCoreTechs 1998-2000
*/
public final class PKServerInvokeMethod {

       protected boolean skipArgs;
       int skipArgIndex;
       private Method[] methods;
       private PKMethodNamesAndParams pkmnap = new PKMethodNamesAndParams();

       public PKMethodNamesAndParams getPKMethodNamesAndParams() { return pkmnap; }
       /**
       * This constructor populates this object with reflected methods from the
       * designated class.  Reflect heirarchy in reverse (to get proper
       * overload) and look for methods with the "PowerKernel_" signature
       * @param tclass The class name we are targeting
       * @param skipArgs true if we want to skip first 2 (or whatever) PowerKernel args.
       */
       public PKServerInvokeMethod(String tclass, boolean tskipArgs) throws ClassNotFoundException {
                
                //pkmnap.classClass = Class.forName(tclass);
                pkmnap.classClass = PowerPlant.theClassLoader.loadClass(tclass, true);
                pkmnap.className = pkmnap.classClass.getName();
                skipArgs = tskipArgs;
                skipArgIndex = (skipArgs ? 2 : 0);
                Method m[];
                m = pkmnap.classClass.getMethods();
                for(int i = m.length-1; i >= 0 ; i--) {
                        if( m[i].getName().startsWith("PowerKernel_") ) {
                                pkmnap.methodNames.addElement(m[i].getName().substring(12));
                                System.out.println("Method :"+m[i].getName().substring(12));
                        }
                }
                // create arrays
                methods = new Method[pkmnap.methodNames.size()];
                pkmnap.methodParams = new Class[pkmnap.methodNames.size()][];
                pkmnap.methodSigs = new String[pkmnap.methodNames.size()];
                pkmnap.returnTypes = new Class[pkmnap.methodNames.size()];
                int methCnt = 0;
                //
                for(int i = m.length-1; i >= 0 ; i--) {
                        if( m[i].getName().startsWith("PowerKernel_") ) {
                                pkmnap.methodParams[methCnt] = m[i].getParameterTypes();
                                pkmnap.methodSigs[methCnt] = m[i].toString();
                                pkmnap.returnTypes[methCnt] = m[i].getReturnType();
                                if( pkmnap.returnTypes[methCnt] == void.class ) pkmnap.returnTypes[methCnt] = Void.class;
                                int ind1 = pkmnap.methodSigs[methCnt].indexOf("PowerKernel_");
                                pkmnap.methodSigs[methCnt] = pkmnap.methodSigs[methCnt].substring(0,ind1)+pkmnap.methodSigs[methCnt].substring(ind1+12);
                                if( skipArgs ) {
                                   try {
                                        ind1 = pkmnap.methodSigs[methCnt].indexOf("(");
                                        int ind2 = pkmnap.methodSigs[methCnt].indexOf(",",ind1);
                                        ind2 = pkmnap.methodSigs[methCnt].indexOf(",",ind2+1);
                                        ind2 = pkmnap.methodSigs[methCnt].indexOf(",",ind2+1);
                                        pkmnap.methodSigs[methCnt] = pkmnap.methodSigs[methCnt].substring(0,ind1+1)+pkmnap.methodSigs[methCnt].substring(ind2+1);
                                   } catch(StringIndexOutOfBoundsException sioobe) {
                                        System.out.println("<<PowerKernel: The method "+pkmnap.methodSigs[methCnt]+" contains too few arguments (first "+skipArgIndex+" skipped), bypassing it..");
                                   }
                                }
                                methods[methCnt++] = m[i];
                        }
                }
       }

       /**
       * Make a PKTransportMethodCall from the passed CustomerConnectionPanel
       * and Packet.
       * @param ccp The CustomerConnectionPanel of the calling party
       * @param pc The Packet passed to the method which will be unwound into separate args if actual arg is not Packet.
       * @return PKTransportMethodCall of the newly formed method call
       * @throw PowerSpaceException if the number of arguments to the method does not match Packet field count
       */
       public static PKTransportMethodCall makeMethodCall(CustomerConnectionPanel ccp, Packet pc) throws PowerSpaceException {
                switch(pc.getNumberFields()) {
                        case 2:
                        return new PKTransportMethodCall(ccp.getSession(),
                                     (String)(pc.getField(0).value()),
                                     "xx", (String)(pc.getField(1).value()) );
                        case 3:
                        return new PKTransportMethodCall(ccp.getSession(),
                                     (String)(pc.getField(0).value()),
                                     "xx", (String)(pc.getField(1).value()),
                                     (Packet)(pc.getField(2).value()) );
                        // here we presumably have something at head of Packet
                        // (such as messagerouting field and method stuff below
                        case 4:
                        return new PKTransportMethodCall(ccp.getSession(),
                                     (String)(pc.getField(1).value()),
                                     "xx", (String)(pc.getField(2).value()),
                                     (Packet)(pc.getField(3).value()) );
                }
                throw new PowerSpaceException("makeMethodCall: Wrong number of fields in Packet "+pc);
       }
       /**
       * For an incoming PKTransportMethodCall, verify and invoke the proper
       * method.  We assume there is a table of class names and this and
       * it has been used to locate this object.  In this case, we may be dealing
       * with a Packet transport, so we unwind it of we need to to call the
       * method with params from packet...we have a single arg of type packet in transport
       * and we are not dealing with a single arg of type packet in invokee
       * @param leg The PowerPlant leg of request origin
       * @param ccp The CustomerConnectionPanel of the calling party
       * @param tmc The PKTransportMethodCall of the method to be called
       * @return Object of result of method invocation
       */
       public Object invokeMethod(Integer leg, CustomerConnectionPanel ccp, PKTransportMethodCall tmc) throws Exception {
                //NoSuchMethodException, InvocationTargetException, IllegalAccessException, PowerSpaceException  {
                
                String targetMethod = tmc.getMethodName();
                int methodIndex = pkmnap.methodNames.indexOf(targetMethod);
                String whyNotFound = "No such method";
                while( methodIndex != -1 && methodIndex < pkmnap.methodNames.size()) {
                //        System.out.println(jj);
                        Class[] params = tmc.getParams();
                        // check each param against method sig
                        // if arg of type packet and not dealing with corresponding
                        // arg of type packet, unwind packet in call and try
                        if( params.length > 0 &&
                            params[0].isAssignableFrom(Packet.class) &&
                            !(pkmnap.methodParams[methodIndex][skipArgIndex].isAssignableFrom(Packet.class)) ) {
                                tmc.unwindPacket();
                                params = tmc.getParams();
                        }
                        //
                        //
//                        for(int iparm1 = 0; iparm1 < params.length ; iparm1++) {        
//                                System.out.println("Calling param: "+params[iparm1]);
//                        }
//                        for(int iparm2 = skipArgIndex ; iparm2 < pkmnap.methodParams[methodIndex].length; iparm2++) {
//                                System.out.println("Method param: "+pkmnap.methodParams[methodIndex][iparm2]);
//                        }
                        //
                        //
                        if( params.length == pkmnap.methodParams[methodIndex].length-skipArgIndex ) {
                                boolean found = true;
                                // if skipArgs, don't compare first 2
                                for(int paramIndex = 0 ; paramIndex < params.length; paramIndex++) {
                                        // can we cast it?
                                        if( params[paramIndex] != null && !pkmnap.methodParams[methodIndex][paramIndex+skipArgIndex].isAssignableFrom(params[paramIndex]) ) {
                                                found = false;
                                                whyNotFound = "Parameters do not match";
                                                break;
                                        }
                                }
                                if( found ) {
                                        if( skipArgs ) {
                                                Object o1[] = tmc.getParamArray();
                                                // pack first 2 args to beginning
                                                Object o2[] = new Object[o1.length+skipArgIndex];
                                                o2[0] = (Object)leg;
                                                o2[1] = (Object)ccp;
                                                for(int i1 = 0; i1 < o1.length; i1++)
                                                        o2[skipArgIndex+i1] = o1[i1];
//                                                return methods[methodIndex].invoke( PKObjectTable.getObject(tmc.getSession(), tmc.getObjref()), o2 );
                                                return methods[methodIndex].invoke( null, o2 );
                                        } 
//                                        return methods[methodIndex].invoke( PKObjectTable.getObject(tmc.getSession(), tmc.getObjref()),tmc.getParamArray() );
                                        // invoke it for return
                                        return methods[methodIndex].invoke( null, tmc.getParamArray() );
                               }
                        } else
                               // tag for later if we find nothing matching
                               whyNotFound = "Wrong number of parameters";
                        methodIndex = pkmnap.methodNames.indexOf(targetMethod,methodIndex+1);
                }
                throw new NoSuchMethodException("Method "+targetMethod+" not found in "+pkmnap.className+" "+whyNotFound);
        }

}

