package com.neocoretechs.powerspaces.server;
import com.neocoretechs.powerspaces.server.handler.*;
import java.io.*;
public class PSObjectOutputStream extends ObjectOutputStream {
        ClassLoader cl;
        public PSObjectOutputStream(OutputStream in, ClassLoader tcl) throws Exception
        {
                super(in);
                cl = tcl;
        }
        public synchronized Class resolveClass(ObjectStreamClass v) throws ClassNotFoundException {
                return cl.loadClass(v.getName());
        }
}
