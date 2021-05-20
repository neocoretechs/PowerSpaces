package com.neocoretechs.powerspaces.server.cluster;
import com.neocoretechs.powerspaces.*;
import com.neocoretechs.powerspaces.server.PSAbstractInputTower;
import com.neocoretechs.powerspaces.server.PSAbstractOutputTower;
import com.neocoretechs.powerspaces.server.PowerPlantLegConnections;
import com.neocoretechs.powerspaces.server.PowerPlant;

import java.io.*;
import java.net.*;
import java.util.*;
/**
* IPCLink maintains input and output towers connected to a remote PowerPlant. <p>
* When we deal with other PowerPlants it is this object that is being utilized.
* For instance, to queue an outbound packet other classes that provide a
* queuePackets method are in reality invoking the one in this class, which
* contains a queuePackets Vector which maintains the Packet queue
* for the input and output legs.  The queuePacket method stuffs one on
* this Vector and performs a notify.  Tower linking and unlinking is done here.
* These objects are created as part of the "PowerLegs" which are created in the
* PowerPlant and govern connections between PowerSpaces.
* @see PowerPlantLegConnections
* @see PSAbstractInputTower
* @see PSAbstractOutputTower
* @author Groff Copyright (C) NeoCoreTechs, Inc. 1998,2000
*/
public final class IPCLink {
         // thse are the two server sockets
         PSAbstractInputTower sin = null;
         PSAbstractOutputTower sout = null;
         boolean linked = false;

         private Vector queuePackets = new Vector();

         public IPCLink() {}

         public IPCLink(PSAbstractInputTower ts) throws IOException {
                sin = ts;
         }

         public IPCLink(PSAbstractInputTower tsi, PSAbstractOutputTower tso) throws IOException {
                sin = tsi;
                sout = tso;
                sout.setPacketQueue(queuePackets);
                linked = true;
         }

         public synchronized void setInput(PSAbstractInputTower ts) {
                sin = ts;
                if( sout != null ) linked = true;
         }
         public synchronized void setOutput(PSAbstractOutputTower ts) {
                sout = ts;
                sout.setPacketQueue(queuePackets);
                if( sin != null) linked = true;
         }

        public synchronized PSAbstractInputTower getInput() { return sin; }
        public synchronized PSAbstractOutputTower getOutput() { return sout; }
        public synchronized boolean isLinked() { return linked; }
        public synchronized void setUnlinked() { linked = false; }

        public synchronized void unLink() {
                int tleg;
                if( sin != null ) {
                        tleg = sin.getLeg();
                        try {
                                sin.reInit();
                        } catch(Exception e) {
                                System.out.println("UnLink fault: "+e.getMessage());
                        }
                        sin = null;
                        System.out.println("IPCLink.unLink: input socket now unlinked");
                        try {
                                // kludgey, we don't want to restart ServerSockets, can't seem to re-bind address
                                if( tleg != 0 && tleg != 4 ) PowerPlant.startInputLeg(tleg);
                        } catch(Exception e) {
                                System.out.println(e.getMessage());
                                e.printStackTrace();
                        }
                }
                if( sout != null ) {
                        tleg = sout.getLeg();
                        try {
                                sout.reInit();
                        } catch(Exception e) {
                                System.out.println("UnLink fault: "+e.getMessage());
                        }
                        sout = null;
                        System.out.println("IPCLink.unLink: output socket now unlinked");
                        try {
                                // kludgey, we don't want to restart ServerSockets, can't seem to re-bind address
                                if( tleg != 0 && tleg != 4 ) PowerPlant.startOutputLeg(tleg);
                        } catch(Exception e) {
                                System.out.println(e.getMessage());
                                e.printStackTrace();
                        }
                }
                linked = false;
        }

        public void queuePacket(Object pps) throws IOException, PowerSpaceException, ClassNotFoundException
        {
                synchronized(queuePackets) {
                        queuePackets.addElement(pps);
                        queuePackets.notify();
                }
        }
}
