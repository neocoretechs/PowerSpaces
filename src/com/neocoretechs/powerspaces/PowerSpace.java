package com.neocoretechs.powerspaces;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
/**
* PowerSpaces - This is the client side of PowerSpaces.  It provides
* the capability to retrieve remote classes and invoke their methods.
* It is also possible to upload bytecode to the server.  The remote PowerSpace
* is running in the servlet engine and objects are passed as serialized
* representations over HTTP.
* @author Groff Copyright (C) NeoCoreTechs, Inc. 1998-2000, All rights reserved.
*/
public class PowerSpace {
        ObjectOutputStream oos;
        ObjectInputStream ois;
        String sessionHeader;
        URL url;
        URLConnection URLC = null;
        Socket so = null;
        InputStream in;
        OutputStream out;
        public boolean isHttp = false;

        /**
        * Connect to PowerSpace at URL
        * @param server The URL of Web server running PowerSpace
        */
        public PowerSpace(String server) {
                try {
                url = new URL(server);
                isHttp = true;
                } catch(Exception e) {
                        System.out.println("Bad URL >> "+e.getMessage());
                        e.printStackTrace();
                }
	}

        /**
        * Connect to PowerSpace at URL with specifis session ID
        * @param server The URL of Web server running PowerSpace
        * @param session The session name within the PowerSpace
        */
        public PowerSpace(String server, String session) {
                try {
                url = new URL(server+"?"+session);
                isHttp = true;
                } catch(Exception e) {
                        System.out.println("Bad URL >> "+e.getMessage());
                        e.printStackTrace();
                }
	}

        /**
        * Connect to PowerSpace at host and port via socket
        */
        public PowerSpace(String server, int port) {
                try {
                so = new Socket(server, port);
                isHttp = false;
                } catch(Exception e) {
                        System.out.println("Bad Socket connect >> "+e.getMessage());
                        e.printStackTrace();
                }
        }
        /**
        * Set up HTTP connection with PowerSpace<p>
        * If sockets, we ignore
        */
        public synchronized void getConnection() throws IOException {
                if( isHttp ) {
                        URLC = url.openConnection();
                        URLC.setDoInput(true);
                        URLC.setDoOutput(true);
                        URLC.setUseCaches(false);
                        URLC.setRequestProperty("Content-type","java-internal");
//                        System.out.println("Connecting to "+URLC);
                }                        
        }
                
        public synchronized InputStream getInputStream() throws IOException {
                if( isHttp )
                  in = URLC.getInputStream();
                else
                  in = so.getInputStream();
                return in;
        }

        public synchronized OutputStream getOutputStream() throws IOException {
                if( isHttp )
                  out = URLC.getOutputStream();
                else
                  out = so.getOutputStream();
                return out;
        }

        public int getContentLength() { return URLC.getContentLength(); }

        /**
        * If HTTP, send a request for session cleanup<p>
        * otherwise force a socket exception on the server to remove panel
        */
        public synchronized void Unplug() throws Exception {
                if( isHttp )
                        removeSession();    
                else {
                        getConnection();
                        getInputStream().close();
                }
        }

//        public String makeSessionHeader() {
//                return psO.getSocket().getInetAddress().getHostAddress()+":"+
//                String.valueOf(psO.getSocket().getPort())+":"+this.toString().substring(this.toString().indexOf('@'));
//        }

        /**
        * Retrieve a remote class from PowerSpace
        * @param tclass The class name
        * @return The PKRemote object with class methods invokable from client
        */
        public synchronized PKRemote getRemote(String tclass) throws Exception {
                return new PKRemote(this, tclass);
        }

        /**
        * Private method to send PKTransportMethodCall to server and get
        * result (or Exception) back
        * @param pktmc The PKTransportMethodCall object to send
        * @return The result of call
        * @throw Exception for any sort of boom
        */
        private synchronized Object sendTransportGetResult(PKTransportMethodCall pktmc) throws Exception {
                        getConnection();
                        oos = new ObjectOutputStream(getOutputStream());
                        oos.writeObject(pktmc);
                        oos.flush();
                        oos.close();
                        ois = new ObjectInputStream(getInputStream());
                        Object op = ois.readObject();
                        ois.close();
                        if( op instanceof Exception ) {
                                System.out.println("Remote exception: "+((Exception)op));
                                ((Exception)op).printStackTrace();
                                throw (Exception)op;
			}
                        //
                        return op;
        }
        /**
        * Wait for result from asynchronous queue
        * This forces an asynchronous wait
        * at client until a new result packet arrives
        * @return The result of asynchronous remote computation
        */
        public synchronized Object collect() throws Exception {
                PKTransportMethodCall p = new PKTransportMethodCall("SYSTEM", "com.neocoretechs.powerspaces.server.handler.PSIPCHandler", "null", "Collect");
                return sendTransportGetResult(p);
        }

