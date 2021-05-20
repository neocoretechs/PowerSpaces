package com.neocoretechs.powerspaces.server;
import java.util.*;
import java.util.zip.*;
import java.net.*;
import java.io.*;
import com.neocoretechs.powerspaces.*;
/**
* PowerSpaces socket to I/O Pole class.
* A pole is an abstraction that defines IO for customer
* connections.  The startFlow method actually does the asychronous
* Packet transfer between PowerPlant queue and customer.
* @author Groff (C) NeoCoreTechs 1998
*/
public abstract class SocketToPlant implements PSCustomerInterface {
        //
        //
        public abstract PowerPlant getPowerPlant();
        public abstract Socket getSocket();
        public abstract void Connect();
        private boolean compression = false;
        //
        public SocketToPlant() {}
        /**
        * the leg is the node 0 - parent, 1 - left, 2 - right
        */
        public abstract int getLeg();
        public void startFlow(String Session, CustomerConnectionPanel ccp) {
                //
                try {
                for(;;) {
                  ObjectInputStream obin;
                  Object o;
                  Object p, pc = null;
//                  System.out.println("SocketTransmissionLine.startFlow getting input stream from "+Session);
                  // trap for EOFException, if so remove and abort
                  // all customer connection panels.  Means they
                  // disconnected...
                  synchronized(this) {
                        obin = new PSObjectInputStream(getSocket().getInputStream(), PowerPlant.theClassLoader);
                        o = obin.readObject();
                  }
                  //
//                  System.out.println("SocketTransmissionLine.startFlow from "+ccp+" read "+o.getClass());
                  // get a command a goin' and send result back
                  try {
                        pc = PowerPlant.command(getLeg(), ccp, (PKTransportMethodCall)o);
                  } catch(Exception e) {
                        System.out.println("** SocketTransmissionLine: command "+o+" Exception "+e);
                        e.printStackTrace();
                        pc = e;
                  }
                  // send status packet
//                  System.out.println("SocketTransmissionLine attempt terminal return packet "+pc);
                  // this will queue to leg 3, resultPacket table
                  // if return is null (void type from method.invoke), we wait
                  // for an asynchronous command return (i.e. we don't queue)
                  if( pc != null ) {
                        ccp.queuePacket(pc);
//                        System.out.println("SocketTransmissionLine: Return Packet "+pc+" queued to session "+ccp.getSession());
//                  } else
//                        System.out.println("SocketTransmissionLine: Asynchronous wait for session "+ccp.getSession());
                  }

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
//                         System.out.println("SocketTransmissionLine: attempt result "+o+" to client "+ccp.getSession());
                         //
                         if( compression ) {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                ObjectOutputStream oi = new ObjectOutputStream(baos);
                                //
//                              System.out.println(o.getClass().getName());
                                oi.writeObject(o);
                                oi.flush();
                                baos.flush();
                                //
                                byte[] bytes = baos.toByteArray();
                                oi.close();
                                baos.close();
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
                                OutputStream os = getSocket().getOutputStream();
                                os.write(bytes);
                                os.flush();
//                                System.out.println("SocketTransmissionLine Wrote "+String.valueOf(bytes.length)+" to "+ccp.getSession());
                         } else {
                                ObjectOutputStream oi = new ObjectOutputStream(getSocket().getOutputStream());
                                //
//                                System.out.println("SocketToPlant writing "+o.getClass().getName());
                                oi.writeObject(o);
                                oi.flush();
                                //
                         }
                } // for
		} // try
		 catch(EOFException eofe) {
                                System.out.println("Customer "+ccp.getSession()+" disconnected");
                                // should send broadcast to cluster to clean up all CustomerConnectionPanels
                                ConnectionPanel.removeCustomer(Session);
				return;
		 }
                 catch(SocketException se) {
                                System.out.println("Customer "+ccp.getSession()+" disconnected");
                                // should send broadcast to cluster to clean up all CustomerConnectionPanels
                                ConnectionPanel.removeCustomer(Session);
				return;
		 }
                 catch(StreamCorruptedException sce) {
                                System.out.println("Customer "+ccp.getSession()+" disconnected");
                                // should send broadcast to cluster to clean up all CustomerConnectionPanels
                                ConnectionPanel.removeCustomer(Session);
				return;
		 }
                 catch(Exception e) { 
                                System.out.println("SocketTransmissionLine EXCEPTION LOGGED !"); 
				System.err.println(e.getMessage()); e.printStackTrace(); 
		 }
			//
       }


}
