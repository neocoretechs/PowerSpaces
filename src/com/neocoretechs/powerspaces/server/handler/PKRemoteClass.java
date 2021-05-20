package com.neocoretechs.powerspaces.server.handler;
import com.neocoretechs.powerspaces.*;
import com.neocoretechs.powerspaces.server.*;
import java.util.*;
import java.io.*;
import java.net.*;
/**
* PKRemoteClass - provides the
* class booter process interface.
* @author Groff (C) NeoCoreTechs 1999,2000,2014
*/
public final class PKRemoteClass {
        /**
        * Retrieve the PKMethodNamesAndParams for a class.
        * Create a PKServerInvokeMethod class and put in hashtable with class name
        */
        public static Object PowerKernel_getRemoteClass(Integer leg, CustomerConnectionPanel ccp, String tclassName) throws PowerSpaceException, FinishedException
	{
                try {
                        
                        PKServerInvokeMethod pksim = (PKServerInvokeMethod)(PowerPlant.handlerClasses.get(tclassName));
                        // attempt to loadif not there
                        if( pksim == null ) {
                                pksim = new PKServerInvokeMethod(tclassName, true);
                                PowerPlant.handlerClasses.put(tclassName, pksim);
                        }
                        return pksim.getPKMethodNamesAndParams();
                } catch(ClassNotFoundException cnfe) {
                        return cnfe;
                }
	}
        /**
        * Remove a session
        */
        public static Object PowerKernel_removeSession(Integer leg, CustomerConnectionPanel ccp) throws Exception
	{
                ConnectionPanel.CustomerConnectionPanelTable.remove(ccp.getSession());
                return "Ok";
	}
}

