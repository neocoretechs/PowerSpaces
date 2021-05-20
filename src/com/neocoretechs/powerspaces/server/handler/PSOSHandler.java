/**
* PSOSHandler - provides the OS process interface. A funky but necessary and useful thing for cluster provisioning
* and monitoring. Somewhat unix specific at this point
* @author Groff (C) NeoCoreTechs 1999,2000,2014
*/
package com.neocoretechs.powerspaces.server.handler;
import com.neocoretechs.powerspaces.*;
import com.neocoretechs.powerspaces.server.*;
import java.io.*;
import java.net.*;

public class PSOSHandler {
        //
        private static String lhost; // local host name to return with command results
        // Vector of Process input streams associated with execution 
        // for each CustomerConnectionPanel
        private static BufferedReader cis; 
        //
        /**
        * Send the OS a command, start process going
        */
        public static synchronized Packet PowerKernel_OSCommand( Integer leg, CustomerConnectionPanel ccp, Object args) throws PowerSpaceException, FinishedException {
		Process pos;              
		String arg = (String)((Packet)(args)).getField(0).value();
                if( !arg.equals("ps -ef") ) return new Packet("No");
		try {
			pos = Runtime.getRuntime().exec(arg);
		} catch(IOException ioe) { return new Packet(ioe); }
		try {
		cis = new BufferedReader(new InputStreamReader(pos.getInputStream(),System.getProperty("file.encoding")) );
		} catch(Exception e) { return new Packet(e); }
		// set the CustomerConnectionPanel property
		ccp.properties.put("cis", cis);
		return new Packet("OK");
        }
        /**
        * Send the OS a command, start process going
        */
        public static synchronized Packet PowerKernel_OSCommandReturn( Integer leg, CustomerConnectionPanel ccp, Object args) throws PowerSpaceException, FinishedException {
		try {
			try {
				if( lhost == null ) lhost = InetAddress.getLocalHost().getHostName()+":";
            } catch(IOException ioe) { lhost = "unknown host:"; }
		 	cis = (BufferedReader)(ccp.properties.get("cis"));	
			if( cis == null ) return new Packet(new Exception("No OSCommand issued before OSCommandReturn"),lhost);
			String cmdReturn = cis.readLine();
			if( cmdReturn == null )
				throw new FinishedException();
			return new Packet(lhost+cmdReturn,(Packet)args);
			// no more, send ReduceSynch
//			if( ConnectionPanel.isParent) {
//				ConnectionPanel.forwardCmd( ConnectionPanel.getParentPowerPlant(),
//                                              ccp, session, new Packet("com.neocoretechs.powerspaces.server.PKParallel","ReduceSynch"));
//			}
		} catch(IOException ioe) { return new Packet(ioe,lhost); }
	}
}

