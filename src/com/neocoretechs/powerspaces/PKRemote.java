package com.neocoretechs.powerspaces;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.util.zip.*;

/**
* PKRemote object creation connects to a PowerSpace from the client
* through a PowerSpace object and retrieves a PKMethodNamesAndParams
* of the class passed in the constructor for the purpose of remotely invoking
* methods on that class using PKTransportMethodCall.
* @see PowerSpace
* @see PKMethodNamesAndParams
* @see PKTransportMethodCall
* @author Groff Copyright (C) NeoCoreTechs 1998,2000 
*/
public final class PKRemote {

       private PKMethodNamesAndParams pkmnap;
       PowerSpace ps;
       ObjectOutputStream oos;
       ObjectInputStream ois;
       Object oin;
       byte[] sobytes;
       boolean compression = false;

       public synchronized PKMethodNamesAndParams getPKMethodNamesAndParams() { return pkmnap; }
       /**
       */
       public PKRemote(PowerSpace tps, String tclass) throws Exception {
                this(tps, tclass, false);
       }

       public PKRemote(PowerSpace tps, String tclass, boolean discover) throws Exception {
                ps = tps;
                if( discover ) {
                        Object[] argPack = new Object[1];
                        argPack[0] = tclass;
                        PKTransportMethodCall p = new PKTransportMethodCall("SYSTEM", "com.neocoretechs.powerspaces.server.handler.PKRemoteClass", "null", "getRemoteClass", argPack);
                        sendReceive(p);
//                      System.out.println("PKRemote sendReceive: "+oin);
                        if( oin instanceof Exception )
                                throw (Exception)(oin);
                        pkmnap = (PKMethodNamesAndParams)(oin);
                } else {
                        pkmnap = new PKMethodNamesAndParams();
                        pkmnap.className = tclass;
                }
       }

       private synchronized void sendReceive(Object p) throws PowerSpaceException {
                try {
                        ps.getConnection();
                        oos = new ObjectOutputStream(ps.getOutputStream());
                        oos.writeObject(p);
                        oos.flush();
                        //
                        if( ps.isHttp ) {
                                int cl = ps.getContentLength();
                                InputStream is = ps.getInputStream();
                                sobytes = new byte[cl];
                                int clr = 0;
                                while( clr < cl ) {
                                        clr += is.read(sobytes, clr, cl-clr);
//                                        System.out.println("READ: "+clr);
                                }
                        } else {
                              ois = new ObjectInputStream(ps.getInputStream());
                              if( compression )
                                sobytes = (byte[])ois.readObject();
                              // if no compression, read the object and return
                              else {
                                oin = ois.readObject();
                                return;
                              }
                        }
                        //
                        byte[] obytes = null;
                        //
                        if( compression) {
                                System.out.println("Compressed size: "+String.valueOf(sobytes == null ? 0 : sobytes.length));
                                ZipEntry entry;
                                // now unzipped bytes ready for deserialization
                                ZipInputStream zipFile = new ZipInputStream(new ByteArrayInputStream(sobytes));
                                entry = zipFile.getNextEntry();
                                int esiz = Integer.valueOf(entry.getName()).intValue();
                                System.out.println("Entry "+String.valueOf(esiz));
                                obytes = new byte[esiz];
                                int ic = 0;
                                while( ic < esiz ) {
                                        ic += zipFile.read(obytes, ic, esiz-ic);
//                        System.out.println("zip read "+String.valueOf(ic));
                                }
                                System.out.println("Uncompressed size: "+String.valueOf(ic));
                                zipFile.close();
                        // now have deflated bytes ready for deserialization
                        } else
                                obytes = sobytes;

                        ObjectInputStream oois = new ObjectInputStream(new ByteArrayInputStream(obytes));
                        oin = oois.readObject();
//                        System.out.println("PKRemote SendReceive read "+oin);
                } catch(Exception e) {
                        System.out.println(e);
                        e.printStackTrace();
                        throw new PowerSpaceException(e.getMessage());
                }
       }

