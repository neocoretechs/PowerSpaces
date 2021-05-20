package com.neocoretechs.powerspaces.server;
import java.util.*;
import java.net.*;
import java.io.*;
import com.neocoretechs.powerspaces.*;
/**
* PowerSpaces customer connection object.
* These are created and queued via PSTransformers in SocketTransmissionLine.
* When we connect we find or create a CustomerConnectionPanel with leg -1,
* then pass the session and CCP to startFlow in SocketToPole.
* @author Groff (C) NeoCoreTechs 1998
*/
public final class PoleToPlant extends SocketToPlant {
        private SocketTransmissionLine tl;
        private short transactType;
        //
        //
        public PoleToPlant() {}
        public PoleToPlant(SocketTransmissionLine ttl) {
                tl = ttl;
        }
        public PowerPlant getPowerPlant() { return tl.getPowerPlant(); }
        public Socket getSocket() { return tl.getSocket(); }
        public int getLeg() { return -1; }
        /**
        * Grab the session Packet header and find the CustomerConnectionPanel
        */
        public void Connect() {
                try {
                  //
                  String session = getSocket().getInetAddress().getHostAddress()+":"+
                                   String.valueOf(getSocket().getLocalPort())+":"+
                                   this.toString().substring(this.toString().indexOf('@'));
		  //
//                  System.out.println("PoleToPlant Connect started "+session);
		  //
                  CustomerConnectionPanel Clink;
                  Clink = (CustomerConnectionPanel)(ConnectionPanel.CustomerConnectionPanelTable.get(session));
                  // has an entry been made
                  if( Clink == null ) {
                        Clink = new CustomerConnectionPanel(session, -1);
                        ConnectionPanel.CustomerConnectionPanelTable.put(session,Clink);
                  }
                  //
                  startFlow(session, Clink);
                  //
                } catch(Exception e) { System.out.println("! EXCEPTION LOGGED in Customer Connect!"); System.err.println(e.getMessage()); e.printStackTrace(); }
       }
}
