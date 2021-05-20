package com.neocoretechs.powerspaces;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
/**
* Method names and parameters for a remote "handler" class. One per class.
* Passed to client on PKRemoteObject creation.  This will contain the
* methods to be advertised to the rest of the world.  A call
* from remote client will verify the method before remote call
* @author Groff Copyright (C) NeoCoreTechs, Inc. 1998-2000
*/
public final class PKMethodNamesAndParams implements Serializable {
       static final long serialVersionUID = 8837760295724028863L;
       public transient Class classClass;
       public String className;
       public transient Vector methodNames = new Vector();
       public transient Class[][] methodParams;
       public String[] methodSigs;
       public transient Class[] returnTypes;

       /**
       * No arg ctor means we deserialized
       */
       public PKMethodNamesAndParams() {}

       public String[] getMethodSigs() { return methodSigs; }

       public Class[] getReturnTypes() { return returnTypes; }

       public Vector getMethodNames() { return methodNames; }

}