       /**
       * Invocation with array which is wound
       * 
       */
       public synchronized Object invoke(String tmethod, Object[] argPack) throws Exception {
                PKTransportMethodCall p = new PKTransportMethodCall("SYSTEM", pkmnap.className, "null", tmethod, new Object[]{argPack});
                sendReceive(p);
                if( oin instanceof Exception ) {
                        System.out.println("Remote Exception:");
                        throw (Exception)oin;
                }
                return oin;
       }
       /**
       * Invocation with your own array of parameters
       * verses an array of objects to be unwound
       */
       public synchronized Object invokeWithParamArray(String tmethod, Object[] argPack) throws Exception {
                PKTransportMethodCall p = new PKTransportMethodCall("SYSTEM", pkmnap.className, "null", tmethod, argPack);
                sendReceive(p);
                if( oin instanceof Exception ) {
                        System.out.println("Remote Exception:");
                        throw (Exception)oin;
                }
                return oin;
       }

       public synchronized Object invoke(String tmethod, Object argPack) throws Exception {
                PKTransportMethodCall p;
                if( argPack instanceof Packet)
                        p = new PKTransportMethodCall("SYSTEM", pkmnap.className, "null", tmethod, (Packet)(argPack));
                else
                        p = new PKTransportMethodCall("SYSTEM", pkmnap.className, "null", tmethod, new Object[]{argPack});                        
                sendReceive(p);
                if( oin instanceof Exception ) {
                        System.out.println("Remote Exception:");
                        throw (Exception)oin;
                }
                return oin;
       }

       public synchronized Object invoke(String tmethod, Object arg1, Object arg2) throws Exception {
                PKTransportMethodCall p;
                p = new PKTransportMethodCall("SYSTEM", pkmnap.className, "null", tmethod, new Object[]{arg1,arg2});                        
                sendReceive(p);
                if( oin instanceof Exception ) {
                        System.out.println("Remote Exception:");
                        throw (Exception)oin;
                }
                return oin;
       }

       public synchronized Object invoke(String tmethod, Object arg1, Object arg2, Object arg3) throws Exception {
                PKTransportMethodCall p;
                p = new PKTransportMethodCall("SYSTEM", pkmnap.className, "null", tmethod, new Object[]{arg1,arg2,arg3});                        
                sendReceive(p);
                if( oin instanceof Exception ) {
                        System.out.println("Remote Exception:");
                        throw (Exception)oin;
                }
                return oin;
       }

       public synchronized Object invoke(String tmethod, Object arg1, Object arg2, Object arg3, Object arg4) throws Exception {
                PKTransportMethodCall p;
                p = new PKTransportMethodCall("SYSTEM", pkmnap.className, "null", tmethod, new Object[]{arg1,arg2,arg3,arg4});                        
                sendReceive(p);
                if( oin instanceof Exception ) {
                        System.out.println("Remote Exception:");
                        throw (Exception)oin;
                }
                return oin;
       }

       public synchronized Object invoke(String tmethod, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) throws Exception {
                PKTransportMethodCall p;
                p = new PKTransportMethodCall("SYSTEM", pkmnap.className, "null", tmethod, new Object[]{arg1,arg2,arg3,arg4,arg5});                        
                sendReceive(p);
                if( oin instanceof Exception ) {
                        System.out.println("Remote Exception:");
                        throw (Exception)oin;
                }
                return oin;
       }

       public synchronized Object invoke(String tmethod, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) throws Exception {
                PKTransportMethodCall p;
                p = new PKTransportMethodCall("SYSTEM", pkmnap.className, "null", tmethod, new Object[]{arg1,arg2,arg3,arg4,arg5,arg6});                        
                sendReceive(p);
                if( oin instanceof Exception ) {
                        System.out.println("Remote Exception:");
                        throw (Exception)oin;
                }
                return oin;
       }

       public synchronized Object invoke(String tmethod, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) throws Exception {
                PKTransportMethodCall p;
                p = new PKTransportMethodCall("SYSTEM", pkmnap.className, "null", tmethod, new Object[]{arg1,arg2,arg3,arg4,arg5,arg6,arg7});                        
                sendReceive(p);
                if( oin instanceof Exception ) {
                        System.out.println("Remote Exception:");
                        throw (Exception)oin;
                }
                return oin;
       }
       /**
       * No params invoke
       */
       public synchronized Object invoke(String tmethod) throws Exception {
                PKTransportMethodCall p = new PKTransportMethodCall("SYSTEM", pkmnap.className, "null", tmethod);
                sendReceive(p);
                if( oin instanceof Exception ) {
                        System.out.println("Remote Exception:");
                        throw (Exception)oin;
                }
                return oin;
       }
}
