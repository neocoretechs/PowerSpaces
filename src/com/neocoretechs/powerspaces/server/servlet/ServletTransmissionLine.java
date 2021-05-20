package com.neocoretechs.powerspaces.server.servlet;
import com.neocoretechs.powerspaces.*;
import com.neocoretechs.powerspaces.server.ConnectionPanel;
import com.neocoretechs.powerspaces.server.CustomerConnectionPanel;
import com.neocoretechs.powerspaces.server.PSObjectInputStream;
import com.neocoretechs.powerspaces.server.PowerPlant;
import com.neocoretechs.powerspaces.server.handler.*;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

import javax.servlet.*;
import javax.servlet.http.*;
/**
* A Servlet transmission line represents a connection to a client
* and runs as a Servlet passing serialized objects via HTTP.
* @author Groff Copyright (C) NeoCoreTechs, Inc. 1998-2000
*/
public final class ServletTransmissionLine {
        private String Session;
        HttpServletRequest request;
        HttpServletResponse response;
        boolean compression = false;
        //
        public ServletTransmissionLine(String tSession, HttpServletRequest trequest, HttpServletResponse tresponse) {
                Session = tSession;
                request = trequest;
                response = tresponse;
                //
                try {
                  System.out.println(new Date().toString()+" ServletTransmissionLine Connect started "+Session);
		  //
                  // Create customer connection panel
                  //
                  CustomerConnectionPanel Clink;
                  Clink = (CustomerConnectionPanel)(ConnectionPanel.CustomerConnectionPanelTable.get(Session));
                  // has an entry been made
                  if( Clink == null ) {
                        Clink = new CustomerConnectionPanel(Session, -1);
                        ConnectionPanel.CustomerConnectionPanelTable.put(Session,Clink);
                        Clink.properties.put("Servlet",this);
                  }
                  //
                  startFlow(Session, Clink);
                  //
                } catch(Exception e) { System.out.println("! EXCEPTION LOGGED in Customer Connect!"); System.err.println(e.getMessage()); e.printStackTrace(); }
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
                try {
                  ObjectInputStream obin;
                  Object o;
                  Object p, pc = null;
//                  System.out.println("ServletTransmissionLine.startFlow getting input stream from "+Session);
                  // trap for EOFException, if so remove and abort
                  // all customer connection panels.  Means they
                  // disconnected...
                  synchronized(this) {
                        obin = new PSObjectInputStream(getInputStream(), PowerPlant.theClassLoader);
                        o = obin.readObject();
                  }
                  //
                  System.out.println(new Date().toString()+" ServletTransmissionLine.startFlow from "+ccp.getSession()+" calling "+o.toString());
                  // get a command a goin' and send result back
                  try {
                        pc = PowerPlant.command(getLeg(), ccp, (PKTransportMethodCall)o);
                  } catch(Exception e) {
                        System.out.println(new Date().toString()+" ServletTransmissionLine EXCEPTION for session "+ccp.getSession()+": "+e);
                        e.printStackTrace();
                        pc = e;
                  }
                  // send status packet
//                  System.out.println("ServletTransmissionLine attempt terminal return packet "+pc);
                  // this will queue to leg 3, resultPacket table
                  // if return is null (void type from method.invoke), we wait
                  // for an asynchronous command return (i.e. we don't queue)
                  if( pc != null ) {
                        ccp.queuePacket(pc);
                  }
//                        System.out.println("ServletTransmissionLine: Return Packet "+pc+" queued to session "+ccp.getSession());
//                  } else
//                        System.out.println("ServletTransmissionLine: Asynchronous wait for session "+ccp.getSession());

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
//                         System.out.println("ServletTransmissionLine: attempt result "+o+" to client "+ccp.getSession());
                         ByteArrayOutputStream baos = new ByteArrayOutputStream();
                         ObjectOutputStream oi = new ObjectOutputStream(baos);
                         //
//                         System.out.println(o.getClass().getName());
                         oi.writeObject(o);
                         oi.flush();
                         baos.flush();
                         //
                         byte[] bytes = baos.toByteArray();
                         oi.close();
                         baos.close();
                         //
                         if( compression ) {
                                System.out.println("Zipping "+String.valueOf(bytes.length));
                                //
                                ByteArrayOutputStream zout = new ByteArrayOutputStream();
                                ZipOutputStream zipStream = new ZipOutputStream(zout);
                                zipStream.setMethod(ZipOutputStream.DEFLATED);
                                zipStream.setLevel(9);  // the best and slowest
                                ZipEntry entry = new ZipEntry(String.valueOf(bytes.length));
                                zipStream.putNextEntry(entry);
                                zipStream.write(bytes, 0, bytes.length);
                                zipStream.closeEntry();
                                zipStream.close();
                                zout.flush();
                                // put deflated bytes to net out
                                bytes = zout.toByteArray();
                                zout.close();
                         }
                         response.setContentLength(bytes.length);
                         OutputStream os = getOutputStream();
                         os.write(bytes);
                         os.flush();
//                         System.out.println("servletTransmissionLine Wrote "+String.valueOf(bytes.length)+" to "+ccp.getSession());
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
                                System.out.println(new Date().toString()+" ServletTransmissionLine EXCEPTION LOGGED !"); 
				System.err.println(e.getMessage()); e.printStackTrace(); 
		 }
			//
       }

}
