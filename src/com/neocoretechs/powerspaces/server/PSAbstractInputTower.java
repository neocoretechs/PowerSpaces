/**
* A "Tower" is an abstraction that defines IO for transmission line
* and PowerPlant connections, and in this case handles input Packets.
* @see PSInputOfPowerPlantCustomer
* @see PSInputOfPowerPlantConnection
* @see PSTowerSynch
* @see IPCLink
* @author Groff Copyright (C) NeoCoreTechs, Inc. 1998-2000
*/
package com.neocoretechs.powerspaces.server;
import java.util.*;
import java.net.*;
import java.io.*;

import com.neocoretechs.powerspaces.*;
import com.neocoretechs.powerspaces.server.cluster.IPCLink;
import com.neocoretechs.powerspaces.server.cluster.PSInputOfPowerPlantConnection;
import com.neocoretechs.powerspaces.server.cluster.PSInputOfPowerPlantCustomer;
import com.neocoretechs.powerspaces.server.cluster.synch.PSTowerSynch;
//
public abstract class PSAbstractInputTower implements PSCustomerInterface {
        //
        private Vector queuePackets;

        ObjectInputStream obin;
        Socket So;
        InputStream sin = null;

        public PSAbstractInputTower(Vector tqueue, Socket tso) {
                queuePackets = tqueue;
                So = tso;
                try {
                sin = So.getInputStream();
                } catch(Exception e) {
                        System.out.println("! BAD SOCKET IN PSAbstractInputTower CONSTRUCTOR !");
                }
        }       
        //
        public InputStream getInputStream() { return sin; }

        public Socket getSocket() { return So; }

        public abstract void Connect();

        public abstract void reInit() throws IOException;
        /**
        * the leg is the node 0 - parent, 1 - left, 2 - right, etc.
        */
        public abstract int getLeg();

        /**
        * Start the flow of Packets; read em, put em on the queue, notify
        * those waiting on the queue
        */
        public void startFlow() throws Exception {
                //
                  Packet p = null;
                  for(;;) {
                        // read customer session header
                        System.out.println("PSAbstractInputTower.startFlow getting input stream");
                        obin = new PSObjectInputStream( sin, PowerPlant.theClassLoader);
                        p = (Packet)(obin.readObject());
                        synchronized(queuePackets) {
                                queuePackets.addElement(p);
                                queuePackets.notify();
                        }
                  } // for
       }
}
