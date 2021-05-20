package com.neocoretechs.powerspaces.server;
import com.neocoretechs.powerspaces.server.handler.*;
import java.io.*;
public class PSObjectInputStream extends ObjectInputStream {
        ClassLoader cl;
        public PSObjectInputStream(InputStream in, ClassLoader tcl) throws Exception
        {
                super(in);
                cl = tcl;
        }
        public synchronized Class resolveClass(ObjectStreamClass v) throws ClassNotFoundException {
                return cl.loadClass(v.getName());
        }
}