        /**
        * Collect the result of remote computations from asynchronous queue
        * Remote methods returning void will cause an asynchronous wait
        * at client until a result packet arrives
        * @return The result of asynchronous remote computation
        */
        public synchronized Object collectResult() throws Exception {
                PKTransportMethodCall p = new PKTransportMethodCall("SYSTEM", "com.neocoretechs.powerspaces.server.handler.PKParallel", "null", "Collect");
                return sendTransportGetResult(p);
        }

        /**
        * Disconnect a session, this being called from Unplug for Http
        */
        public synchronized Object removeSession() throws Exception {
                PKTransportMethodCall p = new PKTransportMethodCall("SYSTEM", "com.neocoretechs.powerspaces.server.handler.PKRemoteClass", "null", "removeSession");
                return sendTransportGetResult(p);
        }

        /**
        * Unwind a packet, since it comes back in "tree" form, we make
        * a nice linear Vector of it.
        */
        public synchronized static void unwindPacket(Packet p, Vector v) throws PowerSpaceException {
                for(int i = 0 ; i < p.numberOfFields() ; i++ )
                        if( p.getField(i).type() == Packet.class )
                                unwindPacket((Packet)(p.getField(i).value()), v);
                        else
                                v.addElement((Object)(p.getField(i).value()));
        }
        /**
        * Here we convert a JAR via name, to a byte array
        * @param jarFile the JAR name
        * @return The byte array read from JAR - image
        */
        public static byte[] jarToByte(String jarFile) {
            ByteArrayOutputStream bs = null;
            try {
                URL url = new URL(jarFile);
                URLConnection URLC = url.openConnection();
                InputStream i = URLC.getInputStream();
                bs = new ByteArrayOutputStream();
                byte[] buf = new byte[512];
                int nbytes = 0;
                while(nbytes != -1) {
                	nbytes = i.read(buf);
//					System.out.println("JAR read "+String.valueOf(nbytes));
                	if( nbytes != -1) {
                		bs.write(buf, 0, nbytes);
                	}
                }	
            } catch(Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
                return null;
            }
            return bs.toByteArray();
        }

    /**
    * Here we convert an array of classes to a byte JAR array
    * @param classes The class array
    * @return The byte array in JAR format
    * @throws IOException if a ZIP error occurs
    */
    public static byte[] classToByte(Class classes[]) throws IOException {
        String names[] = new String[classes.length];
        byte bytecodes[][] = new byte[classes.length][];
        for (int i = 0; i < classes.length; i++)
        {
            names[i] = classes[i].getName()+".class";
            bytecodes[i] = classToByte(classes[i]);
            if( bytecodes[i] == null ) System.out.println(names[i]+" bunk classToByte bytecode");
            else
            System.out.println("Bytecode len "+String.valueOf(bytecodes[i].length)+" for "+names[i]);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ZipOutputStream zipStream = new ZipOutputStream(out);
        for (int i = 0; i < names.length; i++)
        {
            ZipEntry entry = new ZipEntry(names[i]);
            zipStream.putNextEntry(entry);
            zipStream.write(bytecodes[i],0,bytecodes[i].length);
            zipStream.closeEntry();
        }
        zipStream.close();
        return out.toByteArray();
    }
    //

    /**
    * Read bytes from input stream
    * @param is The input stream
    * @return The array of bytes
    * @throws IOException if an I/O error occurs
    */
    private static final byte[] readBytes(InputStream is) throws IOException {
        int length = is.available();
        byte b[] = new byte[length];
        is.read(b, 0, length);
        is.close();
        return b;
    }

    /**
    * Here we convert a class to pure bytecode
    * @param classes The class
    * @return The byte array in bytecode format
    * @throws IOException if a ZIP error occurs
    */
    public static byte[] classToByte(Class tclass) throws IOException {
        String classPath = null;
        String pathSep = null;
        try {
            classPath = System.getProperty("java.class.path");
            pathSep = System.getProperty("path.separator");
        } catch (SecurityException e) {
            throw new IOException("CLASSPATH property undefined while loading: "+tclass.getName());
        }
        if (classPath == null)
            throw new IOException("CLASSPATH property undefined while loading: "+tclass.getName());
        String className = new StringBuffer(String.valueOf(tclass.getName().replace('.', File.separatorChar))).append(".class").toString();
        StringTokenizer st = new StringTokenizer(classPath, pathSep, false);
        while (st.hasMoreTokens())
        {
            String temp = st.nextToken();
            System.out.println(temp);
            if (temp.endsWith(".jar") || temp.endsWith(".zip")) {
                ZipFile zipFile = new ZipFile(temp);
                for (Enumeration entries = zipFile.entries(); entries.hasMoreElements(); ) {
                    ZipEntry entry = (ZipEntry)entries.nextElement();
                    String entryName = entry.getName().replace('/', File.separatorChar);
//                    System.out.println(entryName+" "+className);
                    if (entryName.equals(className)) {
                        System.out.println("reading bytes "+temp+" "+className);
                        return readBytes(zipFile.getInputStream(entry));
                    }
                }
            } else {
                File file = new File(temp + File.separator + className );
                System.out.println(temp + File.separator + className );
                if (file.exists())
                    return readBytes(new FileInputStream(file));
            }
        }
        return null;
    }
        
        
}
