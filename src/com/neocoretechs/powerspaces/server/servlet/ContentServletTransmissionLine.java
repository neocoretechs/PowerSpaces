package com.neocoretechs.powerspaces.server.servlet;
import com.neocoretechs.powerspaces.*;
import com.neocoretechs.powerspaces.server.ConnectionPanel;
import com.neocoretechs.powerspaces.server.CustomerConnectionPanel;
import com.neocoretechs.powerspaces.server.PowerPlant;

import java.net.*;
import java.io.*;
import java.util.*;

import javax.servlet.http.*;
/**
* A Content Servlet transmission line represents a connection to a browser
* and runs as a Servlet passing content via HTTP.
* @author Groff Copyright (C) NeoCoreTechs, Inc. 2001
*/
public final class ContentServletTransmissionLine {
        private String Session;
        HttpServletRequest request;
        HttpServletResponse response;
        boolean compression = false;
        //
        public ContentServletTransmissionLine(String tSession, HttpServletRequest trequest, HttpServletResponse tresponse) {
                Session = tSession;
                request = trequest;
                response = tresponse;
                //
                try {
                  System.out.println(new Date().toString()+" ContentServletTransmissionLine Connect started "+Session);
		  //
                  // Create customer connection panel
                  //
                  CustomerConnectionPanel Clink;
                  Clink = (CustomerConnectionPanel)(ConnectionPanel.CustomerConnectionPanelTable.get(Session));
                  // has an entry been made
                  if( Clink == null ) {
                        Clink = new CustomerConnectionPanel(Session, -1);
                        ConnectionPanel.CustomerConnectionPanelTable.put(Session,Clink);
                        Clink.properties.put("ContentServlet",this);
                  }
                  //
                  startFlow(Session, Clink);
                  //
                } catch(Exception e) {
                        System.out.println(new Date().toString()+" ContentServletTransmissionLine EXCEPTION LOGGED in Customer Connect for "+Session);
                        System.err.println(e.getMessage());
                        e.printStackTrace();
                }
	}

        public InputStream getInputStream() throws IOException
        {
                return request.getInputStream();
        }

        public OutputStream getOutputStream() throws IOException
        {
                return response.getOutputStream();
        }

        public void setRequest(HttpServletRequest trequest) {
                request = trequest;
        }
        public void setResponse(HttpServletResponse tresponse) {
                response = tresponse;
        }

        /**
        * the leg is the node 0 - parent, 1 - left, 2 - right
        */
        public int getLeg() { return -1; }


        public void startFlow(String Session, CustomerConnectionPanel ccp) {
                //
                Object pc,o=null;
                try {
//                  System.out.println(new Date().toString()+" ContentServletTransmissionLine.startFlow getting input stream from "+Session);
                  // trap for EOFException, if so remove and abort
                  // all customer connection panels.  Means they
                  // disconnected...
                  Object[] argParams = null; // our final arg array
                  String cName = null;
                  String cMethod = null;
                  String cParam = null;
                  String[] values;
                  //
                  Enumeration enums = request.getParameterNames();
                  // first should be class
                  try {
                        //
                        int numArgs = 0;
                        while(enums.hasMoreElements()) {
                                cParam = (String)enums.nextElement();
                                if( cParam.equals("class") ) {
                                        values = request.getParameterValues(cParam);
                                        cName = values[0];
                                } else
                                        if( cParam.equals("method") ) {
                                                values = request.getParameterValues(cParam);
                                                cMethod = values[0];
                                        } else
                                                ++numArgs;
                        }

                        if( cName == null )
                                throw new Exception("Class name missing as required parameter");
                        if( cMethod == null )
                                throw new Exception("Method name missing as required parameter");

                        // form our call array
                        argParams = new Object[numArgs];
                        for(int i = 0; i < numArgs; i++) {
                                cParam = (String)("param"+i);
                                values = request.getParameterValues(cParam);
                                // if just one String, don't pass array
                                if( values.length == 1 )
                                        argParams[i] = values[0];
                                else
                                        argParams[i] = values;
                        }
                  } catch(Exception e) {
                        System.out.println(e);
                        response.setContentType("text/plain");
                        PrintWriter out = response.getWriter();
                        out.println(e.getMessage());
                        e.printStackTrace(out);
                        return;
                  }
                  // make the method call transport
                  PKTransportMethodCall pktmc = new PKTransportMethodCall(ccp.getSession(), cName, "null", cMethod, argParams);
                  //
                  System.out.println(new Date().toString()+" ContentServletTransmissionLine.startFlow from "+ccp.getSession()+" calling "+cName+"."+cMethod);
                  // get a command a goin' and send result back
                  try {
                        pc = PowerPlant.command(getLeg(), ccp, pktmc);
                  } catch(Exception e) {
                        System.out.println(new Date().toString()+" ContentServletTransmissionLine EXCEPTION "+e+" from "+cName+"."+cMethod);
                        e.printStackTrace();
                        response.setContentType("text/plain");
                        PrintWriter out = response.getWriter();
                        out.println(e.getMessage());
                        e.printStackTrace(out);
                        return;
                  }
                  // send status packet
//                  System.out.println("ContentServletTransmissionLine attempt terminal return packet "+pc);
                  // this will queue to leg 3, resultPacket table
                  // if return is null (void type from method.invoke), we wait
                  // for an asynchronous command return (i.e. we don't queue)
                  if( pc != null ) {
                        ccp.queuePacket(pc);
                  }
//                        System.out.println("ContentServletTransmissionLine: Return Packet "+pc+" queued to session "+ccp.getSession());
//                  } else
//                        System.out.println("ContentServletTransmissionLine: Asynchronous wait for session "+ccp.getSession());

                  // test - wait for result
                  // next line till close() are "Collect" command
                  /**
                  * collect, get result packets from queue
                  */
                        synchronized(ccp.resultPackets) {
                                while( ccp.resultPackets.size() == 0 ) ccp.resultPackets.wait();
                                o = ccp.resultPackets.elementAt(0);
                                ccp.resultPackets.removeElementAt(0);
                         }
                         //
                         // start the write
                         //
//                         System.out.println("ContentServletTransmissionLine: attempt result to client "+ccp.getSession());
                         //
                         ContentTypeInterface cti = (ContentTypeInterface)o;
                         response.setStatus(HttpServletResponse.SC_CREATED);
                         response.setContentType(cti.getContentType());
                         response.setContentLength(cti.getBytes().length);
                         //PrintWriter out = response.getWriter();
                         OutputStream os = getOutputStream();
                         os.write(cti.getBytes());
//                         System.out.println("ContentServletTransmissionLine Wrote "+String.valueOf(cti.getBytes().length)+" to "+ccp.getSession());
		} // try
		 catch(EOFException eofe) {
                System.out.println(new Date().toString()+" Customer "+ccp.getSession()+" disconnected due to comm. Exception");
                // should send broadcast to cluster to clean up all CustomerConnectionPanels
                ConnectionPanel.removeCustomer(Session);
				return;
		 }
         catch(SocketException se) {
                System.out.println(new Date().toString()+" Customer "+ccp.getSession()+" disconnected due to comm. Exception");
                // should send broadcast to cluster to clean up all CustomerConnectionPanels
                ConnectionPanel.removeCustomer(Session);
				return;
		 }
         catch(Exception e) { 
                System.out.println(new Date().toString()+" ContentServletTransmissionLine EXCEPTION LOGGED !"+e); 
				System.err.println(e.getMessage()); e.printStackTrace(); 
		 }
       }

}
